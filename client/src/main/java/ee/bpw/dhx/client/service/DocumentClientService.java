package ee.bpw.dhx.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadClient;
import ee.bpw.dhx.ws.service.DocumentService;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;


@Service
@Slf4j
public class DocumentClientService extends DocumentService{

	
	//get log4j logger to log events on custom level.
	final Logger logger = LogManager.getLogger();
	
	private static List<DhxDocument> receevedDocuments = new ArrayList<DhxDocument>();
	
	@Override
	public DhxDocument extractAndValidateDocument(SendDocument document) throws DhxException {
		try{
			logger.log(Level.getLevel("EVENT"), "Starting to receive document. for representative: "
					+ document.getRecipient());
			return super.extractAndValidateDocument(document);
		}catch(DhxException ex) {
			log.error(ex.getMessage(), ex);
			logger.log(Level.getLevel("EVENT"), "Document is not recieved. code:" + ex.getExceptionCode() + " message:" + ex.getMessage());
			throw ex;
		}
	}
	
	@Override
	public String recieveDocument (DhxDocument dhxDocument) throws DhxException{
		//try {
			String receiptId = UUID.randomUUID().toString();
			logger.log(Level.getLevel("EVENT"), "Document recieved. for representative: "
					+ dhxDocument.getRecipient() +" receipt:" + receiptId);
			if(dhxDocument.getContainer()!=null){
				logger.log(Level.getLevel("EVENT"), "Document data from capsule: recipient organisationCode:" + dhxDocument.getContainer().getTransport().getDecRecipient().get(0).getOrganisationCode()
					+ " sender organisationCode:" + dhxDocument.getContainer().getTransport().getDecSender().getOrganisationCode());
			}
			receevedDocuments.add(dhxDocument);
			return receiptId;
		/*}catch(DhxException ex) {
			log.error(ex.getMessage(), ex);
			logger.log(Level.getLevel("EVENT"), "Document is not recieved. code:" + ex.getExceptionCode() + " message:" + ex.getMessage());
			throw ex;
		}*/
	}
	
	@Override
	public SendDocumentResponse sendDocument(DhxDocument document) throws DhxException{
			if(document.getInternalConsignmentId() == null) {
				String consignmentId = UUID.randomUUID().toString();
				document.setInternalConsignmentId(consignmentId);
			}
			logger.log(Level.getLevel("EVENT"), "Sending document to recipient:" + document.getRecipient() + " internalConsignmentId:" + document.getInternalConsignmentId());
			SendDocumentResponse response = null;
			try{
				log.info("Sending document for " + document.getRecipient());
				response = super.sendDocument(document);
				log.info("Sending document done");
				logger.log(Level.getLevel("EVENT"), "Document sent to recipient:" + document.getRecipient() + " ReceiptId:" + response.getReceiptId()
						+ (response.getFault()==null?"":" faultCode:" + response.getFault().getFaultCode() + " faultString:" + response.getFault().getFaultString()));
			} catch(DhxException e) {
				log.error("Error occured while sending document. recipient:" + document.getRecipient() + ". " + e.getMessage(), e);
				logger.log(Level.getLevel("EVENT"),"Error occured while sending document. recipient:" + document.getRecipient() + ". " + e.getMessage());
				throw e;
			}
		return response;

	}
	
	@Override
	public boolean isDuplicatePackage (XroadClient from, String consignmentId){
		log.debug("Checking for duplicates. from memberCode:" + from.toString() + " from consignmentId:" + consignmentId);
		for(DhxDocument document : receevedDocuments) {
			if(document.getExternalConsignmentId().equals(consignmentId) && document.getClient().toString().equals(from.toString())) {
				return true;
			}
		}
		return false;
	}
	
}
