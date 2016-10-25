package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.model.IncomingDhxPackage;
import ee.bpw.dhx.model.InternalXroadMember;

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
  public abstract boolean isDuplicatePackage(InternalXroadMember from, String consignmentId)
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
  public String receiveDocument(IncomingDhxPackage document, MessageContext context)
      throws DhxException;

  /**
   * Method returns list of representees.
   * 
   * @return List of representees that are represented by that X-road member or empty List if there
   *         are no representees.
   * @throws DhxException - thrown if error occurs
   */
  public abstract List<DhxRepresentee> getRepresentationList() throws DhxException;

  /**
   * Method returns adressees list from local storage(DB for example). Method does not renew
   * adressees list from X-road.
   * 
   * @return - adressees list from local storage
   * @throws DhxException - thrown if error occurs
   */
  public List<InternalXroadMember> getAdresseeList() throws DhxException;


  /**
   * Methods saves adressees list to local storage(DB for example). This method is called after
   * renewing adressees list from X-road.
   * 
   * @param members - list of the adressees to save
   * @throws DhxException - thrown if error occurs
   */
  public void saveAddresseeList(List<InternalXroadMember> members) throws DhxException;


  /**
   * DHX protocol requires resend logic of failed sending attempts. There is no good way to
   * implement resend logic generically inside DHX. Therefore empty method is provided, that will be
   * called by scheduler. That method must find failed documents and resend them until configured
   * resend timeout or until attempts count equals to configured maximum attempts count. Document
   * resending procedure goes frequently, therefore not every time there is a need to try to resend
   * every document, maybe additional timeout to resend documents(exponential backoff) might be
   * added.
   *
   * @throws DhxException - thrown if error occurs
   */
  public void resendFailedDocuments() throws DhxException;

}
