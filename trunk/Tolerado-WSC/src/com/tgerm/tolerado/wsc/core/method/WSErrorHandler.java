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

package com.tgerm.tolerado.wsc.core.method;

import java.util.Arrays;

import java.util.List;

import com.sforce.ws.SoapFaultException;
import com.tgerm.tolerado.wsc.core.CoreUtil;

public abstract class WSErrorHandler {
	protected static final List<String> RETRYABLES = Arrays
			.asList(new String[] { "client_not_accessible_for_user",
					"client_require_update_for_user", "invalid_session_id",
					"query_timeout", "request_running_too_long",
					"server_unavailable", "sso_service_down",
					"unknown_exception" });

	/**
	 * Partner or Enterprise WSDL will give fine implementations as per
	 * exception
	 * 
	 * @param ex
	 * @return True if login is required
	 */
	protected abstract boolean isLoginExpiredForWSDL(Exception ex);

	/**
	 * Partner or Enterprise WSDL will give fine implementations to tell if they
	 * can retry the exception
	 * 
	 * @param ex
	 * @return
	 */
	protected abstract boolean canRetryForWSDL(Exception ex);

	/**
	 * Checks if their is an Invalid Session Issue.
	 * 
	 * @param ex
	 *            Exception to check against
	 * @return
	 */
	public boolean isLoginExpired(Exception ex) {
		if (ex instanceof SoapFaultException) {
			SoapFaultException af = (SoapFaultException) ex;
			String exCode = CoreUtil.faultCodeFromSoapException(af);
			if (exCode != null) {
				return exCode.equalsIgnoreCase("INVALID_SESSION_ID");
			}
		}

		return isLoginExpiredForWSDL(ex);
	}

	public boolean canRetry(Exception t) {
		if (t instanceof SoapFaultException) {
			SoapFaultException af = (SoapFaultException) t;
			if (af.getCause() != null
					&& af.getCause() instanceof java.net.SocketException) {
				return true;
			}

			String exCode = CoreUtil.faultCodeFromSoapException(af);
			if (exCode != null) {
				return RETRYABLES.contains(exCode.toLowerCase());
			}

			// Check if WSDL implementation can retry the exception
			if (canRetryForWSDL(t)) {
				return true;
			}

			String faultString = CoreUtil.printStackTrace(t);
			if (faultString != null
					&& faultString.indexOf("UnknownHostException") != -1) {
				return true;
			}
		}
		return false;
	}
}
