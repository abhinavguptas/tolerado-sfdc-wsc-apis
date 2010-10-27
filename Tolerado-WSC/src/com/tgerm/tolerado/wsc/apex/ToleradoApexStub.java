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

package com.tgerm.tolerado.wsc.apex;

import com.sforce.soap.apex.wsc.Connector;
import com.sforce.soap.apex.wsc.RunTestsRequest;
import com.sforce.soap.apex.wsc.RunTestsResult;
import com.sforce.soap.apex.wsc.SoapConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.core.ToleradoException;
import com.tgerm.tolerado.wsc.core.ToleradoStub;
import com.tgerm.tolerado.wsc.core.method.WSRecoverableMethod;

/**
 * Stub for Apex WSDL, it does all the house keeping for session headers and
 * keeping the stubs in correct state. Also exposes few WSDL methods for use
 * 
 * @author abhinav
 * 
 */
public class ToleradoApexStub extends ToleradoStub {
	private SoapConnection apexBinding;

	public ToleradoApexStub(Credential c) {
		super(c);
	}

	/**
	 * Prepares the apex binding, sets all the headers and housekeeping
	 * required.
	 */
	@Override
	public void prepare(boolean forceNewSession) {
		super.prepare(forceNewSession);
		// Create Apex Connection
		ConnectorConfig apexConfig = new ConnectorConfig();
		// SFDC Session Id pulled from WSCSession
		apexConfig.setSessionId(session.getSessionId());
		String apexEndpoint = session.getApexServerUrl();
		// Metadata Service Endpoint used from WSCSession
		apexConfig.setServiceEndpoint(apexEndpoint);
		try {
			apexBinding = Connector.newConnection(apexConfig);
		} catch (ConnectionException e) {
			throw new ToleradoException(
					"Failed to instantiate ApexConnection, user:"
							+ credential.getUserName(), e);

		}

	}

	/**
	 * Returns handle to the actual {@link ApexBindingStub}
	 * 
	 * @return {@link ApexBindingStub}
	 */
	public SoapConnection getApexBinding() {
		return apexBinding;
	}

	/**
	 * Run selective tests
	 * 
	 * @param runTestsRequest
	 *            {@link RunTestsRequest} having details of classes etc on which
	 *            tests should be run
	 * @return {@link RunTestsResult} Test results
	 */
	public RunTestsResult runTests(final RunTestsRequest runTestsRequest) {
		RunTestsResult results = new WSRecoverableMethod<RunTestsResult, ToleradoApexStub>(
				"runTests") {
			@Override
			protected RunTestsResult invokeActual(ToleradoApexStub stub)
					throws Exception {
				return stub.getApexBinding().runTests(runTestsRequest);
			}
		}.invoke(this);
		return results;
	}

	/**
	 * Runs all tests cases
	 * 
	 * @return {@link RunTestsResult} Test results
	 */
	public RunTestsResult runAllTests() {
		return new WSRecoverableMethod<RunTestsResult, ToleradoApexStub>(
				"runTests") {
			@Override
			protected RunTestsResult invokeActual(ToleradoApexStub stub)
					throws Exception {
				RunTestsRequest runTestsRequest = new RunTestsRequest();
				runTestsRequest.setAllTests(true);
				runTestsRequest.setNamespace("");
				return stub.getApexBinding().runTests(runTestsRequest);
			}
		}.invoke(this);
	}
}
