package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.InternalRepresentee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.DhxHttpComponentsMessageSender;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.RepresentationList;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;
import eu.x_road.xsd.identifiers.ObjectFactory;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadObjectType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import eu.x_road.xsd.representation.XRoadRepresentedPartyType;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;
import org.springframework.ws.client.core.SimpleFaultMessageResolver;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.http.HttpTransportConstants;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;


@Slf4j
@Component
/**
 * Class for consuming X-road SOAP services sendDocument and representationList
 * @author Aleksei Kokarev
 *
 */
public class DhxGateway extends WebServiceGatewaySupport {

  @Autowired
  @Setter
  private DhxConfig config;

  @Autowired
  @Setter
  private SoapConfig soapConfig;

  @Autowired
  @Setter
  DhxMarshallerService dhxMarshallerService;

  /**
   * Postconstruct method which sets marshaller and unmarshaller.
   */
  @PostConstruct
  public void init() {
    setMarshaller(dhxMarshallerService.getJaxbMarshaller());
    setUnmarshaller(dhxMarshallerService.getJaxbMarshaller());
    DhxHttpComponentsMessageSender messageSender = new DhxHttpComponentsMessageSender();
    messageSender.setConnectionTimeout(soapConfig.getConnectionTimeout());
    messageSender.setReadTimeout(soapConfig.getReadTimeout());
    getWebServiceTemplate().setMessageSender(messageSender);
    /*
     * MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
     * setMessageFactory(new SaajSoapMessageFactory(messageFactory));
     */
  }


  /**
   * class to set header when sending SOAP message.
   * 
   * @author Aleksei Kokarev
   *
   */
  private class SoapRequestHeaderModifier implements WebServiceMessageCallback {


    private XroadMember service;
    private XroadMember client;
    private String serviceName;
    private String serviceVersion;

    public SoapRequestHeaderModifier(XroadMember service, XroadMember client, String serviceName,
        String serviceVersion) {
      super();
      this.service = service;
      this.serviceName = serviceName;
      this.serviceVersion = serviceVersion;
      this.client = client;
    }


    private Document convertStringToDocument(String xmlStr) throws IOException, SAXException,
        ParserConfigurationException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder;
      builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
      return doc;
    }

    private void scanChildren(Node elementtoScan, SOAPElement elementToAddChildren)
        throws SOAPException {
      log.debug("scanningCHilder for " + elementtoScan.getNodeName() + ": "
          + elementtoScan.getLocalName() + ": " + elementtoScan.getPrefix() + ": "
          + elementtoScan.getNamespaceURI() + " cur soap element "
          + elementToAddChildren.getLocalName());
      if (elementtoScan.getNodeType() == Node.TEXT_NODE) {
        elementToAddChildren.addTextNode(elementtoScan.getTextContent());
      } else {
        SOAPElement newSoapElement =
            elementToAddChildren.addChildElement(elementtoScan.getLocalName(),
                elementtoScan.getPrefix(), elementtoScan.getNamespaceURI());
        for (int j = 0; j < elementtoScan.getAttributes().getLength(); j++) {
          log.debug("Adding attribute. " + elementtoScan.getAttributes().item(j).getNodeName()
              + " : " + elementtoScan.getAttributes().item(j).getNodeValue());

          newSoapElement.addAttribute(new QName(elementtoScan.getAttributes().item(j)
              .getNamespaceURI(), elementtoScan.getAttributes().item(j).getLocalName(),
              elementtoScan.getAttributes().item(j).getPrefix()), elementtoScan.getAttributes()
              .item(j).getNodeValue());
        }
        for (int i = 0; i < elementtoScan.getChildNodes().getLength(); i++) {
          Node child = elementtoScan.getChildNodes().item(i);
          scanChildren(child, newSoapElement);
        }
      }
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
      try {
        SoapHeader header = ((SoapMessage) message).getSoapHeader();
        for (Iterator it = ((SaajSoapMessage) message).getSaajMessage().getAttachments(); it
            .hasNext();) {
          AttachmentPart attachment = (AttachmentPart) it.next();
          log.debug("attachment part: {}", attachment.getContentType());
          attachment.setMimeHeader(HttpTransportConstants.HEADER_CONTENT_TRANSFER_ENCODING,
              "base64");
        }
        // Transformer transformer = SAXTransformerFactory.newInstance().newTransformer();
        TransformerFactory fact =
            TransformerFactory.newInstance(
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
                null);
        Transformer transformer = fact.newTransformer();
        eu.x_road.xsd.xroad.ObjectFactory factory = new eu.x_road.xsd.xroad.ObjectFactory();
        /*
         * SOAPHeader head = ((SaajSoapMessage) message).getSaajMessage().getSOAPHeader(); String
         * marshalled; marshalled =
         * marshallObjectToString(factory.createProtocolVersion(soapConfig.getProtocolVersion()));
         * scanChildren(convertStringToDocument(marshalled).getFirstChild(), head); marshalled =
         * marshallObjectToString(factory.createId(UUID.randomUUID().toString()));
         * scanChildren(convertStringToDocument(marshalled).getFirstChild(), head); scanChildren(
         * convertStringToDocument(
         * marshallObjectToString(factory.createClient(getXRoadClientIdentifierType())))
         * .getFirstChild(), head); scanChildren( convertStringToDocument(
         * marshallObjectToString(factory.createService(getXRoadServiceIdentifierType())))
         * .getFirstChild(), head);
         */

        transformer.transform(
            marshallObject(factory.createProtocolVersion(soapConfig.getProtocolVersion())),
            header.getResult());
        transformer.transform(
            marshallObject(factory.createId(UUID.randomUUID().toString())), header.getResult());
        transformer.transform(
            marshallObject(factory.createClient(getXRoadClientIdentifierType())),
            header.getResult());
        if(client.getRepresentee() != null) {
          transformer.transform(
            marshallObject(getRepresented(client.getRepresentee())),
            header.getResult());
        }
        transformer.transform(
            marshallObject(factory.createService(getXRoadServiceIdentifierType())),
            header.getResult());

      } catch (DhxException /*| SOAPException | IOException | SAXException
          | ParserConfigurationException*/ ex) {
        throw new RuntimeException(ex);
      }
    }

    private JAXBElement<XRoadRepresentedPartyType> getRepresented (InternalRepresentee rpresentee) {
      eu.x_road.xsd.representation.ObjectFactory factory = new eu.x_road.xsd.representation.ObjectFactory();
      XRoadRepresentedPartyType party = new XRoadRepresentedPartyType();
      party.setPartyCode(rpresentee.getMemberCode());
      return factory.createRepresentedParty(party);
    }
    private StringSource marshallObject(Object obejct)
        throws DhxException {
      String result = "";
      StringWriter sw = dhxMarshallerService.marshallToWriter(obejct);
      result = sw.toString();
      return new StringSource(result);
    }

    private String marshallObjectToString(Object obejct)
        throws DhxException {
      String result = "";
      StringWriter sw = dhxMarshallerService.marshallToWriter(obejct);
      result = sw.toString();
      return result;
    }

    private XRoadClientIdentifierType getXRoadClientIdentifierType() {
      ObjectFactory factory = new ObjectFactory();
      XRoadClientIdentifierType clientXroad = factory.createXRoadClientIdentifierType();
      clientXroad.setXRoadInstance(client.getXroadInstance());
      clientXroad.setMemberClass(client.getMemberClass());
      clientXroad.setMemberCode(client.getMemberCode());
      clientXroad.setSubsystemCode(client.getSubsystemCode());
      return clientXroad;
    }

    private  XRoadServiceIdentifierType getXRoadServiceIdentifierType() {
      ObjectFactory factory = new ObjectFactory();
      XRoadServiceIdentifierType service = factory.createXRoadServiceIdentifierType();
      service.setXRoadInstance(this.service.getXroadInstance());
      service.setMemberClass(this.service.getMemberClass());
      service.setSubsystemCode(this.service.getSubsystemCode());
      service.setMemberCode(this.service.getMemberCode());
      service.setServiceCode(serviceName);
      service.setServiceVersion(serviceVersion);
      service.setObjectType(XRoadObjectType.SERVICE);
      return service;
    }
  }

  /**
   * Method sends document using SOAP service sendDocument. Uses service version from configuration
   * 
   * @param document - document to send using SOAP service
   * @return response of the service
   * @throws DhxException - throws if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(DhxDocument document) throws DhxException {
    return sendDocument(document, soapConfig.getSendDocumentServiceVersion());
  }

  /**
   * Method sends document using SOAP service sendDocument.
   * 
   * @param document - document to send using SOAP service
   * @param xroadServiceVersion - version of sendDocument service. might be usefull if there are
   *        several versions of the service
   * @return response of the service (sending id or fault)
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(DhxDocument document, String xroadServiceVersion)
      throws DhxException {
    SendDocumentResponse response = null;
    log.info("Sending document to recipient: {}", document.getService().toString());
    try {
      SendDocument request = new SendDocument();
      if (document.getService().getRepresentee() != null) {
        request.setRecipient(document.getService().getRepresentee().getMemberCode());
      }
      request.setDocumentAttachment(document.getDocumentFile());
      if (document.getInternalConsignmentId() != null
          && !document.getInternalConsignmentId().isEmpty()) {
        request.setConsignmentId(document.getInternalConsignmentId());
      } else {
        throw new DhxException(DhxExceptionEnum.DATA_ERROR, "Consignment id is empty!");
      }
      log.info("Sending document for {} sec server: {} with consignmentId: {}", document
          .getService().toString(), soapConfig.getSecurityServer(), request.getConsignmentId());
      getWebServiceTemplate().setCheckConnectionForFault(false);
      getWebServiceTemplate().setCheckConnectionForError(false);
      SimpleFaultMessageResolver resolver = new SimpleFaultMessageResolver();
      getWebServiceTemplate().setFaultMessageResolver(resolver);
      response =
          (SendDocumentResponse) getWebServiceTemplate().marshalSendAndReceive(
              soapConfig.getSecurityServerWithAppender(),
              request,
              new SoapRequestHeaderModifier(document.getService(), document.getClient(),
                  soapConfig
                      .getSendDocumentServiceCode(), xroadServiceVersion));
      log.info("Document sent to: {} ReceiptId: {} Fault: {}",
          document.getService().toString(),
          response.getReceiptId(),
          (response.getFault() == null ? "" : " faultCode:"
              + response.getFault().getFaultCode() + " faultString:"
              + response.getFault().getFaultString()));
    } catch (WebServiceFaultException ex) {
      Fault fault = new Fault();
      fault.setFaultCode(ex.getWebServiceMessage().getFaultCode().getLocalPart());
      fault.setFaultString("SOAP fault returned from web service: " + ex.getMessage());
      response = new SendDocumentResponse();
      response.setFault(fault);
    } catch (Exception ex) {
      throw new DhxException(DhxExceptionEnum.WS_ERROR, "Error occured while sending document."
          + ex.getMessage(), ex);
    }
    return response;
  }

  /**
   * Function gets representation list using SOAP service. uses service version from configuration
   * 
   * @param member - X-road member whom representation list to ask
   * @return - response of the service(list of representees or empty list)
   * @throws DhxException - thrown if error occurs while getting representation list
   */
  public RepresentationListResponse getRepresentationList(XroadMember member) throws DhxException {
    return getRepresentationList(member, soapConfig.getDefaultClient(),
        soapConfig.getRepresentativesServiceVersion());
  }

  /**
   * Function gets representation list using SOAP service. uses service version from configuration
   * 
   * @param member - X-road member whom representation list to ask
   * @param sender - X-road member who sends representation list query(self)
   * @return - response of the service(list of representees or empty list)
   * @throws DhxException - thrown if error occurs while getting representation list
   */
  public RepresentationListResponse getRepresentationList(XroadMember member, XroadMember sender)
      throws DhxException {
    return getRepresentationList(member, sender, soapConfig.getRepresentativesServiceVersion());
  }

  /**
   * Method get representation list using SOAP service.
   * 
   * @param member - X-road member whom representation list to ask
   * @param member - X-road member who sends representation list query(self)
   * @param xroadServiceVersion - version of sendDocument service. might be usefull if there are
   *        several versions of the service
   * @return - response of the service(list of representees or empty list)
   * @throws DhxException - thrown if error occurs while getting representation list
   */
  public RepresentationListResponse getRepresentationList(XroadMember member, XroadMember sender,
      String xroadServiceVersion) throws DhxException {
    RepresentationListResponse response = null;
    log.info("Getting representation list from: {}", member.toString());
    try {
      getWebServiceTemplate().setCheckConnectionForFault(false);
      getWebServiceTemplate().setCheckConnectionForError(false);
      SimpleFaultMessageResolver resolver = new SimpleFaultMessageResolver();
      getWebServiceTemplate().setFaultMessageResolver(resolver);
      response =
          (RepresentationListResponse) getWebServiceTemplate().marshalSendAndReceive(
              soapConfig.getSecurityServerWithAppender(),
              new RepresentationList(),
              new SoapRequestHeaderModifier(member, sender, soapConfig
                  .getRepresentativesServiceCode(),
                  xroadServiceVersion));
      log.info("Representation list received");
    } catch (WebServiceFaultException ex) {
      throw new DhxException(DhxExceptionEnum.WS_ERROR,
          "Error occured while getting representation list. SOAP-fault:"
              + ex.getWebServiceMessage().getFaultCode().getLocalPart()
              + "SOAP fault returned from web service: " + ex.getMessage(), ex);
    } catch (Exception ex) {
      throw new DhxException(DhxExceptionEnum.WS_ERROR,
          "Error occured while getting representation list."
              + ex.getMessage(), ex);
    }
    return response;
  }

  /**
   * Method finds header in message context and sets it back to response also. Client element is
   * parsed and returned.
   * 
   * @param messageContext - SOAP message context
   * @return - client XroadMember found in SOAP message header
   * @throws DhxException - throws if error occurs while getting client from SOAP message
   */
  @SuppressWarnings({"unchecked"})
  public XroadMember getXroadClientAndSetRersponseHeader(MessageContext messageContext)
      throws DhxException {
    try {
      XroadMember client = null;
      SaajSoapMessage soapRequest = (SaajSoapMessage) messageContext.getRequest();
      SoapHeader reqheader = soapRequest.getSoapHeader();
      SaajSoapMessage soapResponse = (SaajSoapMessage) messageContext.getResponse();
      SoapHeader respheader = soapResponse.getSoapHeader();
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      Iterator<SoapHeaderElement> itr = reqheader.examineAllHeaderElements();
      InternalRepresentee representee = null;
      while (itr.hasNext()) {
        SoapHeaderElement ele = itr.next();
        if (ele.getName().getLocalPart().endsWith("client")) {
          JAXBElement<XRoadClientIdentifierType> xrdClientElement =
              (JAXBElement<XRoadClientIdentifierType>) dhxMarshallerService.unmarshall(ele
                  .getSource());
          XRoadClientIdentifierType xrdClient = xrdClientElement.getValue();
          if (xrdClient != null) {
            client = new XroadMember(xrdClient);
          } else {
            throw new DhxException(DhxExceptionEnum.WS_ERROR,
                "Unable to find xroad client in header.");
          }

        } else if(ele.getName().getLocalPart().endsWith("representedParty")) {
          JAXBElement<XRoadRepresentedPartyType> xrdRepresented =
              (JAXBElement<XRoadRepresentedPartyType>) dhxMarshallerService.unmarshall(ele
                  .getSource());
          representee = new InternalRepresentee(xrdRepresented.getValue().getPartyCode(), null, null, null, null);
        }
        transformer.transform(ele.getSource(), respheader.getResult());
      }
      client.setRepresentee(representee);
      log.debug("xrd client memberCode: {}", client.getMemberCode());
      return client;
    } catch (TransformerException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while reading info form soap header." + ex.getMessage(), ex);
    }
  }

  /**
   * Method finds header in message context, parses and returns service element.
   * 
   * @param messageContext - SOAP message context
   * @return - service XroadMember found in SOAP message header
   * @throws DhxException - throws if error occurs while getting client from SOAP message
   */
  @SuppressWarnings({"unchecked"})
  public XroadMember getXroadService(MessageContext messageContext)
      throws DhxException {
    XroadMember service = null;
    SaajSoapMessage soapRequest = (SaajSoapMessage) messageContext.getRequest();
    SoapHeader reqheader = soapRequest.getSoapHeader();
    Iterator<SoapHeaderElement> itr = reqheader.examineAllHeaderElements();
    while (itr.hasNext()) {
      SoapHeaderElement ele = itr.next();
      if (ele.getName().getLocalPart().endsWith("service")) {
        JAXBElement<XRoadServiceIdentifierType> xrdServiceElement =
            (JAXBElement<XRoadServiceIdentifierType>) dhxMarshallerService.unmarshall(ele
                .getSource());
        XRoadServiceIdentifierType xrdService = xrdServiceElement.getValue();
        if (xrdService != null) {
          service = new XroadMember(xrdService);
        } else {
          throw new DhxException(DhxExceptionEnum.WS_ERROR,
              "Unable to find xroad client in header.");
        }

      }
    }
    log.debug("xrd service: {}", service.toString());
    return service;
  }

}
