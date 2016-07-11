package ee.bpw.dhx.ws.service.impl;

import ee.bpw.dhx.exception.DhxException;

import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.util.XsdVersionEnum;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * Class for document sending and receiving. Service is independent from capsule versions that are
 * being sent or received, that means that no changes should be done in service if new capsule
 * version is added. 
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class DocumentServiceImpl implements DocumentService {

  @Autowired
  private DhxConfig config;

  @Autowired
  private SoapConfig soapConfig;

  @Autowired
  AddressService addressService;

  @Autowired
  DhxGateway documentGateway;

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  DhxImplementationSpecificService dhxImplementationSpecificService;


  private SendDocumentResponse sendDocumentNoCapsulePasring(InputStream capsuleStream,
      String consignmentId, String recipient) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    checkFileSize(capsuleStream);
    XroadMember adressee = addressService.getClientForMemberCode(recipient);
    DhxDocument document = new DhxDocument(adressee, capsuleStream, true);
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
    if (config.getCheckDuplicate()
        && dhxImplementationSpecificService.isDuplicatePackage(client,
            document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DUPLICATE_PACKAGE,
          "Already got package with this consignmentID. from:" + client.toString()
              + " consignmentId:" + document.getConsignmentId());
    } else {
      if (document.getRecipient() != null && !document.getRecipient().equals("")) {
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
      String id = dhxImplementationSpecificService.receiveDocument(dhxDocument);
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
      log.info("Receiving document. for representative: " + document.getRecipient());
      DecContainer container = null;
      InputStream fileStream = document.getDocumentAttachment().getInputStream();
      checkFileSize(fileStream);

      InputStream schemaStream = null;
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        // TODO: here need to get version from input as we dont know which version came!!!!
        schemaStream =
            FileUtil.getFileAsStream(config.getXsdForVersion(config.getCurrentCapsuleVersion()));
      } else {
        log.debug("Validating capsule is disabled");
      }
      container = dhxMarshallerService.unmarshallAndValidate(fileStream, schemaStream);
      log.info("Document data from capsule: recipient organisationCode:"
          + container.getTransport().getDecRecipient().get(0).getOrganisationCode()
          + " sender organisationCode:"
          + container.getTransport().getDecSender().getOrganisationCode());
      if (config.getCheckRecipient()) {
        checkRecipient(document.getRecipient(), getStringList(container.getTransport()
            .getDecRecipient()));
      }
      log.info("Recipient from capsule checked and found in representative list "
          + "or own member code. recipient:"
          + container.getTransport().getDecRecipient().get(0).getOrganisationCode());
      log.info("Document received.");
      DhxDocument dhxDocument =
          new DhxDocument(client, document, container, XsdVersionEnum.forClass(container
              .getClass()));
      return dhxDocument;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error while getting attachment stream. " + ex.getMessage(), ex);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not received. code:" + ex.getExceptionCode() + " message:"
          + ex.getMessage());
      throw ex;
    }
  }

  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      String recipient) throws DhxException {
    log.debug("List<SendDocumentResponse>  sendDocument(File capsuleFile, "
        + "String consignmentId, String recipient)");
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    List<SendDocumentResponse> responses;
    try {
      responses = sendDocument(stream, consignmentId, recipient);
    } catch (DhxException ex) {
      throw ex;
    } finally {
      FileUtil.safeCloseStream(stream);
    }

    return responses;
  }

  @Override
  public List<SendDocumentResponse> sendDocument(InputStream capsuleFStream,
      String consignmentId, String recipient) throws DhxException {
    log.debug("List<SendDocumentResponse>  sendDocument(InputStream capsuleFStream, "
        + "String consignmentId, String recipient)");
    if (recipient != null && !recipient.equals("")) {
      List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
      responses.add(sendDocumentNoCapsulePasring(capsuleFStream, consignmentId, recipient));
      return responses;
    } else {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not defined in input. Unable to define recipient");
    }
  }

  @Override
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId)
      throws DhxException {
    log.debug("List<SendDocumentResponse> sendDocument(InputStream capsuleStream, "
        + "String consignmentId)");
    return sendDocument(capsuleStream, consignmentId, config.getCurrentCapsuleVersion());
  }

  @Override
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      XsdVersionEnum version) throws DhxException {
    log.debug("List<SendDocumentResponse> sendDocument(InputStream capsuleStream, "
        + "String consignmentId, , XsdVersionEnum version)");
    if (config.getParseCapsule()) {
      InputStream schemaStream = null;
      if (config.getCapsuleValidate()) {
        schemaStream = FileUtil.getFileAsStream(config.getXsdForVersion(version));
      }
      Object container =
          dhxMarshallerService.unmarshallAndValidate(capsuleStream, schemaStream);
      return sendDocument(container, capsuleStream, consignmentId);
    } else {
      throw new DhxException(
          DhxExceptionEnum.WRONG_RECIPIENT,
          "Unable to define adressees without parsing capsule. "
              + "parsing capsule is disabled in configuration.");
    }
  }

  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException {
    log.debug("List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)");
    return sendDocument(capsuleFile, consignmentId, config.getCurrentCapsuleVersion());
  }

  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      XsdVersionEnum version) throws DhxException {
    log.debug("List<SendDocumentResponse>  sendDocument(File capsuleFile, String consignmentId, "
        + "XsdVersionEnum version) version=" + version.toString());
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    List<SendDocumentResponse> responses;
    try {
      responses = sendDocument(stream, consignmentId, version);
    } catch (DhxException ex) {
      throw ex;
    } finally {
      FileUtil.safeCloseStream(stream);
    }
    return responses;
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
  protected List<SendDocumentResponse> sendDocument(Object container, InputStream capsuleStream,
      String consignmentId) throws DhxException {
    log.debug("Sending document with capsule parsing. consignmentId=" + consignmentId);
    checkFileSize(capsuleStream);
    List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
    List<CapsuleAdressee> adressees = XsdUtil.getAdresseesFromContainer(container);
    if (adressees != null && adressees.size() > 0) {
      // TODO: think of some method not to marshall object, but to use original file and just gzip
      // it. problem is that stream is already read at this point and not able to read it again
      File capsuleFile = null;
      capsuleFile = dhxMarshallerService.marshall(container);
      for (CapsuleAdressee adressee : adressees) {
        XroadMember adresseeXroad =
            addressService.getClientForMemberCode(adressee.getAdresseeCode());
        DhxDocument document =
            new DhxDocument(adresseeXroad, container, XsdVersionEnum.forClass(container
                .getClass()), capsuleFile/* capsuleStream */, consignmentId, true);
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
      log.info("Receiving document. for representative: " + document.getRecipient());
      if (config.getCapsuleValidate()) {
        throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
            "Capsule validation is not implemented. Use service precific for container version");

      } else {
        log.debug("Validating capsule is disabled");
      }
      if (config.getCheckRecipient()) {
        checkRecipient(document.getRecipient(), null);
        log.info("Recipient checked and found in representative list or own member code. recipient:"
            + document.getRecipient());
      }
      DhxDocument dhxDocument = new DhxDocument(client, document);
      log.info("Document received.");
      return dhxDocument;
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not received. code:" + ex.getExceptionCode() + " message:"
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
    if (recipient == null || recipient.equals("")) {
      recipient = soapConfig.getMemberCode();
    }
    List<String> recipientList = new ArrayList<String>();
    List<Representee> representees = dhxImplementationSpecificService.getRepresentationList();
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
   * Method checks filesize againts maximum filesize. NOT IMPLEMENTED!
   * 
   * @param streamToCheck - stream that needs to be checked
   * @throws DhxException thrown if filesize is bigger that maximum filesize
   */
  private void checkFileSize(InputStream streamToCheck) throws DhxException {
    if (config.isCheckFilesize()) {
      log.info("Checking filesize.");
      log.info("File size check not done because check is not implemented.");
      throw new DhxException(DhxExceptionEnum.NOT_IMPLEMENTED,
          "No filesize check is implemented!");
      /*
       * Integer maxSize = config.getMaxFileSizeInBytes(); log.debug("Max file size:" + maxSize +
       * " filesize:" + fileToCheck.length()); if (maxSize < fileToCheck.length()) { throw new
       * DhxException(DhxExceptionEnum.OVER_MAX_SIZE, "File size is too big.  Max file size:" +
       * config.getMaxFileSize()); } return;
       */
    } else {
      log.info("Checking filesize is disabled in configuration.");
    }
  }

}
