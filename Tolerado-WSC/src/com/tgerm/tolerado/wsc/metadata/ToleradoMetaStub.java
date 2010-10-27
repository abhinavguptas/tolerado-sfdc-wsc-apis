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

package com.tgerm.tolerado.wsc.metadata;

import com.sforce.soap.metadata.wsc.AsyncResult;
import com.sforce.soap.metadata.wsc.DescribeMetadataResult;
import com.sforce.soap.metadata.wsc.MetadataConnection;
import com.sforce.soap.metadata.wsc.RetrieveRequest;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.core.ToleradoException;
import com.tgerm.tolerado.wsc.core.ToleradoStub;
import com.tgerm.tolerado.wsc.core.method.WSRecoverableMethod;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;

/**
 * {@link ToleradoPartnerStub} extension for salesforce metadata wsdl
 * 
 * @author abhinav
 * 
 */
public class ToleradoMetaStub extends ToleradoStub {
	private MetadataConnection binding;

	public ToleradoMetaStub(Credential c) {
		super(c);
	}

	@Override
	public void prepare(boolean forceNew) {
		super.prepare(forceNew);
		// Create Metadata Connection
		ConnectorConfig metadataConfig = new ConnectorConfig();
		// SFDC Session Id pulled from WSCSession
		metadataConfig.setSessionId(session.getSessionId());
		// Metadata Service Endpoint used from WSCSession
		metadataConfig.setServiceEndpoint(session.getMetadataServerUrl());
		try {
			binding = com.sforce.soap.metadata.wsc.Connector
					.newConnection(metadataConfig);
		} catch (ConnectionException e) {
			throw new ToleradoException(
					"Failed to instantiate MetadataConnection, user:"
							+ credential.getUserName(), e);
		}

	}

	/**
	 * Retrieve metadata call wrapper
	 * 
	 * @param retrieveRequest
	 * @return
	 */
	public AsyncResult retrieve(final RetrieveRequest retrieveRequest) {
		WSRecoverableMethod<AsyncResult, ToleradoMetaStub> wsMethod = new WSRecoverableMethod<AsyncResult, ToleradoMetaStub>(
				"retrieve") {
			@Override
			protected AsyncResult invokeActual(ToleradoMetaStub stub)
					throws Exception {
				return stub.getMetaBinding().retrieve(retrieveRequest);
			}
		};
		AsyncResult results = wsMethod.invoke(this);
		return results;
	}

	public DescribeMetadataResult describeMetadata(final double asOfVersion) {
		return new WSRecoverableMethod<DescribeMetadataResult, ToleradoMetaStub>(
				"describeMetadata") {
			@Override
			protected DescribeMetadataResult invokeActual(ToleradoMetaStub stub)
					throws Exception {
				return getMetaBinding().describeMetadata(asOfVersion);
			}
		}.invoke(this);
	}

	/**
	 * Gives handle to the internal {@link MetadataConnection}
	 * 
	 * @return {@link MetadataConnection}
	 */
	public MetadataConnection getMetaBinding() {
		return binding;
	}
}
