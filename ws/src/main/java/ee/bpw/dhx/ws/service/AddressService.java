package ee.bpw.dhx.ws.service;


import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.InternalXroadMember;

import java.util.List;



/**
 * Class for creating and storing of address list.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface AddressService {


  /**
   * Method refreshes local list of addresses. Local list of adresses is refreshed using X-road
   * global conf and represenattionList service if needed
   */
  public void renewAddressList() throws DhxException;



  /**
   * Method finds xroadmember in local list of addresses by memberCode
   * 
   * @param memberCode - adressee code, might be either X-road member code or representee code.
   * @param system - adressees system. default system is NULL.
   * @return - return XroadMember object
   * @throws DhxException - thrown if recipient is not found
   */
  public InternalXroadMember getClientForMemberCode(String memberCode, String system)
      throws DhxException;

  public List<InternalXroadMember> getAdresseeList() throws DhxException;

}
