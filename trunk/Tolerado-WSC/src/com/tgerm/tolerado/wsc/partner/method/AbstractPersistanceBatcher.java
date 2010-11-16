package com.tgerm.tolerado.wsc.partner.method;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.tgerm.tolerado.wsc.core.method.WSRecoverableMethod;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;

public abstract class AbstractPersistanceBatcher<R> extends
		WSRecoverableMethod<R[], ToleradoPartnerStub> {
	protected SObject[] records;
	protected boolean throwErrorOnSaveFailure;
	protected int batchSize;

	public AbstractPersistanceBatcher(String methodName, SObject[] objectsToUpdate,
			boolean throwErrorOnSaveFailure, int batchSize) {
		super(methodName);
		this.records = objectsToUpdate;
		this.throwErrorOnSaveFailure = throwErrorOnSaveFailure;
		this.batchSize = batchSize;
	}

	public AbstractPersistanceBatcher(String methodName, SObject[] objectsToUpdate,
			boolean throwErrorOnSaveFailure) {
		this(methodName, objectsToUpdate, throwErrorOnSaveFailure, 200);
	}

	@Override
	protected R[] invokeActual(ToleradoPartnerStub stub) throws Exception {
		R[] batch = batch(stub, records, batchSize);
		if (throwErrorOnSaveFailure)
			assertBatchSuccess(batch);
		return batch;
	}

	private R[] batch(ToleradoPartnerStub stub, SObject[] records,
			int batchSize) throws Exception {
		if (records.length <= batchSize) {
			return persistChunk(stub, records);
		}

		R[] saveResults = newResultTypeArray(records.length);
		SObject[] thisBatch = null;
		int pos = 0;
		while (pos < records.length) {
			int thisBatchSize = Math.min(batchSize, records.length - pos);
			if (thisBatch == null || thisBatch.length != thisBatchSize)
				thisBatch = new SObject[thisBatchSize];
			System.arraycopy(records, pos, thisBatch, 0, thisBatchSize);
			R[] batchResults = batch(stub, thisBatch, thisBatchSize);
			System.arraycopy(batchResults, 0, saveResults, pos, thisBatchSize);
			pos += thisBatchSize;
		}
		return saveResults;
	}

	/**
	 * To be implemented by child classes
	 * 
	 * @param stub
	 * @param sobjs
	 * @return
	 * @throws Exception
	 */
	protected abstract R[] persistChunk(ToleradoPartnerStub stub,
			SObject[] sobjs) throws Exception;

	protected abstract void assertBatchSuccess(R[] results)
			throws Exception;

	protected abstract R[] newResultTypeArray(int length);

}
