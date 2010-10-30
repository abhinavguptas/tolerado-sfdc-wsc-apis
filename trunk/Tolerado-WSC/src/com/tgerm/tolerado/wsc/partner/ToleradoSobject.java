/*
Copyright (c) 2010 tgerm.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tgerm.tolerado.wsc.partner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.wsdl.Constants;

public class ToleradoSobject {

	protected XmlObject orignalSObj;
	/**
	 * Created once for fast parsing of Sobject later on.
	 */
	protected Map<String, XmlObject> msgElementCache;
	/**
	 * Tracks the changes made to this Sobject instance
	 */
	protected Map<String, XmlObject> modifiedMsgElements;

	{
		initCache();
	}

	/**
	 * Constructs using the given {@link SObject} instance
	 * 
	 * @param sobj
	 */
	public ToleradoSobject(XmlObject sobj) {
		init(sobj);
	}

	/**
	 * Constructs an in memory {@link SObject} for the give Sobject type
	 * 
	 * @param sobjectType
	 *            like Contact, Custom_Object__c
	 */
	public ToleradoSobject(String sobjectType) {
		init(sobjectType);
	}

	protected void init(XmlObject sobj) {
		this.orignalSObj = sobj;
		Iterator<XmlObject> children = this.orignalSObj.getChildren();
		while (children.hasNext()) {
			XmlObject kid = children.next();
			msgElementCache.put(kid.getName().getLocalPart(), kid);
		}
	}

	protected void init(String sobjectType) {
		this.orignalSObj = new SObject();
		orignalSObj.setField("type", sobjectType);
	}

	protected void initCache() {
		msgElementCache = new HashMap<String, XmlObject>();
		modifiedMsgElements = new HashMap<String, XmlObject>();
	}

	/**
	 * 
	 * @return Returns reference to the Original {@link SObject} wrapped by this
	 *         instance
	 */
	public XmlObject getOriginalSObject() {
		return orignalSObj;
	}

	/**
	 * @return Returns the updated sobject. "Updated" {@link SObject} means any
	 *         changes done to this instance via
	 *         {@link ToleradoSobject#setAttribute(String, Object)}. One can use
	 *         that Sobject directly in Partner update or create calls
	 */
	public SObject getUpdatedSObject() {
		SObject modified = new SObject();
		modified.setFieldsToNull(new String[] {});
		modified.setType(getType());
		modified.setId(getId());
		Collection<XmlObject> updatedElements = modifiedMsgElements.values();
		for (XmlObject xmlObject : updatedElements) {
			modified.setField(xmlObject.getName().getLocalPart(),
					xmlObject.getValue());
		}
		return modified;
	}

	public String getId() {
		return (String) orignalSObj.getField("Id");
	}

	public void setId(String Id) {
		orignalSObj.setField("Id", Id);
	}

	public String getType() {
		return (String) orignalSObj.getField("type");
	}

	/**
	 * An all in one setter for Sobject. Just mention the attribute to update
	 * and the new value, it will handle the rest.
	 * 
	 * @param attribName
	 *            Attribute Name to update
	 * @param newVal
	 *            new value of attribute
	 */
	public void setAttribute(String attribName, Object newVal) {
		XmlObject xobj = new XmlObject(new QName(Constants.PARTNER_SOBJECT_NS,
				attribName), newVal);
		// Log this as a change
		modifiedMsgElements.put(attribName, xobj);
		// Update the original mapping to, so that getters are working correctly
		msgElementCache.put(attribName, xobj);
	}

	public XmlObject getLookupObject(String lookupName) {
		return msgElementCache.get(lookupName);
	}

	public Iterator<XmlObject> getRelatedList(String relationshipName) {
		Iterator<XmlObject> children = orignalSObj.evaluate(relationshipName
				+ "/records");
		return children;
	}

	/**
	 * Returns nested {@link SObject} for the given relationship name
	 * 
	 * For ex. if Contact has lookup to Account and relationship name is
	 * "Account", then this call should return the Sobject carrying Account
	 * stuff
	 * 
	 * @param name
	 *            relationship for which {@link SObject} should be returned
	 * @return parsed Nested {@link SObject} instance
	 */
	public XmlObject getNestedSObject(String name) {
		return msgElementCache.get(name);
	}

	public String getNestedValue(String parent, String childName) {
		XmlObject parentObj = getNestedSObject(parent);
		return (String) (parentObj != null ? parentObj.getField(childName)
				: null);
	}

	/**
	 * Returns the String/TEXT value for the given attribute name
	 * 
	 * @param attributeName
	 *            Name of attribute or Sobject field whose value should be
	 *            fetched
	 * @return Text value of the attribute
	 */
	public String getTextValue(String attributeName) {
		return (String) getValue(attributeName);
	}

	public Object getValue(String attributeName) {
		XmlObject xObj = msgElementCache.get(attributeName);
		return (xObj != null ? xObj.getValue() : null);
	}

	/**
	 * Returns the {@link Boolean} value for the given attribute name
	 * 
	 * @param attributeName
	 *            Name of attribute or Sobject field whose value should be
	 *            fetched
	 * @return {@link Boolean} value of the attribute
	 */
	public Boolean getBoolValue(String attributeName) {
		Object val = getValue(attributeName);
		return val != null ? Boolean.parseBoolean(val.toString()) : null;
	}

	/**
	 * Returns the {@link BigInteger} value for the given attribute name
	 * 
	 * @param attributeName
	 *            Name of attribute or Sobject field whose value should be
	 *            fetched
	 * @return Int value of the attribute
	 */
	public Integer getIntValue(String attributeName) {
		Object val = getValue(attributeName);
		return val != null ? Integer.parseInt(val.toString()) : null;
	}

	/**
	 * Returns the Double value for the given attribute name
	 * 
	 * @param attributeName
	 *            Name of attribute or Sobject field whose value should be
	 *            fetched
	 * @return Double value of the attribute
	 */
	public Double getDoubleValue(String attributeName) {
		Object val = getValue(attributeName);
		return val != null ? Double.parseDouble(val.toString()) : null;
	}

	public Object getField(String name) {
		return orignalSObj.getField(name);
	}

	public List<XmlObject> getChildrens(String relationshipName) {
		List<XmlObject> sObjects = new ArrayList<XmlObject>();
		XmlObject relationElement = (XmlObject) msgElementCache
				.get(relationshipName);
		Iterator<XmlObject> children = relationElement.getChildren();
		while (children.hasNext()) {
			XmlObject nextChild = children.next();
			// Only children with "records" contain the real Note records,
			// so skip rest like done, queryLocator and size etc
			if (!nextChild.getName().getLocalPart().equals("records"))
				continue;
			sObjects.add(nextChild);
		}
		return sObjects;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}
}
