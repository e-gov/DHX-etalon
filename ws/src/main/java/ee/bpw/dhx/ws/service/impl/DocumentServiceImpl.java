package ee.bpw.dhx.ws.service.impl;

import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;

import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.Unmarshaller;



/**
 * Generic class for document sending and receiving. Does not parse capsule neither validates
 * capsule, because does not contain information about container.
 * capsule - xml file containing document and metadata about the document
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
  Unmarshaller unmarshaller;

  @Autowired
  Jaxb2Marshaller jaxMarshaller;

  @Autowired
  AddressServiceImpl addressService;

  @Autowired
  DhxGateway documentGateway;


  /**
   * Method should receive document(save in database for example) and return unique id of it. Id
   * will be sent as receipt in response.
   * 
   * @param document - document to receive
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public abstract String recieveDocument(DhxDocument document) throws DhxException;

  /**
   * Method should send document to all recipients defined in capsule. Should be implemented in
   * service which has information about version of the capsule
   * 
   * @param capsuleFile - file cantaining capsule to send
   * @param consignmentId - consignment id to set when sending file.
   * @return service responses for each recipient
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public abstract List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException;

  /**
   * Method searches through saved documents and checks if document with save sender and consignment
   * id exists.
   * 
   * @param from - document sender to check
   * @param consignmentId - consignment id to check
   * @return - true if document with same sender and consignment id exists, otherwise false
   */
  public abstract boolean isDuplicatePackage(XroadMember from, String consignmentId);


  /**
   * Method is used by endpoint. Is called when document arrives to endpoint.
   * 
   * @param document - service iniput parameters. document to receive
   * @param client - SOAP message client(who sent the request).
   * @return service response
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public SendDocumentResponse receiveDocumentFromEndpoint(SendDocument document,
      XroadMember client) throws DhxException {
    // XroadMember client = documentGateway.getXroadCLientAndSetRersponseHeader(messageContext);

    if (isDuplicatePackage(client, document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DUPLICATE_PACKAGE,
          "Already got package with this consignmentID. from:" + client.toString()
              + " consignmentId:" + document.getConsignmentId());
    } else {
      if (document.getRecipient() != null) {
        XroadMember member = addressService.getClientForMemberCode(document.getRecipient());
        client.setRepresentee(member.getRepresentee());
      }
      DhxDocument dhxDocument = extractAndValidateDocument(document, client);
      dhxDocument.setClient(client);
      String id = recieveDocument(dhxDocument);
      SendDocumentResponse response = new SendDocumentResponse();
      response.setReceiptId(id);
      return response;
    }
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
  protected DhxDocument extractAndValidateDocument(SendDocument document, XroadMember client)
      throws DhxException {
    try {
      log.info("Recieving document. for representative: " + document.getRecipient());
      // File unpacked = FileUtil.extractAndUnpackAttachment(dhxDocument.getDocumentFile());
      if (config.getCapsuleValidate()) {
        throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
            "Capsule validation is not implemented. Use service precific for container version");

      } else {
        log.debug("Validating capsule is disabled");
      }
      checkRecipient(document.getRecipient(), null);
      log.info("Recipient checked and found in representative list or own member code. recipient:"
          + document.getRecipient());
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
  protected void checkFileSize(File fileToCheck) throws DhxException {
    Integer maxSize = config.getMaxFileSizeInBytes();
    log.debug("Max file size:" + maxSize + " filesize:" + fileToCheck.length());
    if (maxSize < fileToCheck.length()) {
      throw new DhxException(DhxExceptionEnum.OVER_MAX_SIZE,
          "File size is too big.  Max file size:" + config.getMaxFileSize());
    }
    return;
  }
}
