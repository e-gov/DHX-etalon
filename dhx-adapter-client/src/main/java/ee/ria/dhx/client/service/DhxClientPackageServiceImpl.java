package ee.ria.dhx.client.service;

import ee.ria.dhx.client.config.DhxClientConfig;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.OrganisationType;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Recipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecSender;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.ws.service.impl.DhxGateway;
import ee.ria.dhx.ws.service.impl.DhxPackageServiceImpl;

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
public class DhxClientPackageServiceImpl extends DhxPackageServiceImpl {

  @Autowired
  DhxGateway dhxGateway;

  @Autowired
  DhxClientConfig clientConfig;

  @Autowired
  DhxConfig config;

  @Autowired
  SoapConfig soapConfig;

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  DhxPackageProviderService dhxPackageProviderService;

  @Autowired
  AsyncDhxPackageService asyncDhxPackageService;

  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();

  private String getMemberCodeForCapsule(String recipientString) {
    String memberCode;
    if (recipientString.startsWith(soapConfig.getDhxSubsystemPrefix() + ".")) {
      recipientString =
          recipientString.substring((soapConfig.getDhxSubsystemPrefix() + ".").length());
    }

    String[] split = recipientString.split(":");
    if (split.length == 2) {
      /*
       * if(!split[0].equals(soapConfig.getDhxSubsystemPrefix())) { memberCode = split[0]; } else {
       */
      memberCode = split[1];
      // }
    } else {
      memberCode = recipientString;
    }
    return memberCode;
  }

  /**
   * Method to serve UI needs. It replaces capsule adressees if needed.
   * 
   * @param capsuleType - type of the capsule (e.g. correct, invalid etc.)
   * @param recipientString - recipient of the capsule
   * @param consignmentId - consignment ID to set while sending document
   * @return - sendDocument service responsese
   * @throws DhxException - thrown if error occurs while sending document
   */
  public List<DhxSendDocumentResult> sendDocument(String capsuleType, String recipientString,
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
      if (capsuleType.equals("correct")) {
        DecContainer container =
            (DecContainer) dhxMarshallerService.unmarshall(capsuleFile);
        container.getTransport().getDecRecipient()
            .removeAll(container.getTransport().getDecRecipient());
        DecRecipient decRecipient = new DecRecipient();
        decRecipient.setOrganisationCode(getMemberCodeForCapsule(recipientString));
        container.getTransport().getDecRecipient().add(decRecipient);
        DecSender sender = new DecSender();
        sender.setOrganisationCode(soapConfig.getMemberCode());
        container.getTransport().setDecSender(sender);
        container.getRecipient().removeAll(container.getRecipient());
        Recipient recipient = new Recipient();
        OrganisationType org  = new OrganisationType();
        org.setName("Ettevõtte nimi");
        org.setOrganisationCode(getMemberCodeForCapsule(recipientString));
        recipient.setOrganisation(org);
        container.getRecipient().add(recipient);
        capsuleFile = dhxMarshallerService.marshall(container);
        // container.getDecMetadata().setDecId(null);
      }
      /*
       * if (config.getParseCapsule() && !capsuleType.equals("wrongAdressee")) { return
       * sendMultiplePackages(dhxPackageProviderService.getOutgoingPackage(capsuleFile,
       * consignmentId)); } else {
       */
      String recipientCode = null;
      String recipientSystem = null;
      String[] parts = recipientString.split(":");
      if (parts.length == 2) {
        recipientCode = parts[1];
        recipientSystem = parts[0];
      } else {
        recipientCode = recipientString;
      }
      List<DhxSendDocumentResult> responses = new ArrayList<DhxSendDocumentResult>();
      OutgoingDhxPackage pck = dhxPackageProviderService.getOutgoingPackage(capsuleFile,
        consignmentId, recipientCode, recipientSystem);
      responses.add(sendPackage(pck));
      // asyncDhxPackageService.sendPackage(dhxPackageProviderService.getOutgoingPackage(capsuleFile,
      // consignmentId, recipientCode, recipientSystem));

      return responses;
      // }

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
  public IncomingDhxPackage extractAndValidateDocument(SendDocument document,
      InternalXroadMember client, InternalXroadMember service)
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
  protected DhxSendDocumentResult sendDocumentTry(OutgoingDhxPackage document) {
    DhxSendDocumentResult response = null;
    try {
      response = sendPackage(document);
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
      SendDocumentResponse docResponse = new SendDocumentResponse();
      Fault fault = new Fault();
      fault.setFaultCode(faultCode.getCodeForService());
      fault.setFaultString(ex.getMessage());
      docResponse.setFault(fault);
      return new DhxSendDocumentResult(document, docResponse);
    }
    return response;
  }


}
