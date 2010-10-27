package com.tgerm.tolerado.wsc.tests.partner.sobject;
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



import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.DeleteResult;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.core.ToleradoException;
import com.tgerm.tolerado.wsc.core.method.WSRecoverableMethod;
import com.tgerm.tolerado.wsc.partner.PartnerUtil;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;
import com.tgerm.tolerado.wsc.partner.ToleradoSobject;

/**
 * Shows how to use {@link ToleradoSobject} for CRUD operations using the
 * {@link ContactSObject}
 * 
 * @author abhinav
 * 
 */
public class ExtendingSObjectTest extends TestCase{
	// Logger from - Apache commons logging
	private static Log log = LogFactory.getLog(ExtendingSObjectTest.class);

	public void testSObjectExtension() {
		// Create a Sobject first
		String sobjId = createSObject();
		// Query and update The Same Sobject
		queryAndUpdateSObject(sobjId);
		// Delete it
		deleteSobject(sobjId);
	}

	/**
	 * Shows how to delete Sobjects
	 * 
	 * @param sObjId
	 *            Sobject ids to delete
	 */
	static void deleteSobject(final String sObjId) {
		ToleradoPartnerStub partnerStub = new ToleradoPartnerStub(LoginCfg.self
				.getCredential());
		WSRecoverableMethod<DeleteResult[], ToleradoPartnerStub> deleteMethod = new WSRecoverableMethod<DeleteResult[], ToleradoPartnerStub>(
				"delete") {
			@Override
			protected DeleteResult[] invokeActual(ToleradoPartnerStub stub)
					throws Exception {
				final String[] idsToDelete = new String[] { sObjId };
				return stub.getPartnerBinding().delete(idsToDelete);
			}
		};

		DeleteResult[] results = deleteMethod.invoke(partnerStub);
		PartnerUtil.checkSuccess(results);
	}

	/**
	 * Shows how to create a Sobject using the ToleradoSObject fixture
	 * 
	 * @return Id of the created Sobject
	 */
	static String createSObject() {
		// Get the partner stub
		ToleradoPartnerStub partnerStub = new ToleradoPartnerStub(LoginCfg.self
				.getCredential());
		ContactSObject contact = new ContactSObject();
		contact.setFirstName("Abhinav");
		contact.setLastName("Gupta");
		log.debug("Created ToleradoSobject: " + contact);
		// Get the updated Sobejct
		SObject updatedSObject = contact.getUpdatedSObject();

		SObject[] sObjects = new SObject[] { updatedSObject };
		SaveResult[] saveResults = partnerStub.create(sObjects);
		// Throws Error in case Save is failed
		PartnerUtil.checkSuccess(saveResults);
		return saveResults[0].getId();
	}

	/**
	 * Shows how to query the ToleradoSObject for a given Id
	 * 
	 * @param id
	 *            Sobject Id
	 */
	static void queryAndUpdateSObject(String id) {
		ToleradoPartnerStub partnerStub = new ToleradoPartnerStub(LoginCfg.self
				.getCredential());
		// Create SOQL
		String soql = "Select Id, FirstName, LastName from Contact where id = '"
				+ id + "'";
		QueryResult qr = partnerStub.query(soql);
		if (ArrayUtils.isEmpty(qr.getRecords())) {
			throw new ToleradoException("Failed to query the desired Sobject");
		}

		SObject sobj = qr.getRecords()[0];

		ContactSObject contactSobj = new ContactSObject(sobj);
		System.out.println("Queried Contact FirstName:"
				+ contactSobj.getFirstName());
		System.out.println("Queried Contact LastName:"
				+ contactSobj.getLastName());

		contactSobj.setFirstName("Abhinav2");
		SObject updatedSObject = contactSobj.getUpdatedSObject();

		SObject[] sObjects = new SObject[] { updatedSObject };
		SaveResult[] saveResults = partnerStub.update(sObjects);
		// Throws Error in case Save is failed
		PartnerUtil.checkSuccess(saveResults);
	}
}
