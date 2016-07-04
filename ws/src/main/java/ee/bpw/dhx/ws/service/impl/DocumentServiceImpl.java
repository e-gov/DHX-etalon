package ee.bpw.dhx.ws.service.impl;

import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.util.XsdVersionEnum;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;



/**
 * Generic class for document sending and receiving. Does not parse capsule neither validates
 * capsule, because does not contain information about container. capsule - xml file containing
 * document and metadata about the document
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public abstract class DocumentServiceImpl implements DocumentService {

  @Autowired
  private DhxConfig config;

  @Autowired
  private SoapConfig soapConfig;

  @Autowired
  RepresentationService representationService;

  @Autowired
  protected Unmarshaller unmarshaller;

  @Autowired
  Jaxb2Marshaller jaxMarshaller;

  @Autowired
  AddressServiceImpl addressService;

  @Autowired
  DhxGateway documentGateway;

  protected Marshaller marshaller;

  @PostConstruct
  public void init() throws JAXBException {
    marshaller = jaxMarshaller.getJaxbContext().createMarshaller();
  }


  /**
   * Method should receive document(save in database for example) and return unique id of it. Id
   * will be sent as receipt in response.
   * 
   * @param document - document to receive
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  // public abstract String recieveDocument(DhxDocument document) throws DhxException;

  /**
   * Method should send document to all recipients defined in capsule. Should be implemented in
   * service which has information about version of the capsule
   * 
   * @param capsuleFile - file cantaining capsule to send
   * @param consignmentId - consignment id to set when sending file.
   * @return service responses for each recipient
   * @throws DhxException - thrown if error occurs while receiving document
   */
  /*
   * public abstract List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
   * throws DhxException;
   */

  private SendDocumentResponse sendDocumentNoCapsulePasring(File capsuleFile,
      String consignmentId, String recipient) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    XroadMember adressee = addressService.getClientForMemberCode(recipient);
    DhxDocument document = new DhxDocument(adressee, capsuleFile, true);
    SendDocumentResponse response = documentGateway.sendDocument(document);
    return response;
  }


  /**
   * Method is used by endpoint. Is called when document arrives to endpoint Does capsule pasring if
   * it is configured.
   * 
   * @param document - service iniput parameters. document to receive
   * @param client - SOAP message client(who sent the request).
   * @return service response
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public SendDocumentResponse receiveDocumentFromEndpoint(SendDocument document,
      XroadMember client) throws DhxException {
    // XroadMember client = documentGateway.getXroadCLientAndSetRersponseHeader(messageContext);

    if (config.getCheckDuplicate() && isDuplicatePackage(client, document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DUPLICATE_PACKAGE,
          "Already got package with this consignmentID. from:" + client.toString()
              + " consignmentId:" + document.getConsignmentId());
    } else {
      if (document.getRecipient() != null) {
        XroadMember member = addressService.getClientForMemberCode(document.getRecipient());
        client.setRepresentee(member.getRepresentee());
      }
      DhxDocument dhxDocument;
      if (config.getParseCapsule()) {
        dhxDocument = extractAndValidateDocument(document, client);
      } else {
        dhxDocument = extractAndValidateDocumentNoParsing(document, client);
      }
      dhxDocument.setClient(client);
      String id = recieveDocument(dhxDocument);
      SendDocumentResponse response = new SendDocumentResponse();
      response.setReceiptId(id);
      return response;
    }
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
  protected DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client)
      throws DhxException {
    try {
      log.info("Recieving document. for representative: " + document.getRecipient());
      DecContainer container = null;
      File unpacked = FileUtil.extractAndUnpackAttachment(document.getDocumentAttachment());
      checkFileSize(unpacked);
      container = XsdUtil.unmarshallCapsule(unpacked, unmarshaller);
      log.info("Document data from capsule: recipient organisationCode:"
          + container.getTransport().getDecRecipient().get(0).getOrganisationCode()
          + " sender organisationCode:"
          + container.getTransport().getDecSender().getOrganisationCode());
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        XsdUtil.validate(unpacked, FileUtil.getFileAsStream(config
            .getXsdForVersion(XsdVersionEnum.forClass(container.getClass()))));
      } else {
        log.debug("Validating capsule is disabled");
      }
      if(config.getCheckRecipient()) {
        checkRecipient(document.getRecipient(), getStringList(container.getTransport()
            .getDecRecipient()));
      }
      log.info("Recipient from capsule checked and found in representative list "
          + "or own member code. recipient:"
          + container.getTransport().getDecRecipient().get(0).getOrganisationCode());
      log.info("Document recieved.");
      DhxDocument dhxDocument =
          new DhxDocument(client, document, container, XsdVersionEnum.forClass(container
              .getClass()));
      return dhxDocument;
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not recieved. code:" + ex.getExceptionCode() + " message:"
          + ex.getMessage());
      throw ex;
    }
  }

  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      String recipient) throws DhxException {
    if (recipient != null && !recipient.equals("")) {
      checkFileSize(capsuleFile);
      List<SendDocumentResponse> responses =  new ArrayList<SendDocumentResponse>();
      responses.add(sendDocumentNoCapsulePasring(capsuleFile, consignmentId, recipient));
      return responses;
    } else {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not defined in input. Unable to define recipient");
    }
  }

  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException {     
    log.debug("List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)");
    if (config.getParseCapsule()) {
      checkFileSize(capsuleFile);
      Object container = XsdUtil.unmarshallCapsule(capsuleFile, unmarshaller);
      if(config.getCapsuleValidate()){ 
        XsdUtil.validate(capsuleFile, FileUtil.getFileAsStream(config.getXsdForVersion(XsdVersionEnum.forClass(container.getClass())))); 
       }
      return sendDocument(container, capsuleFile, consignmentId);
    } else {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Unable to define adressees without parsing capsule. parsing capsule is disabled in configuration.");
    }
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
  protected List<SendDocumentResponse> sendDocument(Object container, File capsuleFile, String consignmentId)
      throws DhxException {
    log.debug("Sending document with capsule parsing.");
    List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
    List<CapsuleAdressee> adressees = XsdUtil.getAdresseesFromContainer(container);
    if (adressees != null && adressees.size() > 0) {
      //File capsuleFile = null;
      //capsuleFile = XsdUtil.marshallCapsule(container, marshaller);
      for (CapsuleAdressee adressee : adressees) {
        XroadMember adresseeXroad =
            addressService.getClientForMemberCode(adressee.getAdresseeCode());
        DhxDocument document =
            new DhxDocument(adresseeXroad, container, XsdVersionEnum.forClass(container.getClass()),
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
   * Method extracts and validates attached document. Attachment validation is not implemented in
   * this version of service.
   * 
   * @param document - SOAP request object
   * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if
   *         validation is enabled
   * @throws DhxException - thrown if error occurs while extracting or validating document
   */
  protected DhxDocument extractAndValidateDocumentNoParsing(SendDocument document,
      XroadMember client) throws DhxException {
    try {
      log.info("Recieving document. for representative: " + document.getRecipient());
      // File unpacked = FileUtil.extractAndUnpackAttachment(dhxDocument.getDocumentFile());
      if (config.getCapsuleValidate()) {
        throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
            "Capsule validation is not implemented. Use service precific for container version");

      } else {
        log.debug("Validating capsule is disabled");
      }
      if(config.getCheckRecipient()) {
        checkRecipient(document.getRecipient(), null);
        log.info("Recipient checked and found in representative list or own member code. recipient:"
            + document.getRecipient());
      }
      DhxDocument dhxDocument = new DhxDocument(client, document);
      log.info("Document recieved.");
      return dhxDocument;
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not recieved. code:" + ex.getExceptionCode() + " message:"
          + ex.getMessage());
      throw ex;
    }
  }

  /**
   * Checks if recipient is present in representativesList and in capsule recipients. Needed to be
   * sure if document is sent to right recipient. Recipient(if not null) MUST be found in list of
   * own memberCode or own representationList. Recipient or own member code MUST be found in
   * capsuleRecipients(if not null)
   * 
   * @param recipient - recipient from service input.(e.g. representee to whom document is sent)
   * @param capsuleRecipients -recipient list parsed from capsule.
   * @throws DhxException throws if recipient not found. Means that document recipient if faulty
   */
  protected void checkRecipient(String recipient, List<String> capsuleRecipients)
      throws DhxException {
    log.info("Checking recipient.");
    if (recipient == null) {
      recipient = soapConfig.getMemberCode();
    }
    List<String> recipientList = new ArrayList<String>();
    List<Representee> representees = representationService.getRepresentationList();
    Date curDate = new Date();
    if (representees != null && representees.size() > 0) {
      for (Representee representee : representees) {
        if (representee.getStartDate().getTime() <= curDate.getTime()
            && (representee.getEndDate() == null || representee.getEndDate().getTime() >= curDate
                .getTime())) {
          recipientList.add(representee.getMemberCode());
        }
      }
    }
    recipientList.add(soapConfig.getMemberCode());
    if (!recipientList.contains(recipient)) {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in representativesList and own member code. recipient:"
              + recipient);
    }
    if (capsuleRecipients != null) {
      for (String capsuleRecipient : capsuleRecipients) {
        if (capsuleRecipient.equals(recipient)) {
          return;
        }
      }
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in capsule recipient list. recipient:" + recipient);
    }
    return;
  }


  /**
   * Method checks filesize againts maximum filesize.
   * 
   * @param fileToCheck - file that need to be checked
   * @throws DhxException thrown if filesize is bigger that maximum filesize
   */
  private void checkFileSize(File fileToCheck) throws DhxException {
    if (config.getCheckFileSize()) {
      log.info("Checking filesize.");
      Integer maxSize = config.getMaxFileSizeInBytes();
      log.debug("Max file size:" + maxSize + " filesize:" + fileToCheck.length());
      if (maxSize < fileToCheck.length()) {
        throw new DhxException(DhxExceptionEnum.OVER_MAX_SIZE,
            "File size is too big.  Max file size:" + config.getMaxFileSize());
      }
      return;
    } else {
      log.info("Checking filesize is disabled in configuration.");
    }
  }
}
