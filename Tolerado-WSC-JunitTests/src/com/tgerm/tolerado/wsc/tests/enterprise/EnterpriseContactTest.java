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

package com.tgerm.tolerado.wsc.tests.enterprise;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.enterprise.sobject.wsc.Contact;
import com.sforce.soap.enterprise.sobject.wsc.SObject;
import com.sforce.soap.enterprise.wsc.DeleteResult;
import com.sforce.soap.enterprise.wsc.QueryResult;
import com.sforce.soap.enterprise.wsc.SaveResult;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.enterprise.ToleradoEnterpriseStub;

public class EnterpriseContactTest extends TestCase {
	private static Log log = LogFactory.getLog(EnterpriseContactTest.class);

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
			ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
			DeleteResult[] deleteResults = stub
					.delete(new String[] { savedContactId });
			Assert.assertNotNull(deleteResults);
			Assert.assertEquals(1, deleteResults.length);
			Assert.assertTrue(deleteResults[0].getSuccess());
		}
	}

	public void testCRUDOnContact() throws Exception {
		ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
		//
		// Create a new Contact
		//
		Contact contact = new Contact();
		firstName = "Abhinav";
		lastName = "Gupta-" + System.currentTimeMillis();
		contact.setFirstName(firstName);
		contact.setLastName(lastName);

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
		contact = new Contact();
		lastName = "Gupta-" + System.currentTimeMillis();
		contact.setLastName(lastName);
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

	private SObject queryAndAssertContact(ToleradoEnterpriseStub stub) {
		log.debug("Querying Contact having recordId:" + savedContactId);
		QueryResult queryResult = stub
				.query("select Id, FirstName, LastName from Contact where Id ='"
						+ savedContactId + "'");

		Assert.assertNotNull(queryResult);
		Assert.assertNotNull(queryResult.getRecords());
		// Only 1 contact should come
		Assert.assertEquals(queryResult.getRecords().length, 1);
		// Contact just retrieved
		Contact contactFetched = (Contact) queryResult.getRecords()[0];
		Assert.assertEquals(contactFetched.getFirstName(), firstName);
		Assert.assertEquals(contactFetched.getLastName(), lastName);
		return contactFetched;
	}
}
