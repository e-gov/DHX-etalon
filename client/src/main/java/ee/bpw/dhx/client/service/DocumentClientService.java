package ee.bpw.dhx.client.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.bpw.dhx.container21.DhxDocument21;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.impl.container21.DocumentServiceImpl21;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;


@Service
@Slf4j
public class DocumentClientService extends DocumentServiceImpl21 {

  @Autowired
  DhxGateway dhxGateway;

  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();

  private static List<DhxDocument> receevedDocuments = new ArrayList<DhxDocument>();

  /**
   * overriden just to log events
   */
  @Override
  public DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client)
      throws DhxException {
    try {
      logger.log(Level.getLevel("EVENT"), "Starting to receive document. for representative: "
          + document.getRecipient());
      return super.extractAndValidateDocument(document, client);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      logger.log(Level.getLevel("EVENT"), "Document is not recieved. code:" + ex.getExceptionCode()
          + " message:" + ex.getMessage());
      throw ex;
    }
  }

  /**
   * Implementation of abstract method. Saves documents to in memory list
   */
  @Override
  public String recieveDocument2_1(DhxDocument21 dhxDocument) throws DhxException {
    String receiptId = UUID.randomUUID().toString();
    logger.log(Level.getLevel("EVENT"), "Document recieved. for: "
        + dhxDocument.getClient().toString() + " receipt:" + receiptId);
    if (dhxDocument.getContainer() != null) {
      logger.log(Level.getLevel("EVENT"), "Document data from capsule: recipient organisationCode:"
          + dhxDocument.getContainer().getTransport().getDecRecipient().get(0)
              .getOrganisationCode() + " sender organisationCode:"
          + dhxDocument.getContainer().getTransport().getDecSender().getOrganisationCode());
    }
    dhxDocument.setContainer(null);
    dhxDocument.setDocumentFile(null);
    receevedDocuments.add(dhxDocument);
    return receiptId;
  }


  /***
   * Implementation of abtract method. Searches if that consignment id and that member are in the
   * list of saved documents
   */
  @Override
  public boolean isDuplicatePackage(XroadMember from, String consignmentId) {
    log.debug("Checking for duplicates. from memberCode:" + from.toString()
        + " from consignmentId:" + consignmentId);
    for (DhxDocument document : receevedDocuments) {
      if (document.getExternalConsignmentId().equals(consignmentId)
          && document.getClient().toString().equals(from.toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * override just to log events
   */
  @Override
  protected List<SendDocumentResponse> sendDocument(DecContainer container, String consignmentId)
      throws DhxException {
    try {
      return super.sendDocument(container, consignmentId);
    } catch (DhxException ex) {
      logger.log(Level.getLevel("EVENT"),
          "Error occured while sending document. " + ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * overriden to log events
   */
  @Override
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException {
    try {
      return super.sendDocument(capsuleFile, consignmentId);
    } catch (DhxException ex) {
      logger.log(Level.getLevel("EVENT"),
          "Error occured while sending document. " + ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * Overriden to log events
   * 
   * @return
   */
  @Override
  protected SendDocumentResponse sendDocumentTry(DhxDocument document) {
    SendDocumentResponse response = null;
    try {
      response = dhxGateway.sendDocument(document);
    } catch (Exception ex) {
      log.error("Error occured while sending docuemnt. " + ex.getMessage(), ex);
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
