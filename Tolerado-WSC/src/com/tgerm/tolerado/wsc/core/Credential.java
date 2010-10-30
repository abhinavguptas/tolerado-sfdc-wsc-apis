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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Salesforce login credential
 * 
 * @author abhinav
 * 
 */
public class Credential {
	/*
	 * API version defaulted to 20.0
	 */
	public static final String DEFAULT_API_VERSION = "20.0";

	public enum Environment {
		ProductionOrDeveloper, Sandbox, PreRelease, Other;
		public String getHostName() {
			switch (this) {
			case ProductionOrDeveloper:
				return "https://www.salesforce.com";
			case Sandbox:
				return "https://test.salesforce.com";
			case PreRelease:
				return "https://prerelwww.pre.salesforce.com";
			case Other:
				return null;
			}
			return null;
		}
	}

	private String userName;
	private String password;

	// For backward compatibility, its defaulted to Production or DE org.
	// Even that will be the case most of the times
	private Environment environment;
	private String hostName;
	private String apiVersion;

	private String sessionId;
	private String serverUrl;

	protected Credential() {

	}

	public Credential(String userName, String password) {
		this(userName, password, Environment.ProductionOrDeveloper);
	}

	public Credential(String userName, String password, Environment environment) {
		this(userName, password, environment, environment.getHostName());
	}

	public Credential(String userName, String password,
			Environment environment, String hostName) {
		this(userName, password, environment, hostName, DEFAULT_API_VERSION);
	}

	public Credential(String userName, String password,
			Environment environment, String hostName, String apiVersion) {
		this.userName = userName;
		this.password = password;
		this.environment = environment;
		this.hostName = hostName;
		this.apiVersion = apiVersion;
	}

	public static Credential createFromSessionToken(String sessionId,
			String serverUrl) {
		Credential credential = new Credential();
		credential.sessionId = sessionId;
		credential.serverUrl = serverUrl;
		return credential;
	}

	public boolean useSessionToken() {
		return !StringUtils.isBlank(sessionId);
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public String getHostName() {
		return hostName;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
