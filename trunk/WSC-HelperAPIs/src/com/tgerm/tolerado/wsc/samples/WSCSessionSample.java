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
package com.tgerm.tolerado.wsc.samples;

import com.sforce.soap.apex.RunTestsRequest;
import com.sforce.soap.apex.RunTestsResult;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.tgerm.tolerado.samples.cfg.Credential;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.samples.WSCSession.LoginWSDL;

/**
 * @author abhinav
 */
public class WSCSessionSample {

	public static void main(String[] args) throws ConnectionException {
		Credential credential = LoginCfg.self.getCredential();
		LoginWSDL loginWSDL = LoginWSDL.Enterprise;

		// Un-Comment this line, if you want to login via Partner WSDL
		//LoginWSDL loginWSDL = LoginWSDL.Partner;
		WSCSession session = new WSCSession(loginWSDL,
				credential.getUserName(), credential.getPassword());

		// Partner WSDL Code Sample
		doPartner(session);

		// Metadata WSDL Code Sample
		doMeta(session);

		// Apex WSDL Code Sample
		doApex(session);

		// Enterprise WSDL Code Sample
		doEnterprise(session);

	}

	private static void doEnterprise(WSCSession session)
			throws ConnectionException {
		// 
		// Create Enterprise Connection
		//
		ConnectorConfig entCfg = new ConnectorConfig();
		entCfg.setManualLogin(true);
		entCfg.setServiceEndpoint(session.getEnterpriseServerUrl());
		entCfg.setSessionId(session.getSessionId());
		EnterpriseConnection entConn = com.sforce.soap.enterprise.Connector
				.newConnection(entCfg);

		// Enterprise WSDL for query
		QueryResult queryResults = entConn
				.query("SELECT Id, FirstName, LastName FROM Contact LIMIT 1");
		if (queryResults.getSize() > 0) {
			Contact c = (Contact) queryResults.getRecords()[0];
			System.out.println("EnterPrise WSDL : Queried Contact Name: "
					+ c.getFirstName() + " " + c.getLastName());
		}
	}

	private static void doPartner(WSCSession session)
			throws ConnectionException {
		//
		// Create new Partner Connection
		//
		ConnectorConfig config = new ConnectorConfig();
		config.setManualLogin(true);
		config.setServiceEndpoint(session.getPartnerServerUrl());
		config.setSessionId(session.getSessionId());
		PartnerConnection partnerConn = Connector.newConnection(config);

		// Create a new Contact
		SObject contact = new SObject();
		contact.setType("Contact");
		contact.setField("FirstName", "Abhinav");
		contact.setField("LastName", "Gupta");
		SaveResult[] create = partnerConn.create(new SObject[] { contact });
		for (SaveResult saveResult : create) {
			if (!saveResult.isSuccess()) {
				throw new RuntimeException(
						"Failed to save contact via Partner WSDL");
			}
		}
		System.out.println("Partner-WSDL : Save successfully done");
	}

	private static void doApex(WSCSession session) throws ConnectionException {
		//
		// Create Apex Connection
		//
		ConnectorConfig apexConfig = new ConnectorConfig();
		apexConfig.setSessionId(session.getSessionId());
		apexConfig.setServiceEndpoint(session.getApexServerUrl());
		SoapConnection apexConnection = com.sforce.soap.apex.Connector
				.newConnection(apexConfig);

		// Run All Tests in the org
		RunTestsRequest runTestsRequest = new RunTestsRequest();
		runTestsRequest.setAllTests(true);
		runTestsRequest.setNamespace("");
		RunTestsResult runTests = apexConnection.runTests(runTestsRequest);
		System.out.println(" Apex-WSDL: NumTestsRun :  "
				+ runTests.getNumTestsRun());
	}

	private static void doMeta(WSCSession session) throws ConnectionException {
		// 
		// Create Metadata Connection
		//
		ConnectorConfig metadataConfig = new ConnectorConfig();
		metadataConfig.setSessionId(session.getSessionId());
		// Set the metdata server url from LoginResult
		metadataConfig.setServiceEndpoint(session.getMetadataServerUrl());
		MetadataConnection metadataConnection = com.sforce.soap.metadata.Connector
				.newConnection(metadataConfig);

		// Try describing the metadata
		DescribeMetadataResult describeMetadata = metadataConnection
				.describeMetadata(15.0);
		System.out
				.println("Metadata WSDL : OrgName from describeMetadata() call"
						+ describeMetadata.getOrganizationNamespace());
	}
}
