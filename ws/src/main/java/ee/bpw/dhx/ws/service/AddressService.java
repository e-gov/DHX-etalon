package ee.bpw.dhx.ws.service;


import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;

import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PostConstruct;



/**
 * Class for creating and storing of address list.
 * 
 * @author Aleksei Kokarev
 *
 */
@Service
public interface AddressService {


  public List<XroadMember> getAdresseeList();

  public void setAddresseeList(List<XroadMember> members);

  @PostConstruct
  /**
   * Method refreshes local list of addresses
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

}
