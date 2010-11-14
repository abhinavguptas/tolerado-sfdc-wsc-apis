package com.tgerm.tolerado.wsc.tests.partner.sobject;

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

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.DeleteResult;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.sforce.ws.bind.XmlObject;
import com.tgerm.tolerado.samples.cfg.LoginCfg;
import com.tgerm.tolerado.wsc.core.Credential;
import com.tgerm.tolerado.wsc.core.ToleradoException;
import com.tgerm.tolerado.wsc.core.method.WSRecoverableMethod;
import com.tgerm.tolerado.wsc.partner.PartnerUtil;
import com.tgerm.tolerado.wsc.partner.ToleradoPartnerStub;
import com.tgerm.tolerado.wsc.partner.ToleradoSobject;

/**
 * Shows how to use {@link ToleradoSobject} for CRUD operations
 * 
 * @author abhinav
 * 
 */
public class SobjectUsageTest extends TestCase {
	private static final String NOTE_1_TITLE = "Sample Note Title 1";
	private static final String NOTE_1_BODY = "Sample Note Body 1";

	private static final String NOTE_2_TITLE = "Sample Note Title 2";
	private static final String NOTE_2_BODY = "Sample Note Body 2";

	// Logger from - Apache commons logging
	private static Log log = LogFactory.getLog(SobjectUsageTest.class);

	public void testSObject() {
		// Get the partner stub
		Credential credential = LoginCfg.self.getCredential();
		ToleradoPartnerStub partnerStub = new ToleradoPartnerStub(credential);
		//
		// Create new contact
		//
		ToleradoSobject contact = new ToleradoSobject("Contact");
		contact.setField("FirstName", "Abhinav");
		contact.setField("LastName", "Gupta");
		log.debug("Created ToleradoSobject: " + contact);
		// Get the updated Sobejct
		SObject[] sObjects = new SObject[] { contact.getUpdatedSObject() };
		SaveResult[] saveResults = partnerStub.create(sObjects);
		// Throws Error in case Save is failed
		PartnerUtil.checkSuccess(saveResults);
		final String createdContactId = saveResults[0].getId();
		log.debug("Created Contact [" + createdContactId + "] successfully !");

		// Create a few notes on this Contact
		ToleradoSobject note1 = createNote(createdContactId, NOTE_1_BODY,
				NOTE_1_TITLE);
		ToleradoSobject note2 = createNote(createdContactId, NOTE_2_BODY,
				NOTE_2_TITLE);
		SObject[] notes = new SObject[] { note1.getUpdatedSObject(),
				note2.getUpdatedSObject() };
		SaveResult[] noteCreateResults = partnerStub.create(notes);
		PartnerUtil.checkSuccess(noteCreateResults);

		// Create SOQL
		/**
		 * SOQL for loading contact with some fields, lookup and child
		 * relationship
		 */
		String SOQL = "Select Id, FirstName, LastName, Name, CreatedBy.Username, "
				+ "(Select Id, Title, Body From Notes) From Contact where Id = '"
				+ createdContactId + "'";
		QueryResult qr = partnerStub.query(SOQL);
		if (ArrayUtils.isEmpty(qr.getRecords())) {
			throw new ToleradoException("Failed to query the desired Sobject");
		}

		SObject sobj = qr.getRecords()[0];
		ToleradoSobject tSobj = new ToleradoSobject(sobj);
		System.out.println("Queried Contact FirstName:"
				+ tSobj.getTextValue("FirstName"));
		System.out.println("Queried Contact LastName:"
				+ tSobj.getTextValue("LastName"));

		Iterator<XmlObject> relatedList = tSobj.getRelatedList("Notes");
		Assert.assertNotNull(relatedList);
		// Test Note1
		XmlObject loadedNote = relatedList.next();
		Object loadedNoteBody = loadedNote.getField("Body");
		Object loadedNoteTitle = loadedNote.getField("Title");
		Assert.assertTrue(loadedNoteBody.equals(NOTE_1_BODY)
				|| loadedNoteBody.equals(NOTE_2_BODY));
		Assert.assertTrue(loadedNoteTitle.equals(NOTE_1_TITLE)
				|| loadedNoteTitle.equals(NOTE_2_TITLE));
		// Test Note2
		loadedNote = relatedList.next();
		loadedNoteBody = loadedNote.getField("Body");
		loadedNoteTitle = loadedNote.getField("Title");
		Assert.assertTrue(loadedNoteBody.equals(NOTE_1_BODY)
				|| loadedNoteBody.equals(NOTE_2_BODY));
		Assert.assertTrue(loadedNoteTitle.equals(NOTE_1_TITLE)
				|| loadedNoteTitle.equals(NOTE_2_TITLE));

		Assert.assertEquals(tSobj.getNestedValue("CreatedBy", "Username"),
				credential.getUserName());

		tSobj.setField("FirstName", "Abhinav2");
		SObject updatedSObject = tSobj.getUpdatedSObject();

		sObjects = new SObject[] { updatedSObject };
		saveResults = partnerStub.update(sObjects);
		// Throws Error in case Save is failed
		PartnerUtil.checkSuccess(saveResults);
		log.debug("Updated Contact successfully !");

		WSRecoverableMethod<DeleteResult[], ToleradoPartnerStub> deleteMethod = new WSRecoverableMethod<DeleteResult[], ToleradoPartnerStub>(
				"delete") {
			@Override
			protected DeleteResult[] invokeActual(ToleradoPartnerStub stub)
					throws Exception {
				final String[] idsToDelete = new String[] { createdContactId };
				return stub.getPartnerBinding().delete(idsToDelete);
			}
		};

		DeleteResult[] results = deleteMethod.invoke(partnerStub);
		PartnerUtil.checkSuccess(results);
	}

	private static ToleradoSobject createNote(String cId, String noteBody,
			String noteTitle) {
		ToleradoSobject note = new ToleradoSobject("Note");
		note.setField("Body", noteBody);
		note.setField("Title", noteTitle);
		note.setField("ParentId", cId);
		return note;
	}

}
