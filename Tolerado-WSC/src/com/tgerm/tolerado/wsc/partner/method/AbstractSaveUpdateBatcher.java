package com.tgerm.tolerado.wsc.partner.method;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.SaveResult;
import com.tgerm.tolerado.wsc.partner.PartnerUtil;

public abstract class AbstractSaveUpdateBatcher extends
		AbstractPersistanceBatcher<SaveResult> {

	public AbstractSaveUpdateBatcher(String methodName,
			SObject[] objectsToUpdate, boolean throwErrorOnSaveFailure) {
		super(methodName, objectsToUpdate, throwErrorOnSaveFailure);
	}

	@Override
	protected void assertBatchSuccess(SaveResult[] results) {
		PartnerUtil.checkSuccess(results);
	}

	@Override
	protected SaveResult[] newResultTypeArray(int length) {
		return new SaveResult[length];
	}

}
