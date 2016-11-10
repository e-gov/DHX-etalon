package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.bpw.dhx.model.DhxRecipient;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.model.DhxSendDocumentResult;
import ee.bpw.dhx.model.IncomingDhxPackage;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.model.OutgoingDhxPackage;
import ee.bpw.dhx.util.CapsuleVersionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.StringUtil;
import ee.bpw.dhx.ws.config.CapsuleConfig;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.DhxPackageService;

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
public class DhxPackageServiceImpl implements DhxPackageService {

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
      InternalXroadMember client, InternalXroadMember service, MessageContext context)
      throws DhxException {
    if (config.getCheckDuplicate()
        && dhxImplementationSpecificService.isDuplicatePackage(client,
            document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DUPLICATE_PACKAGE,
          "Already got package with this consignmentID. from:" + client.toString()
              + " consignmentId:" + document.getConsignmentId());
    } else {
      if (document.getDocumentAttachment() == null) {
        throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
            "Attached capsule is not found in request");
      }
      IncomingDhxPackage dhxDocument;
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




  @Loggable
  public DhxSendDocumentResult sendPackage(OutgoingDhxPackage outgoingPackage)
      throws DhxException {
    log.debug("Sending document.");
    SendDocumentResponse response = documentGateway.sendDocument(outgoingPackage);      
    return new DhxSendDocumentResult(outgoingPackage, response);
  }

  
  @Override
  public List<DhxSendDocumentResult> sendMultiplePackages(List<OutgoingDhxPackage> packages) {
    List<DhxSendDocumentResult> results = new ArrayList<DhxSendDocumentResult>();
    for(OutgoingDhxPackage outgoingPackage : packages ) {
      results.add(sendDocumentTry(outgoingPackage));
    }
    return results;
  }

  /**
   * Tries to send document and if error occurs, then returns response with fault, not raises
   * exception.
   * 
   * @param document - Document to try to send
   * @return service response for single recipient defined in document
   */
  @Loggable
  protected DhxSendDocumentResult sendDocumentTry(OutgoingDhxPackage document) {
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
    return new DhxSendDocumentResult(document, response);
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
  protected IncomingDhxPackage extractAndValidateDocument(SendDocument document,
      InternalXroadMember client, InternalXroadMember service)
      throws DhxException {
    InputStream schemaStream = null;
    try {
      log.info("Receiving document. for representative: {}", document.getRecipient());
      Object container = null;
      InputStream fileStream = document.getDocumentAttachment().getInputStream();
      dhxMarshallerService.checkFileSize(fileStream);

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
      log.info("Document received.");
      IncomingDhxPackage dhxDocument =
          new IncomingDhxPackage(client, service, document, container,
              CapsuleVersionEnum.forClass(container
                  .getClass()));
      if (config.getCheckRecipient()) {
        checkRecipient(dhxDocument.getRecipient(), adressees);
      }
      if(config.getCheckSender()) {
        checkSender(dhxDocument.getClient(), capsuleConfig.getSenderFromContainer(container));
      }
      log.debug("Recipients from capsule checked and found in representative list "
          + "or own member code. ");
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
  protected IncomingDhxPackage extractAndValidateDocumentNoParsing(SendDocument document,
      InternalXroadMember client, InternalXroadMember service) throws DhxException {
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
      IncomingDhxPackage dhxDocument = new IncomingDhxPackage(client, service, document);
      if (config.getCheckRecipient()) {
        checkRecipient(dhxDocument.getRecipient(), null);
        log.info("Recipient checked and found in representative list or own member code. recipient:"
            + document.getRecipient());
      }
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
   * @param recipient - recipient from service input.(e.g. representee to whom document is sent or
   *        the direct recipient)
   * @param capsuleRecipients -recipient list parsed from capsule.
   * @throws DhxException throws if recipient not found. Means that document recipient if faulty
   */
  @Loggable
  protected void checkRecipient(DhxRecipient recipient, List<CapsuleAdressee> capsuleRecipients)
      throws DhxException {
    log.info("Checking recipient.");
    List<DhxRecipient> recipientList = new ArrayList<DhxRecipient>();
    List<DhxRepresentee> representees =
        dhxImplementationSpecificService.getRepresentationList();
    Date curDate = new Date();
    if (representees != null && representees.size() > 0) {
      for (DhxRepresentee representee : representees) {
        if (representee.getStartDate().getTime() <= curDate.getTime()
            && (representee.getEndDate() == null || representee.getEndDate().getTime() >= curDate
                .getTime())) {
          recipientList
              .add(new DhxRecipient(representee.getMemberCode(), representee.getSystem()));
        }
      }
    }
    for (String subSystem : soapConfig.getAcceptedSubsystemsAsList()) {
      recipientList.add(new DhxRecipient(soapConfig.getMemberCode(), subSystem));
    }
    Boolean found = false;
    for (DhxRecipient rec : recipientList) {
      if (rec.equals(recipient, soapConfig.getDhxSubsystemPrefix())) {
        found = true;
        break;
      }
    }
    if (!found) {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in representativesList and own member code. recipient:"
              + recipient.toString());
    }
    if (capsuleRecipients != null) {
      for (CapsuleAdressee capsuleRecipient : capsuleRecipients) {
        if (recipient.equalsToCapsuleRecipient(capsuleRecipient.getAdresseeCode(),
            soapConfig.getDhxSubsystemPrefix())) {
          return;
        }
      }
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in capsule recipient list. recipient:" + recipient);
    }
    return;
  }


  /**
   * Checks if sender is defined as capsule sender. Needed to be sure if document is sent to from
   * right sender. R
   * 
   * @param client - Xroad client from service input.(e.g. representee who sent document is sent or
   *        the direct sender)
   * @param capsuleSender -sender from capsule.
   * @throws DhxException throws if sender not valid. Means that document sender if faulty
   */
  @Loggable
  protected void checkSender(InternalXroadMember client, CapsuleAdressee capsuleSender)
      throws DhxException {
    log.info("Checking sender.");
    DhxRecipient sender = new DhxRecipient();
    if (client.getRepresentee() != null) {
      sender.setCode(client.getRepresentee().getMemberCode());
      sender.setSystem(client.getRepresentee().getSystem());
    } else {
      sender.setCode(client.getMemberCode());
      sender.setSystem(client.getSubsystemCode());
    }
    if (client.getRepresentee() != null) {
      InternalXroadMember member =
          addressService
              .getClientForMemberCode(client.getMemberCode(), client.getSubsystemCode());
      if (!member.getRepresentor()) {
        throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
            "Xroad sender is representee, but client is not representor. sender:" + sender);
      }
    }
    // check that capsule sender and Xroad sender are the same
    if (sender.equalsToCapsuleRecipient(capsuleSender.getAdresseeCode(),
        soapConfig.getDhxSubsystemPrefix())) {
      return;
    }
    throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
        "Xroad sender not found in capsule. sender:" + sender);
  }




}
