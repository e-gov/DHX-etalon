package ee.bpw.dhx.ws.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;

@Service
public class AddressService {
	
	@Autowired
	SoapConfig config;
	
	private List<XroadMember> members;
	
	public AddressService () {		
	}
	
	public List<XroadMember> getAdresseeList() {	
		if(members == null) {
			members = new ArrayList<XroadMember>();
			members.add(new XroadMember(config.getXroadInstance(), config.getMemberClass(), config.getMemberCode(), config.getSubsystem(), null));
			members.add(new XroadMember(config.getXroadInstance(), "COM", "30000001", config.getSubsystem(), "10560026"));
			members.add(new XroadMember(config.getXroadInstance(), "COM", "30000001", config.getSubsystem(), "10560025"));
			members.add(new XroadMember(config.getXroadInstance(), "GOV", "40000001", config.getSubsystem(), null));
		}
		return members;
	}
	
	public XroadMember getClientForMemberCode(String memberCode) throws DhxException{
		List<XroadMember> members = getAdresseeList();
		for(XroadMember member : members) {
			if (member.getMemberCode().equals(memberCode)){
				return member;
			} else if (member.getRepresentativeCode() != null && member.getRepresentativeCode().equals(memberCode)) {
				return member;
			}
		}
		throw new DhxException(DHXExceptionEnum.WRONG_RECIPIENT, "Recipient is not found in address list. memberCode" + memberCode);
	}

}
