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

package com.tgerm.tolerado.wsc.partner;

import com.sforce.soap.partner.wsc.*;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.core.LoginDriver;
import com.tgerm.tolerado.wsc.core.ToleradoSession;
import com.tgerm.tolerado.wsc.core.method.WSErrorHandler;

/**
 * Login driver that uses Partner WSDL to create session
 * 
 * @author abhinav
 * 
 */
public class PartnerLoginDriver implements LoginDriver {

	public final class PartnerSession extends ToleradoSession {
		private final LoginResult loginResult;

		private PartnerSession(LoginResult lres) {
			this.loginResult = lres;
		}

		@Override
		public String getPartnerServerUrl() {
			/**
			 * Give accurate Partner Endpoint URL
			 */
			return loginResult.getServerUrl();
		}

		@Override
		public String getSessionId() {
			return loginResult.getSessionId();
		}

		@Override
		public Object getLoginResult() {
			return loginResult;
		}

		@Override
		public String getMetadataServerUrl() {
			return loginResult.getMetadataServerUrl();
		}

		@Override
		public WSErrorHandler getErrorHandler() {
			return new PartnerWSErrorHandler();
		}
	}

	@Override
	public ToleradoSession login(Credential cred) {
		PartnerLoginWSMethod loginWSMethod = new PartnerLoginWSMethod(cred);
		final LoginResult lres = loginWSMethod.invoke(null);
		ToleradoSession toleradoSession = new PartnerSession(lres);
		return toleradoSession;
	}

	@Override
	public Type getType() {
		return Type.Partner;
	}

}
