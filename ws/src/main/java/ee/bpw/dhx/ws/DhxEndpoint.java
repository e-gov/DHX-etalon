package ee.bpw.dhx.ws;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.StatusEnum;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;
import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.MemberCodes;
import eu.x_road.dhx.producer.RepresentationList;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;

@Slf4j
@Endpoint
public class DhxEndpoint {
	
	public static final String NAMESPACE_URI = "http://dhx.x-road.eu/producer";


	@Autowired
	DocumentService documentService;
	
	@Autowired
	RepresentationService representationService;

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocument")
	@ResponsePayload
	public SendDocumentResponse sendDocument(
			@RequestPayload SendDocument request,
           MessageContext messageContext) throws Exception {
		SendDocumentResponse response = new SendDocumentResponse();
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
	            
	            log.debug("heade element" +ele.getName().getLocalPart());
	            if(ele.getName().getLocalPart().endsWith("client")) {
	            	XRoadClientIdentifierType client = (XRoadClientIdentifierType)documentService.unmarshall(ele.getSource(), XRoadClientIdentifierType.class);
	            	 log.debug("mem code" +client.getMemberCode());
	            }
	            transformer.transform(ele.getSource(), respheader.getResult());
	        }
		try {
			response = documentService.receiveDocumentFromEndpoint(request, messageContext);
			//response.setStatus(StatusEnum.ACCEPTED.getName());
		} catch (DhxException e) {
			log.error(e.getMessage(), e);
			//response.setStatus(StatusEnum.REJECTED.getName());
			Fault fault = new Fault();
			fault.setFaultCode(e.getExceptionCode().getCodeForService());
			fault.setFaultString(e.getMessage());
			response.setFault(fault);
		}
		return response;
	}
	
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "representationList")
	@ResponsePayload
	public RepresentationListResponse representationList(
			@RequestPayload RepresentationList request) throws Exception {
		try {
			RepresentationListResponse response = new RepresentationListResponse();
			List<String> representatives = representationService.getRepresentationList();
			if(representatives != null) {
				MemberCodes memberCodes = new MemberCodes();
				response.setMemberCodes(memberCodes);
				for(String representative : representatives) {	
					response.getMemberCodes().getMemberCode().add(representative);
				}
			}
			return response;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

}
