package ee.bpw.dhx.ws;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DocumentService;

import com.jcabi.aspects.Loggable;

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
  DhxImplementationSpecificService dhxImplementationSpecificService;

  @Autowired
  DhxGateway dhxGateway;

  /**
   * X-road SOAP service sendDocument.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocument")
  @ResponsePayload
  @Loggable
  public SendDocumentResponse sendDocument(@RequestPayload SendDocument request,
      MessageContext messageContext) throws DhxException {
    SendDocumentResponse response = new SendDocumentResponse();
    try {
      XroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      response = documentService.receiveDocumentFromEndpoint(request, client);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      if (ex.getExceptionCode().isBusinessException()) {
        Fault fault = new Fault();
        fault.setFaultCode(ex.getExceptionCode().getCodeForService());
        fault.setFaultString(ex.getMessage());
        response.setFault(fault);
      } else {
        throw ex;
      }
    }
    return response;
  }

  /**
   * X-road SOAP service representationList.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains representee list
   * @throws DhxException - throws when error occurs while getting representation list
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "representationList")
  @ResponsePayload
  @Loggable
  public RepresentationListResponse representationList(
      @RequestPayload RepresentationList request, MessageContext messageContext)
      throws DhxException {
    try {
      RepresentationListResponse response = new RepresentationListResponse();
      dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      List<Representee> representees = dhxImplementationSpecificService.getRepresentationList();
      if (representees != null) {
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
