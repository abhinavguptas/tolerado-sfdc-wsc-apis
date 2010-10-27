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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.enterprise.sobject.wsc.Account;
import com.sforce.soap.enterprise.sobject.wsc.Contact;
import com.sforce.soap.enterprise.sobject.wsc.SObject;
import com.sforce.soap.enterprise.wsc.DeleteResult;
import com.sforce.soap.enterprise.wsc.SaveResult;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.enterprise.EnterpriseUtil;
import com.tgerm.tolerado.wsc.enterprise.ToleradoEnterpriseStub;

public class AllOrNoneHeaderTest extends TestCase {
	private static Log log = LogFactory.getLog(AllOrNoneHeaderTest.class);
	private Credential credential;
	private Set<String> savedRecordIds;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		credential = LoginCfg.self.getCredential();
		savedRecordIds = new HashSet<String>();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (savedRecordIds != null) {
			ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
			DeleteResult[] deleteResults = stub.delete(savedRecordIds
					.toArray(new String[] {}));
			Assert.assertNotNull(deleteResults);
			Assert.assertEquals(savedRecordIds.size(), deleteResults.length);
			EnterpriseUtil.checkSuccess(deleteResults);
		}
	}

	/**
	 * Creates a test scenario, where 2 contacts are created. One with complete
	 * details and other with the missing details. AllOrNoneHeader is set to
	 * TRUE, The goal is to see if the transaction rollbacks in all.
	 * 
	 * @throws Exception
	 */
	public void testRollBackOnContacts() throws Exception {
		ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
		//
		// Create a new Contact
		//
		Contact c1 = new Contact();
		String firstName = "Abhinav";
		String lastName = "Gupta-" + System.currentTimeMillis();
		c1.setFirstName(firstName);
		c1.setLastName(lastName);

		//
		// Create a new Contact, created without last name intentionally to fail
		//
		Contact c2 = new Contact();
		c2.setFirstName("Abhinav1");

		// Using AllOrNoneHeader = true, to make the rollback happen
		stub.setAllOrNoneHeader(true);

		SaveResult[] saveResults = stub.create(new SObject[] { c1, c2 });
		Assert.assertNotNull(saveResults);
		Assert.assertEquals(2, saveResults.length);
		Assert.assertEquals(false, saveResults[0].getSuccess());
		Assert.assertNull(saveResults[0].getId());
		Assert.assertEquals(false, saveResults[1].getSuccess());
		Assert.assertNull(saveResults[1].getId());
	}

	/**
	 * Creates a test scenario, where 2 contacts are created. One with complete
	 * details and other with the missing details. AllOrNoneHeader = false, The
	 * goal is to see if the parital commits goes well.
	 * 
	 * @throws Exception
	 */
	public void testPartialCommitOnContacts() throws Exception {
		ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
		//
		// Create a new Contact
		//
		Contact c1 = new Contact();
		String firstName = "Abhinav";
		String lastName = "Gupta-" + System.currentTimeMillis();
		c1.setFirstName(firstName);
		c1.setLastName(lastName);

		//
		// Create a new Contact, created without last name intentionally to fail
		//
		Contact c2 = new Contact();
		c2.setFirstName("Abhinav1");

		// Using AllOrNoneHeader = false, to make the parital commits happen
		stub.setAllOrNoneHeader(false);

		SaveResult[] saveResults = stub.create(new SObject[] { c1, c2 });
		Assert.assertNotNull(saveResults);
		Assert.assertEquals(2, saveResults.length);
		// Contact 1 should be created
		Assert.assertEquals(true, saveResults[0].getSuccess());
		Assert.assertNotNull(saveResults[0].getId());
		// Contact 2 should be failed
		Assert.assertEquals(false, saveResults[1].getSuccess());
		Assert.assertNull(saveResults[1].getId());

		// ensure cleanup
		savedRecordIds.add(saveResults[0].getId());
	}

	/**
	 * Creates a test scenario, where 1 contact + 1 Account are created. One
	 * with complete details and other with the missing details. AllOrNoneHeader
	 * is set to TRUE, The goal is to see if the transaction rollbacks in all.
	 * We will also see, if differnt sobject types goes well with this
	 * 
	 * @throws Exception
	 */
	public void testRollBackOnContactAndAccount() throws Exception {
		ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
		//
		// Create a new Contact
		//
		Contact c1 = new Contact();
		String firstName = "Abhinav";
		c1.setFirstName(firstName);
		//
		// / We are not setting last name to make it fail
		//
		// String lastName = "Gupta-" + System.currentTimeMillis();
		// c1.setField("LastName", lastName);

		//
		// Create a new Contact, created without last name intentionally to fail
		//
		Account a1 = new Account();
		a1.setName("Abhinav-Account");

		// Using AllOrNoneHeader = true, to make the rollback happen
		stub.setAllOrNoneHeader(true);

		SaveResult[] saveResults = stub.create(new SObject[] { c1, a1 });
		Assert.assertNotNull(saveResults);
		Assert.assertEquals(2, saveResults.length);
		Assert.assertEquals(false, saveResults[0].getSuccess());
		Assert.assertNull(saveResults[0].getId());
		Assert.assertEquals(false, saveResults[1].getSuccess());
		Assert.assertNull(saveResults[1].getId());
	}

	/**
	 * Creates a test scenario, where 1 contact + 1 Account are created. One
	 * with complete details and other with the missing details. AllOrNoneHeader
	 * is set to FALSE, The goal is to see if partial commits work across the
	 * sobject types. We will also see, if different sobject types goes well
	 * with this
	 * 
	 * @throws Exception
	 */
	public void testPartialCommitsOnContactAndAccount() throws Exception {
		ToleradoEnterpriseStub stub = new ToleradoEnterpriseStub(credential);
		//
		// Create a new Contact
		//
		Contact c1 = new Contact();
		String firstName = "Abhinav";
		c1.setFirstName(firstName);
		//
		// / We are not setting last name to make it fail
		//
		// String lastName = "Gupta-" + System.currentTimeMillis();
		// c1.setField("LastName", lastName);

		//
		// Create a new Contact, created without last name intentionally to fail
		//
		Account a1 = new Account();
		a1.setName("Abhinav-Account");

		// Using AllOrNoneHeader = false, to make the partial commits happen
		stub.setAllOrNoneHeader(false);

		SaveResult[] saveResults = stub.create(new SObject[] { c1, a1 });
		Assert.assertNotNull(saveResults);
		Assert.assertEquals(2, saveResults.length);
		Assert.assertEquals(false, saveResults[0].getSuccess());
		Assert.assertNull(saveResults[0].getId());
		Assert.assertEquals(true, saveResults[1].getSuccess());
		Assert.assertNotNull(saveResults[1].getId());
	}

}
