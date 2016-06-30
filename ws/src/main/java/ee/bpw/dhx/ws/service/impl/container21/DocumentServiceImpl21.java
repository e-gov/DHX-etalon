package ee.bpw.dhx.ws.service.impl.container21;

import ee.bpw.dhx.container21.DhxDocument21;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.impl.DocumentServiceImpl;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Implementation of document service specific for container version 2.1. Extends generis
 * DocumentService implementation. Uses containers specific for capsule version 2.1
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
public abstract class DocumentServiceImpl21 extends DocumentServiceImpl {

  @Autowired
  private DhxConfig config;

  @Autowired
  private SoapConfig soapConfig;

  @Autowired
  Unmarshaller unmarshaller;

  @Autowired
  Jaxb2Marshaller jaxMarshaller;

  @Autowired
  AddressService addressService;

  @Autowired
  DhxGateway documentGateway;

  Marshaller marshaller;

  @PostConstruct
  public void init() throws JAXBException {
    marshaller = jaxMarshaller.getJaxbContext().createMarshaller();
  }


  @Override
  /**
   * Method is a wrapper. Parses capsule from stream and send document.
   * @see sendDocument(DecContainer container, String consignmentId)
   * @Deprecated
   * @param capsuleStream 
   * @param consignmentId
   * @return
   * @throws DhxException
   */
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException {
    /*
     * if(config.getCapsuleValidate()){ validate(capsuleStream,
     * FileUtil.getFileAsStream(config.getCapsuleXsdFile())); }
     */
    checkFileSize(capsuleFile);
    DecContainer container = XsdUtil.unmarshallCapsule(capsuleFile, unmarshaller);
    return sendDocument(container, consignmentId);
  }

  /**
   * 
   * @param container - container which needs to be sent. container will be sent to each recipient
   *        defined in container
   * @param consignmentId - id of the sending package. not id of the document. if null, then random
   *        consignmentID will be generated
   * @return service response, containing information about document for every receipient
   * @throws DhxException - throws error if it occured while reading container. if error occured
   *         while sending to one of the recipients, then error returned in reponse fault
   */
  protected List<SendDocumentResponse> sendDocument(DecContainer container, String consignmentId)
      throws DhxException {
    List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
    if (container != null && container.getTransport() != null
        && container.getTransport().getDecRecipient() != null
        && container.getTransport().getDecRecipient().size() > 0) {
      File capsuleFile = null;
      capsuleFile = XsdUtil.marshallCapsule(container, marshaller);
      for (DecRecipient recipient : container.getTransport().getDecRecipient()) {
        XroadMember adressee =
            addressService.getClientForMemberCode(recipient.getOrganisationCode());
        DhxDocument document =
            new DhxDocument21(adressee, /* recipient.getOrganisationCode(), */container,
                capsuleFile, true);
        responses.add(sendDocumentTry(document));
      }
      return responses;

    } else {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Container or recipient is empty. Unable to send document");
    }

  }

  /**
   * Tries to send document and if error occurs, then returns response with fault, not raises
   * exception.
   * 
   * @param document - Document to try to send
   * @return service response for single recipient defined in document
   */
  protected SendDocumentResponse sendDocumentTry(DhxDocument document) {
    SendDocumentResponse response = null;
    try {
      response = documentGateway.sendDocument(document);
    } catch (Exception ex) {
      log.error("Error occured while sending docuemnt. " + ex.getMessage(), ex);
      DhxExceptionEnum faultCode = DhxExceptionEnum.TECHNICAL_ERROR;
      if (ex instanceof DhxException) {
        if (((DhxException) ex).getExceptionCode() != null) {
          faultCode = ((DhxException) ex).getExceptionCode();
        }
      }
      response = new SendDocumentResponse();
      Fault fault = new Fault();
      fault.setFaultCode(faultCode.getCodeForService());
      fault.setFaultString(ex.getMessage());
      response.setFault(fault);
    }
    return response;
  }

  /**
   * Method extracts and validates capsule. Uses capsuleXsdFile21 configuration parameter for find
   * XSD against which to validate
   * 
   * @param document - SOAP request object
   * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if
   *         validation is enabled
   * @throws DhxException - throws if error occured while reading or extracting file
   */
  @Override
  protected DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client)
      throws DhxException {
    try {
      log.info("Recieving document. for representative: " + document.getRecipient());
      DecContainer container = null;
      File unpacked = FileUtil.extractAndUnpackAttachment(document.getDocumentAttachment());
      checkFileSize(unpacked);
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        XsdUtil.validate(unpacked, FileUtil.getFileAsStream(config.getCapsuleXsdFile21()));
      } else {
        log.debug("Validating capsule is disabled");
      }
      container = XsdUtil.unmarshallCapsule(unpacked, unmarshaller);
      log.info("Document data from capsule: recipient organisationCode:"
          + container.getTransport().getDecRecipient().get(0).getOrganisationCode()
          + " sender organisationCode:"
          + container.getTransport().getDecSender().getOrganisationCode());
      super.checkRecipient(document.getRecipient(), getStringList(container.getTransport()
          .getDecRecipient()));
      log.info("Recipient from capsule checked and found in representative list "
          + "or own member code. recipient:"
          + container.getTransport().getDecRecipient().get(0).getOrganisationCode());
      log.info("Document recieved.");
      DhxDocument21 dhxDocument = new DhxDocument21(client, document, container);
      return dhxDocument;
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not recieved. code:" + ex.getExceptionCode() + " message:"
          + ex.getMessage());
      throw ex;
    }
  }

  private List<String> getStringList(List<DecRecipient> recipients) {
    List<String> recipientList = new ArrayList<String>();
    if (recipients != null && recipients.size() > 0) {
      for (DecRecipient recipient : recipients) {
        recipientList.add(recipient.getOrganisationCode());
      }
    }
    return recipientList;
  }

  /**
   * Function receives document and return unique id of it. Id will be sent as receipt in response
   * 
   * @param document - document to receive
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public String recieveDocument(DhxDocument document) throws DhxException {
    if (document instanceof DhxDocument21) {
      return recieveDocument2_1((DhxDocument21) document);
    } else {
      throw new DhxException(DhxExceptionEnum.WS_ERROR,
          "Wrong type of document. Expected version 2.1");
    }
  }

  /**
   * Function receives document of version 2.1 and returns unique id of it. Id will be sent as
   * receipt in response
   * 
   * @param document - document to receive
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public abstract String recieveDocument2_1(DhxDocument21 document) throws DhxException;

}
