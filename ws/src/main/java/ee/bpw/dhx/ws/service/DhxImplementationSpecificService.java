package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;

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
   */
  public abstract boolean isDuplicatePackage(XroadMember from, String consignmentId);

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
   * Method returns list of representees.
   * 
   * @return List of representees that are represented by that X-road member or empty List if there
   *         are no representees.
   */
  public abstract List<Representee> getRepresentationList();

  /**
   * Method returns adressees list from local storage(DB for example). Method does not renew
   * adressees list from X-road.
   * 
   * @return - adressees list from local storage
   */
  public List<XroadMember> getAdresseeList();


  /**
   * Methods saves adressees list to local storage(DB for example). This method is called after
   * renewing adressees list from X-road.
   * 
   * @param members -
   */
  public void saveAddresseeList(List<XroadMember> members);

}
