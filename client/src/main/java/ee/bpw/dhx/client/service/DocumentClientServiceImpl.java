package ee.bpw.dhx.client.service;

import ee.bpw.dhx.client.config.DhxClientConfig;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.IncomingDhxPackage;
import ee.bpw.dhx.model.OutgoingDhxPackage;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
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
import java.util.ArrayList;
import java.util.List;


/**
 * Extension of DocumentServiceImpl. Contains changes needed for client application. e.g. event
 * logging and abstract method implementations.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Service
public class DocumentClientServiceImpl extends DocumentServiceImpl {

  @Autowired
  DhxGateway dhxGateway;

  @Autowired
  DhxClientConfig clientConfig;

  @Autowired
  DhxConfig config;

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();

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
    log.debug("sending document. capsuleType= {} recipientString= {} consignmentId= {}",
        capsuleType, recipientString, consignmentId);
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
            (DecContainer) dhxMarshallerService.unmarshall(capsuleFile);
        container.getTransport().getDecRecipient()
            .removeAll(container.getTransport().getDecRecipient());
        DecRecipient recipient = new DecRecipient();
        recipient.setOrganisationCode(recipientString);
        container.getTransport().getDecRecipient().add(recipient);
        capsuleFile = dhxMarshallerService.marshall(container);
      }
      if (config.getParseCapsule() && !capsuleType.equals("wrongAdressee")) {
        return sendDocument(capsuleFile, consignmentId);
      } else {
        String recipientCode = null;
        String recipientSystem = null;
        String[] parts = recipientString.split("\\.");
        if (parts.length == 2) {
          recipientCode = parts[1];
          recipientSystem = parts[0];
        } else if (parts.length == 3) { // system with DHX. prefix example(DHX.south.10560025)
          recipientCode = parts[2];
          recipientSystem = parts[0] + "." + parts[1];
        } else {
          recipientCode = recipientString;
        }
        List<SendDocumentResponse> responses = new ArrayList<SendDocumentResponse>();
        responses.add(sendDocument(capsuleFile, consignmentId, recipientCode, recipientSystem));
        return responses;
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
  public IncomingDhxPackage extractAndValidateDocument(SendDocument document, InternalXroadMember client, InternalXroadMember service)
      throws DhxException {
    try {
      logger.log(Level.getLevel("EVENT"), "Starting to receive document. for representative: "
          + document.getRecipient() + " consignmentId: " + document.getConsignmentId());
      return super.extractAndValidateDocument(document, client, service);
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
   * Overriden to log events.
   */
  @Override
  protected SendDocumentResponse sendDocumentTry(OutgoingDhxPackage document) {
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
