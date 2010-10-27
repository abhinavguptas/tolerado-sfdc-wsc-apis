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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoginDriverLocator {
	private static final String CLS_PARTNER_DRIVER = "com.tgerm.tolerado.wsc.partner.PartnerLoginDriver";
	private static final String CLS_ENTERPRISE_DRIVER = "com.tgerm.tolerado.wsc.enterprise.EnterpriseLoginDriver";

	/**
	 * This map will give a performance boost by caching the loaded classes for
	 * once
	 */
	private static final Map<String, Class<?>> WSDL_CONNECTOR_CLASSES = new HashMap<String, Class<?>>();
	static {
		// This block will load and cache for once the classes for WSDL
		// connectors.
		Class<?> connectorClass;
		try {
			connectorClass = Class.forName(CLS_PARTNER_DRIVER);
			WSDL_CONNECTOR_CLASSES.put(CLS_PARTNER_DRIVER, connectorClass);
		} catch (Exception e) {
			// do nothing, as we are just trying to load the classes
		}
		try {
			connectorClass = Class.forName(CLS_ENTERPRISE_DRIVER);
			WSDL_CONNECTOR_CLASSES.put(CLS_ENTERPRISE_DRIVER, connectorClass);
		} catch (Exception e) {
			// do nothing, as we are just trying to load the classes
		}
	}

	public static LoginDriver locate() {
		Collection<Class<?>> drivers = WSDL_CONNECTOR_CLASSES.values();
		if (drivers == null || drivers.isEmpty()) {
			throw new ToleradoException(
					"Neither Partner nor Enterprise WSDL2Java classes found in classpath. So can't proceed with login");
		}

		try {
			return (LoginDriver) drivers.iterator().next().newInstance();
		} catch (Exception e) {
			throw new ToleradoException("Failed to instantiate Login Driver", e);
		}
	}

	public static void main(String[] args) {
		LoginDriver locateDriver = locate();
		System.out.println(locateDriver);
	}
}
