package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.InternalRepresentee;
import ee.bpw.dhx.model.Recipient;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.CapsuleVersionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.StringUtil;
import ee.bpw.dhx.ws.config.CapsuleConfig;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.DocumentService;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
@Service("documentService")
public class DocumentServiceImpl implements DocumentService {

  @Autowired
  @Getter
  @Setter
  DhxConfig config;

  @Autowired
  @Setter
  SoapConfig soapConfig;

  @Autowired
  @Setter
  CapsuleConfig capsuleConfig;

  @Autowired
  @Setter
  AddressService addressService;

  @Autowired
  @Setter
  DhxGateway documentGateway;

  @Autowired
  @Setter
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  @Setter
  DhxImplementationSpecificService dhxImplementationSpecificService;


  @Loggable
  private SendDocumentResponse sendDocumentNoCapsulePasring(InputStream capsuleStream,
      String consignmentId, String recipient, String recipientSystem, String senderSubsystem) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    checkFileSize(capsuleStream);
    XroadMember adressee = addressService.getClientForMemberCode(recipient, recipientSystem);
    XroadMember sender = soapConfig.getDefaultClient();
    if(senderSubsystem != null) {
      sender.setSubsystemCode(senderSubsystem);
    }
    return sendDocumentToXroadMember(capsuleStream, consignmentId, adressee, sender);
  }

  @Loggable
  @Override
  public SendDocumentResponse sendDocument(InputStream capsuleStream, String consignmentId,
      XroadMember recipient) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    return sendDocument(capsuleStream, consignmentId, recipient, soapConfig.getDefaultClient());
  }
  
  @Loggable
  @Override
  public SendDocumentResponse sendDocument(InputStream capsuleStream, String consignmentId,
      XroadMember recipient, XroadMember sender) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    return sendDocumentToXroadMember(capsuleStream, consignmentId, recipient, sender);
  }

  @Loggable
  @Override
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      XroadMember recipient) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    return sendDocument(capsuleFile, consignmentId, recipient, soapConfig.getDefaultClient());
  }
  
  @Loggable
  @Override
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      XroadMember recipient, XroadMember sender) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    SendDocumentResponse response;
    try {
      response = sendDocumentToXroadMember(stream, consignmentId, recipient, soapConfig.getDefaultClient());
    } catch (DhxException ex) {
      throw ex;
    } finally {
      FileUtil.safeCloseStream(stream);
    }
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
  @Override
  @Loggable
  public SendDocumentResponse receiveDocumentFromEndpoint(SendDocument document,
      XroadMember client, XroadMember service, MessageContext context) throws DhxException {
    if (config.getCheckDuplicate()
        && dhxImplementationSpecificService.isDuplicatePackage(client,
            document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DUPLICATE_PACKAGE,
          "Already got package with this consignmentID. from:" + client.toString()
              + " consignmentId:" + document.getConsignmentId());
    } else {
      /*
       * if (!StringUtil.isNullOrEmpty(document.getRecipient())) { XroadMember member =
       * addressService.getClientForMemberCode(document.getRecipient(),
       * document.getRecipientSystem()); client.setRepresentee(member.getRepresentee()); }
       */
      if (document.getDocumentAttachment() == null) {
        throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
            "Attached capsule is not found in request");
      }
      DhxDocument dhxDocument;
      if (config.getParseCapsule()) {
        dhxDocument = extractAndValidateDocument(document, client, service);
      } else {
        dhxDocument = extractAndValidateDocumentNoParsing(document, client, service);
      }
      String id = dhxImplementationSpecificService.receiveDocument(dhxDocument, context);
      SendDocumentResponse response = new SendDocumentResponse();
      response.setReceiptId(id);
      return response;
    }
  }


  @Override
  @Loggable
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      String recipient, String recipientSystem) throws DhxException {
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    SendDocumentResponse response;
    try {
      response = sendDocumentNoCapsulePasring(stream, consignmentId, recipient, recipientSystem, null);
    } catch (DhxException ex) {
      throw ex;
    } finally {
      FileUtil.safeCloseStream(stream);
    }

    return response;
  }
  
  @Override
  @Loggable
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      String recipient, String recipientSystem, String senderSubsystem) throws DhxException {
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    SendDocumentResponse response;
    try {
      response = sendDocumentNoCapsulePasring(stream, consignmentId, recipient, recipientSystem, senderSubsystem);
    } catch (DhxException ex) {
      throw ex;
    } finally {
      FileUtil.safeCloseStream(stream);
    }

    return response;
  }

  @Override
  @Loggable
  public List<SendDocumentResponse> sendDocument(InputStream capsuleFStream,
      String consignmentId, String recipient, String recipientSystem) throws DhxException {
    if (!StringUtil.isNullOrEmpty(recipient)) {
      List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
      responses.add(sendDocumentNoCapsulePasring(capsuleFStream, consignmentId, recipient,
          recipientSystem, null));
      return responses;
    } else {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not defined in input. Unable to define recipient");
    }
  }
  
  @Override
  @Loggable
  public List<SendDocumentResponse> sendDocument(InputStream capsuleFStream,
      String consignmentId, String recipient, String recipientSystem, String senderSubsystem) throws DhxException {
    if (!StringUtil.isNullOrEmpty(recipient)) {
      List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
      responses.add(sendDocumentNoCapsulePasring(capsuleFStream, consignmentId, recipient,
          recipientSystem, senderSubsystem));
      return responses;
    } else {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not defined in input. Unable to define recipient");
    }
  }

  @Override
  @Loggable
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId)
      throws DhxException {
    return sendDocument(capsuleStream, consignmentId, capsuleConfig.getCurrentCapsuleVersion());
  }

  @Override
  @Loggable
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      CapsuleVersionEnum version) throws DhxException {
    if (version == null) {
      throw new DhxException(DhxExceptionEnum.XSD_VERSION_ERROR,
          "Unable to send document using NULL xsd version");
    }
    if (config.getParseCapsule()) {
      InputStream schemaStream = null;
      if (config.getCapsuleValidate()) {
        schemaStream = FileUtil.getFileAsStream(capsuleConfig.getXsdForVersion(version));
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
  @Loggable
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException {
    return sendDocument(capsuleFile, consignmentId, capsuleConfig.getCurrentCapsuleVersion());
  }

  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      CapsuleVersionEnum version) throws DhxException {
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
  

  @Loggable
  private SendDocumentResponse sendDocumentToXroadMember(InputStream capsuleStream,
      String consignmentId, XroadMember recipient, XroadMember sender) throws DhxException {
    log.debug("Sending document with no capsule parsing.");
    checkFileSize(capsuleStream);
    InputStream schemaStream = null;
    FileInputStream fisValidate = null;
    File file = null;
    try {
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        file = FileUtil.createFileAndWrite(capsuleStream);
        schemaStream =
            FileUtil.getFileAsStream(capsuleConfig.getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
        fisValidate = new FileInputStream(file);
        dhxMarshallerService.validate(fisValidate, schemaStream);
        capsuleStream = new FileInputStream(file);
      } else {
        log.debug("Validating capsule is disabled");
      }
      DhxDocument document = new DhxDocument(recipient, sender, capsuleStream, true);
      document.setInternalConsignmentId(consignmentId);
      SendDocumentResponse response = documentGateway.sendDocument(document);
      return response;
    } catch (FileNotFoundException ex) {
      throw new DhxException(DhxExceptionEnum.WS_ERROR,
          "Error occured while reading or writing casule file.", ex);
    } finally {
      FileUtil.safeCloseStream(capsuleStream);
      FileUtil.safeCloseStream(fisValidate);
      FileUtil.safeCloseStream(schemaStream);
      if (file != null) {
        file.delete();
      }
    }
  }



  @Loggable
  protected List<SendDocumentResponse> sendDocument(Object container, InputStream capsuleStream,
      String consignmentId) throws DhxException {
    log.debug("Sending document with capsule parsing. consignmentId: {}", consignmentId);
    checkFileSize(capsuleStream);
    List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
    List<CapsuleAdressee> adressees = capsuleConfig.getAdresseesFromContainer(container);
    if (adressees != null && adressees.size() > 0) {
      File capsuleFile = null;
      capsuleFile = dhxMarshallerService.marshall(container);
      for (CapsuleAdressee adressee : adressees) {
        // for older DVK messages, adressee might contain adressees subsystem, so split it out
        String[] adresseeParts = adressee.getAdresseeCode().split("\\.");
        String adresseeCode;
        String adresseeSystem = null;
        if (adresseeParts != null && adresseeParts.length == 2) {
          adresseeCode = adresseeParts[1];
          adresseeSystem = adresseeParts[0];
        } else {
          adresseeCode = adressee.getAdresseeCode();
        }
        XroadMember adresseeXroad =
            addressService.getClientForMemberCode(adresseeCode, adresseeSystem);
        DhxDocument document =
            new DhxDocument(adresseeXroad, soapConfig.getDefaultClient(), container, CapsuleVersionEnum.forClass(container
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
  @Loggable
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
   * @param client - X-road member who did the SOAP query.
   * @param service - X-road member who owns the SOAP service
   * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if
   *         validation is enabled
   * @throws DhxException - throws if error occured while reading or extracting file
   */
  @Loggable
  protected DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client, XroadMember service)
      throws DhxException {
    InputStream schemaStream = null;
    try {
      log.info("Receiving document. for representative: {}", document.getRecipient());
      Object container = null;
      InputStream fileStream = document.getDocumentAttachment().getInputStream();
      checkFileSize(fileStream);

      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        schemaStream =
            FileUtil.getFileAsStream(capsuleConfig.getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
      } else {
        log.debug("Validating capsule is disabled");
      }
      container = dhxMarshallerService.unmarshallAndValidate(fileStream, schemaStream);
      List<CapsuleAdressee> adressees = capsuleConfig.getAdresseesFromContainer(container);
      if (log.isDebugEnabled()) {
        for (CapsuleAdressee adressee : adressees) {
          log.debug("Document data from capsule: recipient organisationCode: {}",
              adressee.getAdresseeCode());
        }
      }
      Recipient recipient = getRecipient(document, service);
      if (config.getCheckRecipient()) {
        checkRecipient(recipient, adressees);
      }
      log.debug("Recipients from capsule checked and found in representative list "
          + "or own member code. ");
      log.info("Document received.");
      DhxDocument dhxDocument =
          new DhxDocument(client, document, container, CapsuleVersionEnum.forClass(container
              .getClass()));
      dhxDocument.setService(service);
      dhxDocument.setRecipient(recipient);
      return dhxDocument;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error while getting attachment stream. " + ex.getMessage(), ex);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not received. code: {} message: {}",
          ex.getExceptionCode(), ex.getMessage());
      throw ex;
    } finally {
      FileUtil.safeCloseStream(schemaStream);
    }
  }
  
  private Recipient getRecipient (SendDocument document, XroadMember service) {
    Recipient recipient = new Recipient(soapConfig.getDhxSubsystemPrefix());
    if (!StringUtil.isNullOrEmpty(document.getRecipient())) {
      recipient.setCode(document.getRecipient());
      recipient.setSystem(document.getRecipientSystem());
    } else {
      recipient.setCode(service.getMemberCode());
      recipient.setSystem(service.getSubsystemCode());
    }
    return recipient;
  }


  /**
   * Method extracts and validates attached document. Attachment validation is not implemented in
   * this version of service.
   * 
   * @param document - SOAP request object
   * @param client - X-road member who did the SOAP query.
   * @param service - X-road member who owns the SOAP service
   * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if
   *         validation is enabled
   * @throws DhxException - thrown if error occurs while extracting or validating document
   */
  @Loggable
  protected DhxDocument extractAndValidateDocumentNoParsing(SendDocument document,
      XroadMember client, XroadMember service) throws DhxException {
    InputStream schemaStream = null;
    try {
      log.info("Receiving document. for representative: {}", document.getRecipient());
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        schemaStream =
            FileUtil.getFileAsStream(capsuleConfig.getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
        dhxMarshallerService.validate(document.getDocumentAttachment().getInputStream(),
            schemaStream);
      } else {
        log.debug("Validating capsule is disabled");
      }
      Recipient recipient = getRecipient(document, service);
      if (config.getCheckRecipient()) {
        checkRecipient(recipient, null);
        log.info("Recipient checked and found in representative list or own member code. recipient:"
            + document.getRecipient());
      }
      DhxDocument dhxDocument = new DhxDocument(client, document);
      dhxDocument.setService(service);
      dhxDocument.setRecipient(recipient);
      log.info("Document received.");
      return dhxDocument;
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR, ex.getMessage());
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not received. code: {} message: {}",
          ex.getExceptionCode(), ex.getMessage());
      throw ex;
    } finally {
      FileUtil.safeCloseStream(schemaStream);
    }
  }

  /**
   * Checks if recipient is present in representativesList and in capsule recipients. Needed to be
   * sure if document is sent to right recipient. Recipient(if not null) MUST be found in list of
   * own memberCode or own representationList. Recipient or own member code MUST be found in
   * capsuleRecipients(if not null)
   * 
   * @param recipient - recipient from service input.(e.g. representee to whom document is sent or the direct recipient)
   * @param capsuleRecipients -recipient list parsed from capsule.
   * @throws DhxException throws if recipient not found. Means that document recipient if faulty
   */
  @Loggable
  protected void checkRecipient(Recipient recipient, List<CapsuleAdressee> capsuleRecipients)
      throws DhxException {
    log.info("Checking recipient.");
    String recipientWithSystem = null;
    /*if (StringUtil.isNullOrEmpty(recipient)) {
      recipient = soapConfig.getMemberCode();
      recipientWithSystem = recipient;
    } else {
      if (!StringUtil.isNullOrEmpty(recipientSystem)) {
        recipientWithSystem = recipientSystem + "." + recipient;
      } else {
        recipientWithSystem = recipient;
      }
    }*/
    List<Recipient> recipientList = new ArrayList<Recipient>();
    List<InternalRepresentee> representees =
        dhxImplementationSpecificService.getRepresentationList();
    Date curDate = new Date();
    if (representees != null && representees.size() > 0) {
      for (InternalRepresentee representee : representees) {
        if (representee.getStartDate().getTime() <= curDate.getTime()
            && (representee.getEndDate() == null || representee.getEndDate().getTime() >= curDate
                .getTime())) {
          
          String prefix = "";
          if (!StringUtil.isNullOrEmpty(representee.getSystem())) {
            prefix = representee.getSystem() + ".";
          }
          recipientList.add(new Recipient(representee.getMemberCode(), representee.getSystem(), soapConfig.getDhxSubsystemPrefix()));
        }
      }
    }
    for(String subSystem : soapConfig.getAcceptedSubsystemsAsList()) {
      recipientList.add(new Recipient(soapConfig.getMemberCode(), subSystem, soapConfig.getDhxSubsystemPrefix()));
    }
    if (!recipientList.contains(recipient)) {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in representativesList and own member code. recipient:"
              + recipient.toString());
    }
    if (capsuleRecipients != null) {
      for (CapsuleAdressee capsuleRecipient : capsuleRecipients) {
        // in capsule there is no system, but from adressee might come with system concatenated,
        // either with DHX prefix or not
        if (recipient.equalsToCapsuleRecipient(capsuleRecipient.getAdresseeCode())) {
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
  @Loggable
  private void checkFileSize(InputStream streamToCheck) throws DhxException {
    if (config.getCheckFilesize()) {
      log.info("Checking filesize.");
      log.info("File size check not done because check is not implemented.");
      throw new DhxException(DhxExceptionEnum.NOT_IMPLEMENTED,
          "No filesize check is implemented!");
    } else {
      log.info("Checking filesize is disabled in configuration.");
    }
  }

}
