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

package com.tgerm.tolerado.wsc.tests.apex;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.apex.wsc.RunTestsResult;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.apex.ToleradoApexStub;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.core.ToleradoSession;

public class ApexTest extends TestCase {
	private static Log log = LogFactory.getLog(ApexTest.class);
	private Credential credential = LoginCfg.self.getCredential();

	public void testCRUDOnContact() throws Exception {
		// Create stub
		ToleradoApexStub aStub = new ToleradoApexStub(credential);
		// This call does the rest
		log.debug("Running All Tests using Apex WSDL");
		RunTestsResult runResult = aStub.runAllTests();
		Assert.assertNotNull(runResult);
	}

	public void testCRUDOnContactViaSessionToken() throws Exception {
		// Create stub
		ToleradoApexStub aStub = new ToleradoApexStub(credential);
		ToleradoSession session = aStub.getSession();
		Credential tok = Credential.createFromSessionToken(
				session.getSessionId(), session.getPartnerServerUrl());
		// Create fresh stub using sessionid and serverurl only
		// no credential given here
		ToleradoApexStub stubFromToken = new ToleradoApexStub(tok);
		// This call does the rest
		log.debug("Running All Tests using Apex WSDL via Token");
		RunTestsResult runResult = stubFromToken.runAllTests();
		Assert.assertNotNull(runResult);
	}
}
