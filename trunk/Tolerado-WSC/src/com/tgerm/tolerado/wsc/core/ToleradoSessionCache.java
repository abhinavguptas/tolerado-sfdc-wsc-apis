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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tgerm.tolerado.wsc.core.LoginDriver.Type;

/**
 * @author abhinav
 * 
 */
public class ToleradoSessionCache {
	private static Log log = LogFactory.getLog(ToleradoSessionCache.class);

	/**
	 * CacheKey to keep the enterprise and partner login's separate. See this
	 * Issue, for more details :
	 * http://code.google.com/p/tolerado-sfdc-wsc-apis/issues/detail?id=2
	 * 
	 * @author abhinav
	 */
	private static class CacheKey {
		private final LoginDriver.Type driverType;
		private final Credential credential;

		public CacheKey(Type driverType, Credential credential) {
			super();
			this.driverType = driverType;
			this.credential = credential;
		}

		public LoginDriver.Type getDriverType() {
			return driverType;
		}

		public Credential getCredential() {
			return credential;
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

	private static Map<CacheKey, ToleradoSession> cache = new HashMap<CacheKey, ToleradoSession>();

	public static ToleradoSession sessionFor(LoginDriver loginDriver,
			Credential credential) {
		// Try using cache by default
		boolean forceLogin = false;
		return sessionFor(loginDriver, credential, forceLogin);
	}

	public static ToleradoSession sessionFor(LoginDriver loginDriver,
			Credential credential, boolean forceLogin) {
		if (forceLogin) {
			return renewSession(credential, loginDriver);
		} else {
			return sessionFromCache(credential, loginDriver);
		}
	}

	public static void clearCache() {
		cache.clear();
	}

	private static ToleradoSession sessionFromCache(Credential cred,
			LoginDriver loginDriver) {
		ToleradoSession stub = cache.get(new CacheKey(loginDriver.getType(),
				cred));
		if (stub == null) {
			synchronized (ToleradoSessionCache.class) {
				if (stub == null) {
					stub = renewSession(cred, loginDriver);
				}
			}
		}
		return stub;
	}

	private static ToleradoSession renewSession(Credential cred,
			LoginDriver loginDriver) {
		log.debug("Login Call for " + cred.getUserName());
		ToleradoSession session = loginDriver.login(cred);
		cache.put(new CacheKey(loginDriver.getType(), cred), session);
		return session;
	}

}
