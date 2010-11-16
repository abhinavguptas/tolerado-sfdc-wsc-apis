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
public class UpdateSobjectsMethod extends AbstractSaveUpdateBatcher {
	public UpdateSobjectsMethod(SObject[] objectsToUpdate,
			boolean throwErrorOnSaveFailure) {
		super("update", objectsToUpdate, throwErrorOnSaveFailure);
	}

	public UpdateSobjectsMethod(SObject[] sObjectsToUpdate) {
		this(sObjectsToUpdate, false);
	}

	@Override
	protected SaveResult[] persistChunk(ToleradoPartnerStub stub, SObject[] sobjs)
			throws Exception {
		return stub.getPartnerBinding().update(sobjs);
	}
}
