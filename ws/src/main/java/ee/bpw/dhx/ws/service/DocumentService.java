package ee.bpw.dhx.ws.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
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
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.StringSource;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadClient;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import eu.x_road.dhx.producer.Fault;
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
	 * Function must receive document and return unique id of it. Id will be sent as receipt is response
	 * @param document - document to receive
	 * @return - unique id of the document that was saved. 
	 * @throws DhxException
	 */
	public abstract String recieveDocument (DhxDocument document) throws DhxException;
	
	
	public SendDocumentResponse receiveDocumentFromEndpoint (SendDocument document, MessageContext messageContext) throws DhxException{
		XroadClient client = getXroadCLientAndSetRersponseHeader(messageContext);
		
		if (isDuplicatePackage(client, document.getConsignmentId())) {
			throw new DhxException(DHXExceptionEnum.DUPLICATE_PACKAGE, "Already got package with this consignmentID. from:" + client.toString() + " consignmentId:" + document.getConsignmentId());
		} else {
			DhxDocument dhxDocument = extractAndValidateDocument(document);
			dhxDocument.setClient(client);
			String id = recieveDocument(dhxDocument);
			SendDocumentResponse response = new SendDocumentResponse();
			response.setReceiptId(id);
			return response;
		}
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
				container = unmarshallCapsule(unpacked);
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
	
	public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId) throws DhxException{
		DecContainer container = unmarshallCapsule(capsuleFile);
		return sendDocument(container, consignmentId);
	}
	
	public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId) throws DhxException{
		DecContainer container = unmarshallCapsule(capsuleStream);
		return sendDocument(container, consignmentId);
	}
	
	/**
	 * 
	 * @param container - container which needs to be sent
	 * @param consignmentId - id of the sending package. not id of the document. if null, then random consignmentID will be generated
	 * @return
	 * @throws DhxException - throws error if it occured while reading container. if error occured while sending to one of the recipients, then error returned in reponse fault
	 */
	public List<SendDocumentResponse> sendDocument(DecContainer container, String consignmentId) throws DhxException{
		List<SendDocumentResponse> responses  = new ArrayList<SendDocumentResponse>();
		if(container != null && container.getTransport() != null 
				&& container.getTransport().getDecRecipient() != null 
				&& container.getTransport().getDecRecipient().size()>0) {
			File capsuleFile = null;
		    capsuleFile = marshallCapsule(container);
	        for (DecRecipient recipient : container.getTransport().getDecRecipient()) {	
	        	DhxDocument document = new DhxDocument(recipient.getOrganisationCode(), container, capsuleFile, true);
	        	try{
		        	SendDocumentResponse response  = sendDocument(document);
		        	responses.add(response);
	        	} catch (Exception ex) {
	        		log.error("Error occured while sending docuemnt. " + ex.getMessage(), ex);
	        		DHXExceptionEnum faultCode = DHXExceptionEnum.TECHNICAL_ERROR;
	        		if(ex instanceof DhxException) {
	        			if (((DhxException)ex).getExceptionCode() != null) {
	        				faultCode = ((DhxException)ex).getExceptionCode();
	        			}
	        		}
	        		SendDocumentResponse response = new SendDocumentResponse();
        			Fault fault = new Fault();
        			fault.setFaultCode(faultCode.getCodeForService());
        			fault.setFaultString(ex.getMessage());
        			response.setFault(fault);
	        	}
	        }
	        return responses;

		} else {
			throw new DhxException("Container or recipient is empty. Unable to send document");
		}
		
	}
	
	/**
	 * Function send document using SOAP service.
	 * @param document
	 * @return
	 * @throws DhxException
	 */
	protected SendDocumentResponse sendDocument(DhxDocument document) throws DhxException{
		SendDocumentResponse response = null;
			log.info( "Sending document to recipient:" + document.getRecipient());
			try{
				//printStream(stream);
				SendDocument request = new SendDocument();
				request.setRecipient(document.getRecipient());
				request.setDocumentAttachment(document.getDocumentFile());
				if(document.getInternalConsignmentId() != null && !document.getInternalConsignmentId().isEmpty()) {
					request.setConsignmentId(document.getInternalConsignmentId());
				} else {
					//TODO: is that ok to generat UUID? or is it ok to send document id as consigment id at all?? 
					request.setConsignmentId(UUID.randomUUID().toString());
				}
				log.info("Sending document for " + document.getRecipient() + " sec server:" + soapConfig.getSecurityServer() + " with consignmentId:" + request.getConsignmentId());
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
	
	public Object unmarshall (Source source, Class classToUnmarshall) throws DhxException{
		try {
			Object obj = (Object) unmarshaller.unmarshal(source);
			log.debug("declared type" + ((JAXBElement)obj).getValue());
			if (classToUnmarshall.isInstance(obj)) {
				return obj;
			}else if(obj instanceof JAXBElement && classToUnmarshall.isInstance(((JAXBElement)obj).getValue())) {
				return ((JAXBElement)obj).getValue();
			}
			else {
				log.info("Got unknown unmarshalled object. class:" + obj.getClass().getCanonicalName());
				return null;
			}
		} catch (JAXBException ex) {
			throw new DhxException("Unable to unmrashall object. Obj desired class:" + classToUnmarshall.getCanonicalName() + ex.getMessage(), ex);
		}
	}
	
	public XroadClient getXroadCLientAndSetRersponseHeader (MessageContext messageContext) throws DhxException{
		try{
		XroadClient client = null;
		SaajSoapMessage soapRequest = (SaajSoapMessage) messageContext
                .getRequest();
        SoapHeader reqheader = soapRequest.getSoapHeader();
        SaajSoapMessage soapResponse = (SaajSoapMessage) messageContext
                .getResponse();
        SoapHeader respheader = soapResponse.getSoapHeader();
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Iterator<SoapHeaderElement> itr = reqheader.examineAllHeaderElements();
        while (itr.hasNext()) {
            SoapHeaderElement ele = itr.next();
            if(ele.getName().getLocalPart().endsWith("client")) {
            	XRoadClientIdentifierType xrdClient = (XRoadClientIdentifierType)unmarshall(ele.getSource(), XRoadClientIdentifierType.class);
            	if(xrdClient != null) {
            		client = new XroadClient(xrdClient);
            	} else {
            		throw new DhxException("Unable to find xroad client in header.");
            	}
            	
            }
            transformer.transform(ele.getSource(), respheader.getResult());
        }
        log.debug("xrd client" + client.getMemberCode());
		return client;
		}catch(TransformerException e) {
			throw new DhxException("Error occured while reading info form soap header." + e.getMessage(), e);
		}
	}
	
	public DecContainer unmarshallCapsule (File capsuleFile) throws DhxException{
		try{
			return unmarshallCapsule(new FileInputStream(capsuleFile));	
		}catch(FileNotFoundException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}
	/**
	 * Parses capsule
	 * @param capsuleFile
	 * @return
	 * @throws DhxException
	 */
	public DecContainer unmarshallCapsule (InputStream capsuleStream) throws DhxException{
		try{
			if (log.isDebugEnabled()) {
				log.debug("unmarshalling file" );
			}
			/*JAXBContext unmarshalContext = JAXBContext.newInstance("ee.riik.schemas.deccontainer.vers_2_1");
			Unmarshaller unmarshaller = unmarshalContext.createUnmarshaller();	*/
			Object obj = (Object) unmarshaller.unmarshal(capsuleStream);
			if (obj instanceof DecContainer) {
				return (DecContainer)obj;
			} else {
				log.info("Got unknown unmarshalled object");
				return null;
			}
		}catch(JAXBException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}
	
	/**
	 * Parses capsule
	 * @param capsuleFile
	 * @return
	 * @throws DhxException
	 */
	public File marshallCapsule (DecContainer container) throws DhxException{
		try{
			Marshaller marshallerHeader = marshaller.getJaxbContext().createMarshaller();
			if (log.isDebugEnabled()) {
				log.debug("marshalling container" );
			}
			File outputFile = FileUtil.createPipelineFile(0, "");
			marshallerHeader.marshal(container, outputFile);
			return outputFile;
		}catch(IOException|JAXBException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}

	//abstract public List<String> getSendingOptions();
	
	abstract public boolean isDuplicatePackage (XroadClient from, String consignmentId);
}