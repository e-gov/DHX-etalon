package ee.bpw.dhx.ws.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.Unmarshaller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.context.MessageContext;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

/**
 * Generic class for document sending and receiving. 
 * Does not parse capsule neither validates capsule, because does not contain information about container. 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public abstract class DocumentServiceImpl implements DocumentService{

	@Autowired
	private DhxConfig config;
	
	@Autowired
	private SoapConfig soapConfig;
	
	@Autowired
	RepresentationService representationService;
	
	@Autowired
	Unmarshaller unmarshaller;
	
	@Autowired 
	Jaxb2Marshaller jaxMarshaller;
	
	@Autowired
	AddressServiceImpl addressService;
	
	@Autowired
	DhxGateway documentGateway;
	
	
	/**
	 * Method should receive document(save in database for example) and return unique id of it. Id will be sent as receipt in response. 
	 * @param document - document to receive
	 * @return - unique id of the document that was saved. 
	 * @throws DhxException
	 */
	public abstract String recieveDocument (DhxDocument document) throws DhxException;
		
	/**
	 * Method should send document to all recipients defined in capsule. Should be implemented in service which has information about version of the capsule
	 * @param capsuleFile
	 * @param consignmentId
	 * @return
	 * @throws DhxException
	 */
	public abstract List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId) throws DhxException;
	
	/**
	 * Method searches through saved documents and checks if document with save sender and consignment id exists. 
	 * @param from - document sender to check
	 * @param consignmentId - consignment id to check
	 * @return - true if document with same sender and consignment id exists, otherwise false
	 */
	abstract public boolean isDuplicatePackage (XroadMember from, String consignmentId) ;
	
	
	/**
	 * Method is used by endpoint. Is called when document arrives to endpoint.
	 * @param document
	 * @param messageContext
	 * @return
	 * @throws DhxException
	 */
	public SendDocumentResponse receiveDocumentFromEndpoint (SendDocument document, XroadMember client) throws DhxException{
		//XroadMember client = documentGateway.getXroadCLientAndSetRersponseHeader(messageContext);
		
		if (isDuplicatePackage(client, document.getConsignmentId())) {
			throw new DhxException(DHXExceptionEnum.DUPLICATE_PACKAGE, "Already got package with this consignmentID. from:" + client.toString() + " consignmentId:" + document.getConsignmentId());
		} else {
			if(document.getRecipient() != null) {
				XroadMember member = addressService.getClientForMemberCode(document.getRecipient());
				client.setRepresentee(member.getRepresentee());
			}
			DhxDocument dhxDocument = extractAndValidateDocument(document, client);
			dhxDocument.setClient(client);
			String id = recieveDocument(dhxDocument);
			SendDocumentResponse response = new SendDocumentResponse();
			response.setReceiptId(id);
			return response;
		}
	}

	
	/**
	 * Method extracts and validates attached document. Attachment validation is not implemented in this version of service.
	 * @param document - SOAP request object
	 * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if validation is enabled
	 * @throws DhxException
	 */
	protected DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client) throws DhxException {
		try{
			log.info("Recieving document. for representative: "
					+ document.getRecipient());
			DhxDocument dhxDocument = new DhxDocument(client, document);
			//File unpacked = FileUtil.extractAndUnpackAttachment(dhxDocument.getDocumentFile());
			if(config.getCapsuleValidate()) {
				throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Capsule validation is not implemented. Use service precific for container version");

			} else {
				log.debug("Validating capsule is disabled");
			}
			checkRecipient(document.getRecipient(), null);
			log.info( "Recipient checked and found in representative list or own member code. recipient:" + document.getRecipient());	
			log.info( "Document recieved.");
			return dhxDocument;
		} catch(DhxException ex) {
			log.error(ex.getMessage(), ex);
			log.info( "Document is not recieved. code:" + ex.getExceptionCode() + " message:" + ex.getMessage());
			throw ex;
		}
	}
	
	
	/**
	 * Implementation is done in service for specific container version. To define adresssees, container needs to be opened.
	 * @param capsuleStream
	 * @param consignmentId
	 * @return
	 * @throws DhxException
	 * @Deprecated - does not support file size checking!
	 */
	//public abstract List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId) throws DhxException;
	
	/*public SendDocumentResponse sendDocument(InputStream capsuleStream, String consignmentId, String recipient) throws DhxException{
		XroadMember adressee = addressService.getClientForMemberCode(recipient);
		DhxDocument document = new DhxDocument(adressee, capsuleStream, true);
		return documentGateway.sendDocument(document);
	}*/
		
	

	/**
	 * Checks if recipient is present in representativesList and in capsule recipients 
	 * @param recipient
	 * @throws DhxException
	 */
	protected void checkRecipient (String recipient, List<String> capsuleRecipients) throws DhxException{
		if (recipient == null) {
			recipient = soapConfig.getMemberCode();
		}
		List<String> recipientList = new ArrayList<String>();
		List<Representee> representees = representationService.getRepresentationList();
		Date curDate = new Date();
		if(representees != null && representees.size()>0) {
			for(Representee representee : representees) {
				if(representee.getStartDate().getTime()<=curDate.getTime()
						&&(representee.getEndDate() == null || representee.getEndDate().getTime()>=curDate.getTime())) {
					recipientList.add(representee.getMemberCode());
				}
			}
		}
		recipientList.add(soapConfig.getMemberCode());
		if(!recipientList.contains(recipient)) {
			throw new DhxException(DHXExceptionEnum.WRONG_RECIPIENT, "Recipient not found in representativesList and own member code. recipient:" + recipient);
		}
		if(capsuleRecipients != null) {
			for(String capsuleRecipient : capsuleRecipients) {
				if(capsuleRecipient.equals(recipient)) {
					return;
				}
			}
			throw new DhxException(DHXExceptionEnum.WRONG_RECIPIENT, "Recipient not found in capsule recipient list. recipient:" + recipient);
		} 
		return;
	}
	

	protected void checkFileSize (File fileToCheck) throws DhxException{
		Integer maxSize = config.getMaxFileSizeInBytes();
		log.debug("Max file size:" + maxSize + " filesize:" + fileToCheck.length());
		if(maxSize<fileToCheck.length()) {
			throw new DhxException(DHXExceptionEnum.OVER_MAX_SIZE, "File size is too big.  Max file size:" + config.getMaxFileSize());
		}
		return;
	}
}
