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



import com.sforce.soap.partner.sobject.wsc.SObject;
import com.tgerm.tolerado.wsc.partner.ToleradoSobject;

/**
 * How to create super easy to use SObject wrappers using ToleradoSobject
 * 
 * @author abhinav
 * 
 */
public class ContactSObject extends ToleradoSobject {
	public ContactSObject() {
		// Just pass the Sobject type
		super("Contact");
	}

	public ContactSObject(SObject sobj) {
		super(sobj);
	}

	public String getFirstName() {
		// delegates to getTextValue for fetching the correct field value
		return getTextValue("FirstName");
	}

	public String getLastName() {
		return getTextValue("LastName");
	}

	public void setFirstName(String val) {
		// delegates to setAttribute for setting the correct attribute.
		setValue("FirstName", val);
	}

	public void setLastName(String val) {
		setValue("LastName", val);
	}
}
