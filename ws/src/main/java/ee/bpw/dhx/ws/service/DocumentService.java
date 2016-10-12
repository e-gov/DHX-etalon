package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.CapsuleVersionEnum;

import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import org.springframework.ws.context.MessageContext;

import java.io.File;
import java.io.InputStream;
import java.util.List;


/**
 * Interface for document sending and receiving. Service must be independent from capsule versions
 * that are being sent or received, that means that no changes should be done in service if new
 * capsule version is added.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface DocumentService {


  /**
   * Send document. No capsule parsing is done and document is sent to recipient.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      XroadMember recipient) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient. X-road sender is
   * put from sender parameter. Use this method if sender differs from the one configured as
   * default(for example if sending from non default subsystem or if representee is sending the
   * document).
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @param sender - xroadMember representing sender of the document. Can be used if sender differs
   *        from the one from configuration or for example to send document from another(different
   *        from default configured) subsystem
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      XroadMember recipient, XroadMember sender) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient. Sender parameter
   * will be put as X-road sender. Use this method if sender differs from the one configured as
   * default(for example if sending from non default subsystem or if representee is sending the
   * document).
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @param sender - xroadMember representing sender of the document. Can be used if sender differs
   *        from the one from configuration or for example to send document from another(different
   *        from default configured) subsystem
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(InputStream capsuleStream, String consignmentId,
      XroadMember recipient, XroadMember sender) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(InputStream capsuleStream, String consignmentId,
      XroadMember recipient) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      String recipientCode, String recipientSystem) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient. senderSubsystem
   * represents X-road subSystem which will used to send the document. Use this method if sending
   * from same X-road member as configured but from non default subsystem.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public SendDocumentResponse sendDocument(File capsuleFile, String consignmentId,
      String recipientCode, String recipientSystem, String senderSubsystem) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      String recipientCode, String recipientSystem) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient. senderSubsystem
   * represents X-road subSystem which will used to send the document. Use this method if sending
   * from same X-road member as configured but from non default subsystem.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      String recipientCode, String recipientSystem, String senderSubsystem) throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated - DHX protocol does not have to open the capsule at all. Use version without
   *             capsule opening and define single recipient
   *             {@link #sendDocument(File, String, String, String)}
   */
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
      throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param version - version of the capsule to parse
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated - DHX protocol does not have to open the capsule at all. Use version without
   *             capsule opening and define single recipient
   *             {@link #sendDocument(File, String, String, String)}
   */
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      CapsuleVersionEnum version) throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule. NO FILESIZE CHECK IS
   * DONE is this method. To check file size use {@link #sendDocument(File, String)}
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated - DHX protocol does not have to open the capsule at all. Use version without
   *             capsule opening and define single recipient
   *             {@link #sendDocument(InputStream, String, String, String)}
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId)
      throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule. NO FILESIZE CHECK IS
   * DONE is this method. To check file size use {@link #sendDocument(File, String)}
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param version - version of the capsule to parse
   * @return - sendDocument service responses
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated - DHX protocol does not have to open the capsule at all. Use version without
   *             capsule opening and define single recipient
   *             {@link #sendDocument(InputStream, String, String, String)}
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      CapsuleVersionEnum version) throws DhxException;


  /**
   * Method is used by endpoint. Is called when document arrives to endpoint.
   * 
   * @param document - service iniput parameters. document to receive
   * @param client - SOAP message client(who sent the request).
   * @param service - SOAP message service(to whom was request sent).
   * @return service response
   * @param context - SOAP message context. If something is missing in parsed objects, then take
   *        them from context
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public SendDocumentResponse receiveDocumentFromEndpoint(SendDocument document,
      XroadMember client, XroadMember service, MessageContext context) throws DhxException;
}
