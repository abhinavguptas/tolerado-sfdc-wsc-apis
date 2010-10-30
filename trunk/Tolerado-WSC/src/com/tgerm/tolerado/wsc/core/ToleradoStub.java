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

package com.tgerm.tolerado.wsc.core;

import com.sforce.ws.ConnectorConfig;

/**
 * Base class for all Tolerado Stubs, it gives transparent salesforce session
 * caching. Also, gives api to renew session if required
 * 
 * @author abhinav
 * 
 */
public abstract class ToleradoStub {
	// Change this, in case you want something lesser
	public static int DEFAULT_CONNECTOR_READ_TIMEOUT = Integer.MAX_VALUE;

	protected Credential credential;
	protected ToleradoSession session;
	protected LoginDriver loginDriver;
	protected ConnectorConfig connectorConfig;

	public ToleradoStub(Credential c) {
		// Loads the first available driver to use
		this(c, LoginDriverLocator.locate());
	}

	protected ToleradoStub(Credential credential, LoginDriver loginDriver) {
		super();
		this.credential = credential;
		this.loginDriver = loginDriver;
		prepare(false);
	}

	/**
	 * Prepares SFDC Session either by Making Login calls using Partner or
	 * Enterprise WSDL. Or by using the session token i.e. SessionId and
	 * ServerUrl
	 * 
	 * @param forceNew
	 *            If true, SessionCache would be bypassed, and login is done
	 *            again using user name and password
	 */
	public void prepare(boolean forceNew) {
		if (!credential.useSessionToken()) {
			session = ToleradoSessionCache.sessionFor(getLoginDriver(),
					credential, forceNew);
		} else {
			session = new ToleradoTokenSession(credential);
		}
		// Prepare Connector Config
		this.connectorConfig = prepareConnectorConfig(session);

		// Give a chance to WSDL specific stubs to prepare
		prepareBinding(forceNew);

	}

	/**
	 * Each WSDL, knows the endpoint
	 * 
	 * @return
	 */
	protected abstract String getServiceEndpoint();

	/**
	 * WSDL's like partner, apex etc can prepare them self in this call
	 * 
	 * @param forceNew
	 *            Create new Session with salesforce, discarding any caching
	 *            done internally
	 */
	protected abstract void prepareBinding(boolean forceNew);

	public ToleradoSession getSession() {
		return session;
	}

	/**
	 * Override this method, if you want to take control over
	 * {@link ConnectorConfig}
	 * 
	 * @param session
	 *            Tolerado Session having SessionId and ServerUrl information
	 * @return A brand new {@link ConnectorConfig}, that is prepared for making
	 *         authorized webservice calls.
	 */
	protected ConnectorConfig prepareConnectorConfig(ToleradoSession session) {
		// Create Apex Connection
		ConnectorConfig cfg = new ConnectorConfig();
		cfg.setManualLogin(true);
		cfg.setServiceEndpoint(getServiceEndpoint());
		cfg.setSessionId(session.getSessionId());
		// This should be good for all
		cfg.setReadTimeout(DEFAULT_CONNECTOR_READ_TIMEOUT);
		return cfg;
	}

	/**
	 * 
	 * @return The salesforce login {@link Credential} for this stub
	 */
	public Credential getCredential() {
		return credential;
	}

	protected LoginDriver getLoginDriver() {
		return loginDriver;
	}

}
