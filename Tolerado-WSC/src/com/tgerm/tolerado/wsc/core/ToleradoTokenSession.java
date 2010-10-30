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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tgerm.tolerado.wsc.core.method.WSErrorHandler;

public class ToleradoTokenSession extends ToleradoSession {
	/*
	 * Pattern to parse ServerUrls and transform them into other
	 */
	static final Pattern URL_PARSER = Pattern
			.compile("[/]services[/]Soap[/]([a-z])");

	protected Credential authToken;
	protected String metadataUrl;

	public ToleradoTokenSession(Credential authToken) {
		super();
		this.authToken = authToken;
		metadataUrl = toWSDLEndpointUrl(authToken.getServerUrl(), "m");
	}

	@Override
	public String getMetadataServerUrl() {
		return metadataUrl;
	}

	@Override
	public String getSessionId() {
		return authToken.getSessionId();
	}

	@Override
	public Object getLoginResult() {
		// Login result would not be available, for user initiaited sessions
		return null;
	}

	@Override
	public WSErrorHandler getErrorHandler() {
		return new WSErrorHandler() {
			@Override
			protected boolean isLoginExpiredForWSDL(Exception ex) {
				// As this Session, is user created i.e. ToleradoStubs didn't
				// made the login call to get
				// the sessionId and serverUrls. So we can't decide if login is
				// expired or not.
				String faultString = CoreUtil.printStackTrace(ex);
				if (faultString != null
						&& faultString.indexOf("INVALID_SESSION_ID") != -1) {
					return true;
				}
				return false;
			}

			@Override
			protected boolean canRetryForWSDL(Exception ex) {
				return false;
			}
		};
	}

	private static String toWSDLEndpointUrl(String fromWSDLEndpointUrl,
			String wsdlEndpointToken) {
		Matcher m = URL_PARSER.matcher(fromWSDLEndpointUrl);
		StringBuffer sb = new StringBuffer();
		if (m.find()) {
			m.appendReplacement(sb, "/services/Soap/" + wsdlEndpointToken);
			m.appendTail(sb);
		}
		String uurl = sb.toString();
		return uurl;
	}

	public static void main(String[] args) {
		String surl = "https://cs3-api.salesforce.com/services/Soap/c/20.0/00DQ0000000BJNq";
		String sessionId = "00DQ0000000BJNq!AQsAQG0CzzDJD0E4W6sr2qrtDI5eTtZbShTTy9pMmfBB2VwmXSJJiBg0taiIRKxhL4cX6dzgYJWG1NSLw_JtQBRP4itFeRNd";
		Credential t = Credential.createFromSessionToken(sessionId, surl);
		ToleradoTokenSession sess = new ToleradoTokenSession(t);
		System.out.println(sess);

	}

}
