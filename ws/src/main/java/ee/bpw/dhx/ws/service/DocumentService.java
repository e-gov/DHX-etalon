package ee.bpw.dhx.ws.service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;
import eu.x_road.xsd.identifiers.ObjectFactory;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;

@Service
@Slf4j
public abstract class DocumentService extends WebServiceGatewaySupport {

	@Autowired
	private DhxConfig config;
	
	@Autowired
	private SoapConfig soapConfig;
	
	@Autowired
	RepresentationService representationService;
	
	@Autowired
	Unmarshaller unmarshaller;
	
	@Autowired 
	Jaxb2Marshaller marshaller;
	
	
	private class SoapRequestHeaderModifier implements WebServiceMessageCallback{

		
		private String recipient;
		
		public SoapRequestHeaderModifier(String recipient){
			super();
			this.recipient = recipient;
		}
		
		@Override
		public void doWithMessage(WebServiceMessage message) throws IOException,
				TransformerException {
			 try {
			SoapHeader header = ((SoapMessage)message).getSoapHeader();
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        Marshaller marshallerHeader = marshaller.getJaxbContext().createMarshaller();
        	marshallerHeader.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        	eu.x_road.xsd.xroad.ObjectFactory factory = new eu.x_road.xsd.xroad.ObjectFactory();
	        transformer.transform(marshallObject(factory.createProtocolVersion(soapConfig.getProtocolVersion()), marshallerHeader), header.getResult());
	        transformer.transform(marshallObject(factory.createId(UUID.randomUUID().toString()), marshallerHeader), header.getResult());
	        transformer.transform(marshallObject(factory.createClient(getXRoadClientIdentifierType()), marshallerHeader), header.getResult());
	        transformer.transform(marshallObject(factory.createService(getXRoadServiceIdentifierType()), marshallerHeader), header.getResult());
	        } catch(JAXBException ex){
	        	throw new RuntimeException(ex);
	        }
		}

		private StringSource marshallObject(Object obejct, Marshaller objectMarshaller) {
			String result = "";
			StringWriter sw = new StringWriter();
	        try {
	        	objectMarshaller.marshal(obejct, sw);
	            result = sw.toString();
	        } catch (JAXBException e) {
	            throw new RuntimeException(e);
	        }
			return new StringSource(result);
		}
		
		private XRoadClientIdentifierType getXRoadClientIdentifierType () {
			ObjectFactory factory = new ObjectFactory();
			XRoadClientIdentifierType client = factory.createXRoadClientIdentifierType();
			client.setXRoadInstance(soapConfig.getXroadInstance());
			client.setMemberClass(soapConfig.getMemberClass());
			client.setMemberCode(soapConfig.getMemberCode());
			client.setSubsystemCode(soapConfig.getSubsystem());
			return client;
		}
		
		private XRoadServiceIdentifierType getXRoadServiceIdentifierType () {
			ObjectFactory factory = new ObjectFactory();
			XRoadServiceIdentifierType service = factory.createXRoadServiceIdentifierType();
			service.setXRoadInstance(soapConfig.getServiceXroadInstance());
			service.setMemberClass(soapConfig.getServiceMemberClass());
			service.setSubsystemCode(soapConfig.getServiceSubsystem());
			service.setMemberCode(this.recipient);
			service.setServiceCode(soapConfig.getServiceCode());
			service.setServiceVersion(soapConfig.getServiceVersion());
			return service;
		}
	}
	
	/**
	 * Function must receive document and return unique id of it. extractAndValidateDocument might be part of the receive document process
	 * @param document - document to receive
	 * @return - unique id of the document that was saved. 
	 * @throws DhxException
	 */
	public abstract String recieveDocument (DhxDocument document) throws DhxException;
	
	
	public SendDocumentResponse receiveDocumentFromEndpoint (SendDocument document) throws DhxException{
		DhxDocument dhxDocument = extractAndValidateDocument(document);
		String id = recieveDocument(dhxDocument);
		SendDocumentResponse response = new SendDocumentResponse();
		response.setReceiptId(id);
		return response;
	}
	
	/**
	 * 
	 * @param document - SOAP request object
	 * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if validation is enabled
	 * @throws DhxException
	 */
	public DhxDocument extractAndValidateDocument(SendDocument document) throws DhxException {
		try{
			log.info("Recieving document. for representative: "
					+ document.getRecipient());
			DecContainer container = null;
			DhxDocument dhxDocument = new DhxDocument(document);
			File unpacked = FileUtil.extractAndUnpackAttachment(dhxDocument.getDocumentFile());
			if(config.getCapsuleValidate()) {
				log.debug("Validating capsule is enabled");
				XsdUtil.validate(unpacked, FileUtil.getFileAsStream(config.getCapsuleXsdFile()));
				container = XsdUtil.parseCapsule(unpacked, unmarshaller);
				log.info( "Document data from capsule: recipient organisationCode:" + container.getTransport().getDecRecipient().get(0).getOrganisationCode()
						+ " sender organisationCode:" + container.getTransport().getDecSender().getOrganisationCode());	
				checkRecipient(document.getRecipient());
				log.info( "Recipient from capsule checked and found in representative list or own member code. recipient:" + container.getTransport().getDecRecipient().get(0).getOrganisationCode());	
				
			} else {
				log.debug("Validating capsule is disabled");
			}
			log.info( "Document recieved.");
			return dhxDocument;
		} catch(DhxException ex) {
			log.error(ex.getMessage(), ex);
			log.info( "Document is not recieved. code:" + ex.getExceptionCode() + " message:" + ex.getMessage());
			throw ex;
		}
	}
	
	/**
	 * Function send document using SOAP service.
	 * @param document
	 * @return
	 * @throws DhxException
	 */
	public SendDocumentResponse sendDocument(DhxDocument document) throws DhxException{
		SendDocumentResponse response = null;
			log.info( "Sending document to recipient:" + document.getRecipient());
			try{
				//printStream(stream);
				SendDocument request = new SendDocument();
				request.setRecipient(document.getRecipient());
				request.setDocumentAttachment(document.getDocumentFile());
				if(document.getId() != null) {
					request.setConsignmentId(document.getId());
				} else {
					//TODO: is that ok to generat UUID? or is it ok to send document id as consigment id at all?? 
					request.setConsignmentId(UUID.randomUUID().toString());
				}
				log.info("Sending document for " + document.getRecipient() + " sec server:" + soapConfig.getSecurityServer() + " with setConsignmentId:" + request.getConsignmentId());
				if(getMarshaller() == null) {
					setMarshaller(marshaller);
				}
				if(getUnmarshaller() == null) {
					setUnmarshaller(marshaller);
				}
				response = (SendDocumentResponse) getWebServiceTemplate()
						.marshalSendAndReceive(
								soapConfig.getSecurityServer(),
								request,
								new SoapRequestHeaderModifier(document.getRecipient()));
				log.info( "Document sent to recipient:" + document.getRecipient() + " ReceiptId:" + response.getReceiptId()
						+ (response.getFault()==null?"":" faultCode:" + response.getFault().getFaultCode() + " faultString:" + response.getFault().getFaultString()));
			} catch(Exception e) {
				throw new DhxException(DHXExceptionEnum.WS_ERROR, "Error occured while sending document." + e.getMessage(), e);
			}
			return response;
	}
	
	/*TODO> korduvstaatmine*/

	/**
	 * 
	 * @param recipient
	 * @throws DhxException
	 */
	private void checkRecipient (String recipient) throws DhxException{
		List<String> recipientList = new ArrayList<String>();
		recipientList.addAll(representationService.getRepresentationList());
		recipientList.add(soapConfig.getMemberCode());
		if(!recipientList.contains(recipient)) {
			throw new DhxException(DHXExceptionEnum.WRONG_RECIPIENT, "Recipient not found in representativesList and own member code. recipient:" + recipient);
		}
	}
}
