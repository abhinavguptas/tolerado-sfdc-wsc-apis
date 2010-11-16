/**
 * 
 */
package com.tgerm.tolerado.wsc.partner.method;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.UpsertResult;
import com.tgerm.tolerado.wsc.partner.PartnerUtil;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;

/**
 * @author abhinav
 * 
 */
public class UpsertSobjectsMethod extends
		AbstractPersistanceBatcher<UpsertResult> {
	private String externalIDFieldName;

	public UpsertSobjectsMethod(String externalIDFieldName,
			SObject[] objectsToUpsert, boolean throwErrorOnSaveFailure) {
		super("upsert", objectsToUpsert, throwErrorOnSaveFailure);
		this.externalIDFieldName = externalIDFieldName;
	}

	public UpsertSobjectsMethod(String externalIDFieldName,
			SObject[] sObjectsToUpdate) {
		this(externalIDFieldName, sObjectsToUpdate, false);
	}

	@Override
	protected UpsertResult[] persistChunk(ToleradoPartnerStub stub,
			SObject[] sobjs) throws Exception {
		UpsertResult[] results = stub.getPartnerBinding().upsert(
				externalIDFieldName, sobjs);
		return results;
	}

	@Override
	protected void assertBatchSuccess(UpsertResult[] results) {
		PartnerUtil.checkSuccess(results);
	}

	@Override
	protected UpsertResult[] newResultTypeArray(int length) {
		return new UpsertResult[length];
	}

}
