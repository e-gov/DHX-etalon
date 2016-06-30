package ee.bpw.dhx.ws;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.Members;
import eu.x_road.dhx.producer.RepresentationList;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;

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

  /**
   * X-road SOAP service sendDocument. 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocument")
  @ResponsePayload
  public SendDocumentResponse sendDocument(@RequestPayload SendDocument request,
      MessageContext messageContext) {
    SendDocumentResponse response = new SendDocumentResponse();
    try {
      XroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      response = documentService.receiveDocumentFromEndpoint(request, client);
      // response.setStatus(StatusEnum.ACCEPTED.getName());
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      // response.setStatus(StatusEnum.REJECTED.getName());
      Fault fault = new Fault();
      fault.setFaultCode(ex.getExceptionCode().getCodeForService());
      fault.setFaultString(ex.getMessage());
      response.setFault(fault);
    }
    return response;
  }

  /**
   * X-road SOAP service representationList.
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains representee list
   * @throws DhxException - throws when error occurs while getting representation list
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "representationList")
  @ResponsePayload
  public RepresentationListResponse representationList(
      @RequestPayload RepresentationList request, MessageContext messageContext) throws DhxException{
    try {
      RepresentationListResponse response = new RepresentationListResponse();
      dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      List<Representee> representees = representationService.getRepresentationList();
      if (representees != null) {
        // MemberCodes memberCodes = new MemberCodes();
        Members members = new Members();
        for (Representee representee : representees) {
          members.getMember().add(representee.convertToMember());
        }
        response.setMembers(members);
      }
      return response;
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw ex;
    }
  }

}
