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

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.QueryResult;

/**
 * SFDC Web service Query Helper class. When querying with Salesforce partner
 * binding you can directly fetch only 500 records. To fetch more then 500 one
 * needs to use queryMore() API, this API requires setting some headers and
 * maintaining a cursor on client. This class simplifies and exposes and easy to
 * use interface to queryMore() API, so it does all the housekeeping for
 * following
 * <ol>
 * <li>Updating Partner binding {@link SoapBindingStub} to have correct headers
 * for batch queries</li>
 * <li>Calling query() and queryMore() at correct times</li>
 * <li>Maintaining client side cursor and using it the right way</li>
 * <li>Gives common Java style interface for querying the records</li>
 * </ol>
 * <p>
 * Sample code
 * </p>
 * 
 * <pre>
 * <code>
 * 		// Assuming soapBinding is created correctly already.
 * 		String soql = 'select Id, Name from Contact';
 * 		ToleradoQuery sf = new ToleradoQuery(soapBinding, soql);
 * 		while (sf.hasMoreRecords()) {
 * 			SObject[] records = sf.getRecords();
 * 			// ....
 * 			// process these records
 * 			// ...
 * 		}
 * </code>
 * </pre>
 * 
 * @author <a href="http://www.tgerm.com">abhinav</a>
 */
public class ToleradoQuery {
	/**
	 * Batch size defaulted to 200
	 */
	private static final int DEFAULT_BATCH_SIZE = 200;

	private ToleradoPartnerStub binding;
	private String soql;
	private QueryResult queryResult;

	/**
	 * Use this constructor for querying with default batch size i.e. 200
	 * 
	 * @param binding
	 *            The partner binding, it should be read with required session
	 *            headers set in place.
	 * @param soql
	 *            SOQL query string
	 */
	public ToleradoQuery(ToleradoPartnerStub binding, String soql) {
		this(binding, soql, DEFAULT_BATCH_SIZE);
	}

	/**
	 * You can use this constructor to override the default batch size
	 * 
	 * @param pStub
	 *            The partner binding, it should be read with required session
	 *            headers set in place.
	 * @param soql
	 *            SOQL query string
	 * @param batchSize
	 *            the new batch size for querying
	 */
	public ToleradoQuery(ToleradoPartnerStub pStub, String soql, int batchSize) {
		super();
		// Query can't fetch more then 500 records, so batch size should be less
		// than that.
		if (batchSize > 500)
			throw new IllegalArgumentException(
					"BatchSize can't be more then 500 ");

		this.binding = pStub;
		this.soql = soql;
		pStub.getPartnerBinding().setQueryOptions(batchSize);
	}

	/**
	 * This method is meant to be called inside for/while loop, for fetching
	 * next lot/batch of records
	 * 
	 * @return Next batch of Sobjects returned by Query
	 */
	public SObject[] getRecords() throws Exception {
		if (queryResult == null) {
			// Here means we need to do a plain query
			// and get the cursor for later use
			queryResult = binding.query(soql);
		} else if (!queryResult.isDone()) {
			// if there are more results then use
			// queryMore() to fetch the rest.
			queryResult = binding.queryMore(queryResult.getQueryLocator());
		}
		// Whatever is the way i.e. query() or queryMore() end result will be
		// Sobject[] so return those back
		return queryResult.getRecords();
	}

	/**
	 * This method should be used as expression inside for/while loops to
	 * determine if their are records to fetch
	 * 
	 * @return TRUE if there are more records available on fetch
	 */
	public boolean hasMoreRecords() {
		// One can get records until, nothing is queried or the cursor is not
		// done
		return queryResult == null || !queryResult.isDone();
	}

}
