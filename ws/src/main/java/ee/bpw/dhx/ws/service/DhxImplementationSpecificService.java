package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.InternalRepresentee;
import ee.bpw.dhx.model.XroadMember;

import org.springframework.ws.context.MessageContext;

import java.util.List;

/**
 * Interface which declares methods that are needed for DHX web services to work, but those methods
 * are not implemented by DHX ws itself.
 * 
 * @author Aleksei Kokarev
 *
 */

public interface DhxImplementationSpecificService {


  /**
   * Method searches through saved documents and checks if document with same sender and consignment
   * id exists.
   * 
   * 
   * @param from - document sender to check
   * @param consignmentId - consignment id to check
   * @return - true if document with same sender and consignment id exists, otherwise false
   * @throws DhxException - thrown if error occurs
   */
  public abstract boolean isDuplicatePackage(XroadMember from, String consignmentId)
      throws DhxException;

  /**
   * Method should receive document(save in database for example) and return unique id of it. Id
   * will be sent as receipt in response.
   * 
   * @param document - document to receive
   * @param context - if something is missing in document, then there is opportunity to take it from
   *        context
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public String receiveDocument(DhxDocument document, MessageContext context) throws DhxException;

  /**
   * Method returns list of representees.
   * 
   * @return List of representees that are represented by that X-road member or empty List if there
   *         are no representees.
   * @throws DhxException - thrown if error occurs
   */
  public abstract List<InternalRepresentee> getRepresentationList() throws DhxException;

  /**
   * Method returns adressees list from local storage(DB for example). Method does not renew
   * adressees list from X-road.
   * 
   * @return - adressees list from local storage
   * @throws DhxException - thrown if error occurs
   */
  public List<XroadMember> getAdresseeList() throws DhxException;


  /**
   * Methods saves adressees list to local storage(DB for example). This method is called after
   * renewing adressees list from X-road.
   * 
   * @param members - list of the adressees to save
   * @throws DhxException - thrown if error occurs
   */
  public void saveAddresseeList(List<XroadMember> members) throws DhxException;

}
