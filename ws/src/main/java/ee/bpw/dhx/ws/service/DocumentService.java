package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;

import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import java.io.File;
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
  public String recieveDocument(DhxDocument document) throws DhxException;

  /**
   * Method should send document to all recipients defined in capsule. Should be implemented in
   * service which has information about version of the capsule
   * 
   * @param capsuleFile - file cantaining capsule to send
   * @param consignmentId - consignment id to set when sending file.
   * @return service responses for each recipient
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public List<SendDocumentResponse> sendDocument(File capsuleFile, String consignmentId)
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
      XroadMember client) throws DhxException;
}
