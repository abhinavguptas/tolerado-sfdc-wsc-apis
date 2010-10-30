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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.QueryResult;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.core.ToleradoSession;
import com.tgerm.tolerado.wsc.core.ToleradoSessionCache;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;

public class PartnerRetryableTest extends TestCase {
	private static Log log = LogFactory.getLog(PartnerRetryableTest.class);
	private Credential credential = LoginCfg.self.getCredential();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// clearing cache is important. As sessions would conflict with each
		// other across testXXX calls
		ToleradoSessionCache.clearCache();
	}

	/**
	 * Tests if Enterprise stub retries on session expire. This retry should
	 * work, as we will create Enterprise stub with credentials here, so the
	 * code should relogin in this case
	 * 
	 * @throws Exception
	 */
	public void testRetryableQueryOnContactWithCredentials() throws Exception {
		ToleradoPartnerStub stub = new ToleradoPartnerStub(credential);
		queryContact(stub);
		// logout
		stub.logout();
		// try re-query, this should work after retry, as we are having
		// credentials
		queryContact(stub);
	}

	/**
	 * Tests if Enterprise stub retries on session expire. This retry SHOULD NOT
	 * work, as we will create Enterprise stub with sessionId/serverurl here, so
	 * the code shouldn't relogin in this case
	 * 
	 * @throws Exception
	 */
	public void testRetryableQueryOnContactWithSessionToken() throws Exception {
		ToleradoPartnerStub stub = new ToleradoPartnerStub(credential);
		ToleradoSession session = stub.getSession();
		Credential tok = Credential.createFromSessionToken(session.getSessionId(),
				session.getPartnerServerUrl());
		// Create fresh enterprisestub using sessionid and serverurl only
		// no credential given here
		ToleradoPartnerStub stubFromToken = new ToleradoPartnerStub(tok);
		// Query using the new stub
		queryContact(stubFromToken);
		// logout, to invalidate the sessio
		stub.logout();

		try {
			// this call should fail now, as we
			// can't relogin automatically
			queryContact(stubFromToken);

			fail("Shouldn't reach this line, as session is expired. ");
		} catch (Exception e) {
		}

	}

	private void queryContact(ToleradoPartnerStub stub) {
		QueryResult qr = stub.query("Select Name from Contact LIMIT 1");
		Assert.assertNotNull(qr);
		SObject[] records = qr.getRecords();
		Assert.assertNotNull(records);
		Assert.assertEquals(1, records.length);
	}
}
