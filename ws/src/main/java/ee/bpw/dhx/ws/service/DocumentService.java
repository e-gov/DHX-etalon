package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.XsdVersionEnum;

import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import java.io.File;
import java.io.InputStream;
import java.util.List;


/**
 * Interface for document sending and receiving.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface DocumentService {


  /**
   * Method should receive document(save in database for example) and return unique id of it. Id
   * will be sent as receipt in response.
   * 
   * @param document - document to receive
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public String receiveDocument(DhxDocument document) throws DhxException;


  /**
   * Send document. No capsule parsing is done and document is sent to recipient.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @pararm recipient - to whom document is sent
   * @return
   * @throws DhxException
   */
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      String recipient) throws DhxException;

  /**
   * Send document. No capsule parsing is done and document is sent to recipient. NO FILESIZE CHECK
   * IS DONE is this method. To check file size use {@link #sendDocument(File, String, String)}
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @pararm recipient - to whom document is sent
   * @return
   * @throws DhxException
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      String recipient) throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @return
   * @throws DhxException
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
   * @return
   * @throws DhxException
   */
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId,
      XsdVersionEnum version) throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule.
   * 
   * NO FILESIZE CHECK IS DONE is this method. To check file size use
   * {@link #sendDocument(File, String)}
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @return
   * @throws DhxException
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId)
      throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule.
   * 
   * NO FILESIZE CHECK IS DONE is this method. To check file size use
   * {@link #sendDocument(File, String)}
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param version - version of the capsule to parse
   * @return
   * @throws DhxException
   */
  public List<SendDocumentResponse> sendDocument(InputStream capsuleStream, String consignmentId,
      XsdVersionEnum version) throws DhxException;


  /**
   * Method searches through saved documents and checks if document with save sender and consignment
   * id exists.
   * 
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
      XroadMember client) throws DhxException;
}
