package ee.bpw.dhx.ws;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;
import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.MemberCodes;
import eu.x_road.dhx.producer.Members;
import eu.x_road.dhx.producer.RepresentationList;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

@Slf4j
@Endpoint
/**
 * Endpoint class which offers SOAP services.
 * @author Aleksei Kokarev
 *
 */
public class DhxEndpoint {
	
	public static final String NAMESPACE_URI = "http://dhx.x-road.eu/producer";


	@Autowired
	DocumentService documentService;
	
	@Autowired
	RepresentationService representationService;
	
	@Autowired
	DhxGateway dhxGateway;

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocument")
	@ResponsePayload
	/**
	 * X-road SOAP service sendDocument. 
	 * @param request 
	 * @param messageContext
	 * @return
	 * @throws Exception
	 */
	public SendDocumentResponse sendDocument(
			@RequestPayload SendDocument request,
           MessageContext messageContext) throws Exception {
		SendDocumentResponse response = new SendDocumentResponse();
		try {
			XroadMember client = dhxGateway.getXroadCLientAndSetRersponseHeader(messageContext);
			response = documentService.receiveDocumentFromEndpoint(request, client);
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
	/**
	 * X-road SOAP service representationList.
	 * @param request
	 * @param messageContext
	 * @return
	 * @throws Exception
	 */
	public RepresentationListResponse representationList(
			@RequestPayload RepresentationList request, MessageContext messageContext) throws Exception {
		try {
			RepresentationListResponse response = new RepresentationListResponse();
			dhxGateway.getXroadCLientAndSetRersponseHeader(messageContext);
			List<Representee> representees = representationService.getRepresentationList();
			if(representees != null) {
				//MemberCodes memberCodes = new MemberCodes();
				Members members = new Members();
				for(Representee representee : representees) {	
					members.getMember().add(representee.convertToMember());
				}
			}
			return response;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

}
