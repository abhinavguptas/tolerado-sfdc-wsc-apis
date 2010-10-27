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

import org.apache.commons.lang.ArrayUtils;

import com.sforce.soap.partner.wsc.DeleteResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.tgerm.tolerado.wsc.core.ToleradoException;

/**
 * Utility class for any common functions related to salesforce web service
 * calls
 * 
 * @author abhinav
 * 
 */
public class PartnerUtil {
	/**
	 * Checks if the {@link DeleteResult} in parameter contains any failure. If
	 * there is some failure {@link ToleradoException} is thrown to notify
	 * 
	 * @param delResult
	 *            {@link DeleteResult} to verify
	 */
	public static void checkSuccess(DeleteResult[] delResult) {
		if (!ArrayUtils.isEmpty(delResult)) {
			for (DeleteResult deleteResult : delResult) {
				if (deleteResult.isSuccess())
					continue;
				String errString = PartnerUtil.toErrorString(deleteResult
						.getErrors());
				throw new ToleradoException("Create/Update failed. Cause: "
						+ errString);
			}
		}
	}

	/**
	 * Checks if the {@link SaveResult} in parameter contains any failure. If
	 * there is some failure {@link ToleradoException} is thrown to notify
	 * 
	 * @param delResult
	 *            {@link SaveResult} to verify
	 */

	public static void checkSuccess(SaveResult[] createResult) {
		if (!ArrayUtils.isEmpty(createResult)) {
			for (SaveResult saveResult : createResult) {
				if (saveResult.isSuccess())
					continue;
				String errString = PartnerUtil.toErrorString(saveResult
						.getErrors());
				throw new ToleradoException("Create/Update failed. Cause: "
						+ errString);
			}
		}
	}

	/**
	 * Prepares an error string for all {@link Error} objects in the argument
	 * 
	 * @param errors
	 *            {@link Error} for which error string should be created
	 * @return Error string
	 */
	public static String toErrorString(
			com.sforce.soap.partner.wsc.Error[] errors) {
		if (ArrayUtils.isEmpty(errors))
			return null;

		StringBuilder buffer = new StringBuilder();
		for (com.sforce.soap.partner.wsc.Error error : errors) {
			String[] fields = error.getFields();
			buffer.append(System.getProperty("line.separator")).append(
					error.toString()).append(", Fields=").append(toCSV(fields))
					.append(", StatusCode=").append(error.getStatusCode())
					.append(", Message=").append(error.getMessage());
		}
		return buffer.toString();
	}

	/**
	 * Converts the string array into CSV
	 * 
	 * @param args
	 *            string array to be converted to CSV String
	 * @return CSV string
	 */
	public static String toCSV(String[] args) {
		String val = "";
		if (!ArrayUtils.isEmpty(args)) {
			for (String s : args) {
				val += s + ",";
			}
			val = val.substring(0, val.lastIndexOf(","));
		}
		return val;
	}

}
