/**
 * 
 */
package com.tgerm.tolerado.wsc.partner.method;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.SaveResult;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;

/**
 * @author abhinav
 * 
 */
public class CreateSobjectsMethod extends AbstractSaveUpdateBatcher {
	public CreateSobjectsMethod(SObject[] objectsToSave,
			boolean throwErrorOnSaveFailure) {
		super("create", objectsToSave, throwErrorOnSaveFailure);
	}

	public CreateSobjectsMethod(SObject[] sObjectsToSave) {
		this(sObjectsToSave, false);
	}

	@Override
	protected SaveResult[] persistChunk(ToleradoPartnerStub stub,
			SObject[] sobjs) throws Exception {
		return stub.getPartnerBinding().create(sobjs);
	}

}
