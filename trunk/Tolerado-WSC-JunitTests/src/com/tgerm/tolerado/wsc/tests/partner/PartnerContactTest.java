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

package com.tgerm.tolerado.wsc.tests.partner;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.DeleteResult;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;
import com.tgerm.tolerado.wsc.partner.ToleradoSobject;

public class PartnerContactTest extends TestCase {
	private static Log log = LogFactory.getLog(PartnerContactTest.class);

	private Credential credential = LoginCfg.self.getCredential();
	private String firstName;
	private String lastName;
	private String savedContactId;

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (savedContactId != null) {
			log.debug("Deleting Contact :" + firstName + " " + lastName
					+ " having recordId:" + savedContactId);
			ToleradoPartnerStub stub = new ToleradoPartnerStub(credential);
			DeleteResult[] deleteResults = stub
					.delete(new String[] { savedContactId });
			Assert.assertNotNull(deleteResults);
			Assert.assertEquals(1, deleteResults.length);
			Assert.assertTrue(deleteResults[0].getSuccess());
		}
	}

	public void testCRUDOnContact() throws Exception {
		ToleradoPartnerStub stub = new ToleradoPartnerStub(credential);
		//
		// Create a new Contact
		//
		SObject contact = new SObject();
		contact.setType("Contact");
		firstName = "Abhinav";
		lastName = "Gupta-" + System.currentTimeMillis();
		contact.setField("FirstName", firstName);
		contact.setField("LastName", lastName);

		log.debug("Creating new Contact :" + firstName + " " + lastName);
		SaveResult[] saveResults = stub.create(new SObject[] { contact });
		savedContactId = null;
		for (SaveResult saveResult : saveResults) {
			if (!saveResult.isSuccess()) {
				Assert
						.fail("Failed to create Contact record using Partner for name :"
								+ firstName + " " + lastName);
			} else {
				savedContactId = saveResult.getId();
				log.debug("Created new Contact :" + firstName + " " + lastName
						+ " having recordId:" + savedContactId);
			}
		}

		// 
		// Query the previously created Contact
		//
		queryAndAssertContact(stub);

		//
		// Update the same Contact
		//
		contact = new SObject();
		contact.setType("Contact");
		lastName = "Gupta-" + System.currentTimeMillis();
		contact.setField("LastName", lastName);
		contact.setId(savedContactId);

		SaveResult[] updateResults = stub.update(new SObject[] { contact });
		for (SaveResult updateResult : updateResults) {
			if (!updateResult.isSuccess()) {
				Assert
						.fail("Failed to update Contact record using Partner for name :"
								+ firstName + " " + lastName);
			} else {
				log.debug("Updated Contact :" + firstName + " " + lastName
						+ " having recordId:" + savedContactId);
			}
		}

		queryAndAssertContact(stub);

	}

	private SObject queryAndAssertContact(ToleradoPartnerStub stub) {
		log.debug("Querying Contact having recordId:" + savedContactId);
		QueryResult queryResult = stub
				.query("select Id, FirstName, LastName, LastModifiedDate from Contact where Id ='"
						+ savedContactId + "'");

		Assert.assertNotNull(queryResult);
		Assert.assertNotNull(queryResult.getRecords());
		// Only 1 contact should come
		Assert.assertEquals(queryResult.getRecords().length, 1);
		// Contact just retrieved
		ToleradoSobject contactFetched = new ToleradoSobject(queryResult.getRecords()[0]);
		Assert.assertEquals(contactFetched.getValue("FirstName"), firstName);
		Assert.assertEquals(contactFetched.getValue("LastName"), lastName);
		Date datetimeValue = contactFetched.getDatetimeValue("LastModifiedDate");
		Assert.assertNotNull(datetimeValue);
		return (SObject) contactFetched.getOriginalSObject();
	}
}
