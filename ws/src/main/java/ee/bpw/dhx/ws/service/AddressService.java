package ee.bpw.dhx.ws.service;


import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;

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
  public void renewAddressList();



  /**
   * Method finds xroadmember in local list of addresses by memberCode
   * 
   * @param memberCode - adressee code, might be either X-road member code or representee code.
   * @return - return XroadMember object
   * @throws DhxException - thrown if recipient is not found
   */
  public XroadMember getClientForMemberCode(String memberCode) throws DhxException;

  public List<XroadMember> getAdresseeList();

}
