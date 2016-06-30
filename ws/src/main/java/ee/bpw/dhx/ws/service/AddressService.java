package ee.bpw.dhx.ws.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;

/**
 * Class for creating and storing of address list. Stores address list in memory at the moment;
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
	 * @param memberCode - adressee code, might be either X-road member code or representee code.
	 * @return - return XroadMember object
	 * @throws DhxException
	 */
	public XroadMember getClientForMemberCode(String memberCode) throws DhxException;

}
