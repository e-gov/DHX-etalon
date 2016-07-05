package ee.bpw.dhx.client.service;

import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.impl.DocumentServiceImpl;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Extension of DocumentServiceImpl. Contains changes needed for client application. e.g. event
 * logging and abstract method implementations.
 * 
 * @author Aleksei Kokarev
 *
 */
@Service
@Slf4j
public class DocumentClientServiceImpl extends DocumentServiceImpl {

  @Autowired
  DhxGateway dhxGateway;

  @Autowired
  DhxClientConfig clientConfig;

  @Autowired
  DhxConfig config;

  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();

  private static List<DhxDocument> receevedDocuments = new ArrayList<DhxDocument>();

  /**
   * Method to serve UI needs. It replaces capsule adressees if needed.
   * 
   * @param capsuleType - type of the capsule (e.g. correct, invalid etc.)
   * @param recipientString - recipient of the capsule
   * @param consignmentId - consignment ID to set while sending document
   * @return - sendDocument service responsese
   * @throws DhxException - thrown if error occurs while sending document
   */
  public List<SendDocumentResponse> sendDocument(String capsuleType, String recipientString,
      String consignmentId) throws DhxException {
    log.debug("sending document. capsuleType=" + capsuleType + " recipientString="
        + recipientString + " consignmentId=" + consignmentId);
    try {
      String capsuleFilePath = "";
      switch (capsuleType) {
        case "correct":
          capsuleFilePath = clientConfig.getCapsuleCorrect();
          break;
        case "invalid":
          capsuleFilePath = clientConfig.getCapsuleInvalid();
          break;
        case "notxml":
          capsuleFilePath = clientConfig.getCapsuleNotxml();
          break;
        case "wrongAdressee":
          capsuleFilePath = clientConfig.getCapsuleWrongAdressee();
          break;
        default:
          break;
      }
      File capsuleFile = FileUtil.getFile(capsuleFilePath);
      // if we want to send to wrong adressee , then wont change the capsule
      if (!capsuleType.equals("wrongAdressee")) {
        DecContainer container =
            (DecContainer) XsdUtil.unmarshallCapsule(capsuleFile, unmarshaller);
        container.getTransport().getDecRecipient()
            .removeAll(container.getTransport().getDecRecipient());
        DecRecipient recipient = new DecRecipient();
        recipient.setOrganisationCode(recipientString);
        container.getTransport().getDecRecipient().add(recipient);
        capsuleFile = XsdUtil.marshallCapsule(container, super.marshaller);
      }
      if (config.getParseCapsule()) {
        return sendDocument(capsuleFile, consignmentId);
      } else {
        return sendDocument(capsuleFile, consignmentId, recipientString);
      }

    } catch (DhxException ex) {
      logger.log(Level.getLevel("EVENT"),
          "Error occured while sending document. " + ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * overriden just to log events.
   */
  @Override
  public DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client)
      throws DhxException {
    try {
      logger.log(Level.getLevel("EVENT"), "Starting to receive document. for representative: "
          + document.getRecipient() + " consignmentId: " + document.getConsignmentId());
      return super.extractAndValidateDocument(document, client);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      logger.log(
          Level.getLevel("EVENT"),
          "Document is not received. code:" + ex.getExceptionCode() + " message:"
              + ex.getMessage());
      throw ex;
    }
  }

  /**
   * Implementation of abstract method. Saves documents to in memory list
   */
  @Override
  public String receiveDocument(DhxDocument dhxDocument) throws DhxException {
    String receiptId = UUID.randomUUID().toString();
    logger.log(Level.getLevel("EVENT"), "Document received. for: "
        + dhxDocument.getClient().toString() + " receipt:" + receiptId + " consignmentId: "
        + dhxDocument.getExternalConsignmentId());
    if (dhxDocument.getParsedContainer() != null) {
      DecContainer container = (DecContainer) dhxDocument.getParsedContainer();
      logger.log(Level.getLevel("EVENT"),
          "Document data from capsule: recipient organisationCode:"
              + container.getTransport().getDecRecipient().get(0).getOrganisationCode()
              + " sender organisationCode:"
              + container.getTransport().getDecSender().getOrganisationCode());
    }
    dhxDocument.setParsedContainer(null);
    dhxDocument.setDocumentFile(null);
    receevedDocuments.add(dhxDocument);
    return receiptId;
  }


  /**
   * Implementation of abtract method. Searches if that consignment id and that member are in the.
   * list of saved documents
   */
  @Override
  public boolean isDuplicatePackage(XroadMember from, String consignmentId) {
    log.debug("Checking for duplicates. from memberCode:" + from.toString()
        + " from consignmentId:" + consignmentId);
    if (receevedDocuments != null && receevedDocuments.size() > 0) {
      for (DhxDocument document : receevedDocuments) {
        if (document.getExternalConsignmentId() != null
            && document.getExternalConsignmentId().equals(consignmentId)
            && document.getClient().toString().equals(from.toString())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * override just to log events.
   */
  /*
   * @Override protected List<SendDocumentResponse> sendDocument(Object container, InputStream
   * capsuleStream, String consignmentId) throws DhxException { try { return
   * super.sendDocument(container, capsuleStream, consignmentId); } catch (DhxException ex) {
   * logger.log(Level.getLevel("EVENT"), "Error occured while sending document. " + ex.getMessage(),
   * ex); throw ex; } }
   * 
   * /** overriden to log events.
   */
  /*
   * @Override public List<SendDocumentResponse> sendDocument(File capsuleFile, String
   * consignmentId, String recipient) throws DhxException { try { return
   * super.sendDocument(capsuleFile, consignmentId, recipient); } catch (DhxException ex) {
   * logger.log(Level.getLevel("EVENT"), "Error occured while sending document. " + ex.getMessage(),
   * ex); throw ex; } }
   */

  /**
   * Overriden to log events.
   */
  @Override
  protected SendDocumentResponse sendDocumentTry(DhxDocument document) {
    SendDocumentResponse response = null;
    try {
      response = dhxGateway.sendDocument(document);
    } catch (Exception ex) {
      log.error("Error occured while sending document. " + ex.getMessage(), ex);
      logger.log(Level.getLevel("EVENT"),
          "Error occured while sending document. " + ex.getMessage(), ex);
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


}
