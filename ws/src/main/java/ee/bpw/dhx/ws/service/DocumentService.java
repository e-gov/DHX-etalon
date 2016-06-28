package ee.bpw.dhx.ws.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;

/**
 * Generic class for document sending and receiving. 
 * Does not parse capsule neither validates capsule, because does not contain information about container. 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public abstract class DocumentService{

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
	AddressService addressService;
	
	@Autowired
	DhxGateway documentGateway;
	
	
	/**
	 * Function receives document and returns unique id of it. Id will be sent as receipt in response
	 * @param document - document to receive
	 * @return - unique id of the document that was saved. 
	 * @throws DhxException
	 */
	public abstract String recieveDocument (DhxDocument document) throws DhxException;
	
	
	public SendDocumentResponse receiveDocumentFromEndpoint (SendDocument document, MessageContext messageContext) throws DhxException{
		XroadMember client = documentGateway.getXroadCLientAndSetRersponseHeader(messageContext);
		
		if (isDuplicatePackage(client, document.getConsignmentId())) {
			throw new DhxException(DHXExceptionEnum.DUPLICATE_PACKAGE, "Already got package with this consignmentID. from:" + client.toString() + " consignmentId:" + document.getConsignmentId());
		} else {
			client.setRepresentativeCode(document.getRecipient());
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
	public DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client) throws DhxException {
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
	
	public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId) throws DhxException{
		return sendDocument(FileUtil.getFileAsStream(capsuleFile), consignmentId);
	}
	
	/**
	 * Implementation is done in service for specific container version. To define adresssees, container needs to be opened.
	 * @param capsuleStream
	 * @param consignmentId
	 * @return
	 * @throws DhxException
	 */
	public abstract List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId) throws DhxException;
	
	public SendDocumentResponse sendDocument(InputStream capsuleStream, String consignmentId, String recipient) throws DhxException{
		XroadMember adressee = addressService.getClientForMemberCode(recipient);
		DhxDocument document = new DhxDocument(adressee, capsuleStream, true);
		return documentGateway.sendDocument(document);
	}
		
	

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
		recipientList.addAll(representationService.getRepresentationList());
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
		}
		throw new DhxException(DHXExceptionEnum.WRONG_RECIPIENT, "Recipient not found in capsule recipient list. recipient:" + recipient);
	}
	
	abstract public boolean isDuplicatePackage (XroadMember from, String consignmentId);
}
