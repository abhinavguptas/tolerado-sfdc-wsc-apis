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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tgerm.tolerado.wsc.core.ToleradoException;
import com.tgerm.tolerado.wsc.core.ToleradoStub;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;

/**
 * Instances of this class represents a single web service method. Those methods
 * can be from any WSDL like partner, apex, metadata or enterprise. An example
 * method can be "queryMore()" from partner wsdl.
 * 
 * This class gives a recoverable fixture to a web service method call, with
 * transparent healing by retrying a number of times for known and retryable
 * issues.
 * 
 * Child implementations can just extend this class and override the
 * {@link WSRecoverableMethod#invokeActual(ToleradoPartnerStub)} method to do
 * the real web service method call via the correct Soap Stub. For ex.
 * 
 * <pre>
 * 
 * Credential cred = new Credential(&quot;&lt;your login name&gt;&quot;, &quot;&lt;your pass&gt;&quot;);
 * ToleradoApexStub apexStub = ToleradoStubRegistry.getApexStub(cred);
 * 
 * RunTestsResult results = new WSRecoverableMethod&lt;RunTestsResult, ToleradoApexStub&gt;(
 * 		&quot;runTests&quot;) {
 * 
 * 	&#064;Override
 * 	protected RunTestsResult invokeActual(ToleradoApexStub stub)
 * 			throws Exception {
 * 		// The real web service call done here using the Apex Binding 
 * 		// available in stub
 * 		return stub.getApexBinding().runTests(runTestsRequest);
 * 	}
 * }.invoke(apexStub);
 * 
 * </pre>
 * 
 * 
 * @author abhinav
 * 
 */
public abstract class WSRecoverableMethod<Result, Stub extends ToleradoStub> {
	// this.getClass() used for accurate logging
	private Log log = LogFactory.getLog(this.getClass());
	// how many retries are done
	protected int retries;

	private String methodName;

	private Stub toleradoStub;

	public WSRecoverableMethod(String methodName) {
		super();
		this.methodName = methodName;

	}

	/**
	 * Method exposed to public client API
	 * 
	 * @param stub
	 *            The stub to be used internally by this call for accessing web
	 *            services.
	 * @return
	 * @throws ToleradoException
	 */
	public Result invoke(Stub stub) {
		// Update the instance level attribute
		toleradoStub = stub;

		retries = 0;
		while (true) {
			try {
				return invokeActual(stub);
			} catch (Exception exc) {
				if (retries >= getMaxRetries()) {
					throw new ToleradoException(
							"All retry attempts failed to execute "
									+ getMethodInfo(), exc);
				}
				if (canRetry(exc)) {
					// Give a Retry
					retries++;
					log.warn("Retrying " + retries + " time, remote method : "
							+ getMethodInfo() + ", retrycount: " + retries
							+ ", ExceptionMsg: " + exc.getMessage());

					// If Session is invalid, re login otherwise sleep.
					if (isLoginExpired(exc))
						// if login is required re-login other wise sleep
						reLogin(stub);
					else {
						waitBeforeNextRetry();
					}
				} else {
					// All retries exhausted, fail now
					throw new ToleradoException(
							"Unknown Exception occured for " + getMethodInfo(),
							exc);
				}
			}
		}

	}

	protected WSErrorHandler getErrorHandler() {
		return toleradoStub.getSession().getErrorHandler();
	}

	/**
	 * 
	 * @return Returns some human readable information about this webservice
	 *         call impl
	 */
	protected String getMethodInfo() {
		return methodName;
	}

	/**
	 * This method will do the real call, meant to be retried by invoke(S)
	 * 
	 * @param stub
	 *            Stub to be used for any operation
	 * @return
	 * @throws Exception
	 */
	protected abstract Result invokeActual(Stub stub) throws Exception;

	/**
	 * Will be called on Retryable failure to give a pause and retry. Child
	 * implemenations may override this if required any change to this behavior
	 */
	protected void waitBeforeNextRetry() {
		try {
			int delta = 1 + retries;
			Thread.sleep(3000 * delta);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Returns true if the Exception is coming because of Login expiration.
	 * 
	 * @param ex
	 *            Exception thrown by Salesforce for the failure
	 * @return
	 */
	protected boolean isLoginExpired(Exception t) {
		WSErrorHandler errorHandler = getErrorHandler();
		return errorHandler.isLoginExpired(t);
	}

	/**
	 * Renews the user's session, if login was previously expired
	 * 
	 * @param stub
	 *            The {@link ToleradoPartnerStub} instance
	 */
	protected void reLogin(Stub stub) {
		if (stub != null) {
			log.warn("Preparing New SFDC Session, by forcing a login call");
			stub.prepare(true);
		}
	}

	/**
	 * Child implementations can override if more retries are needed. It
	 * defaults to 5.
	 * 
	 * @return Returns maximum retries that should be made
	 */
	protected int getMaxRetries() {
		return 5;
	}

	/**
	 * Returns true if this Exception can be retried
	 * 
	 * @param ex
	 *            Exception thrown by Salesforce
	 * @return
	 */
	protected boolean canRetry(Exception t) {
		WSErrorHandler errorHandler = getErrorHandler();
		return errorHandler.canRetry(t);
	}

}
