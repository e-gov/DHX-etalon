package ee.bpw.dhx.ws.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.SoapConfig;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.xroad.GlobalGroupType;
import eu.x_road.xsd.xroad.MemberType;
import eu.x_road.xsd.xroad.SharedParametersType;
import eu.x_road.xsd.xroad.SubsystemType;

/**
 * Class for creating and storing of address list. Stores address list in memory at the moment;
 * @author Aleksei Kokarev
 *
 */
@Service
@Slf4j
public class AddressService {
	
	@Autowired
	SoapConfig config;
	
	
	private List<XroadMember> members;
	
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	DhxGateway dhxGateway;
	
	@Autowired
	Unmarshaller unmarshaller;
	
	
	public AddressService () {	
	}
	
	public List<XroadMember> getAdresseeList() {	
		return members;
	}
	
	public void setAddresseeList(List<XroadMember> members){
		this.members = members;
	}
	
	@PostConstruct
	/**
	 * Method refreshes local list of addresses
	 */
	public void renewAddressList() {
		List<XroadMember> members = new ArrayList<XroadMember>();
		try{
			SharedParametersType globalConf = getGlobalConf();
			if(globalConf != null) {
				for(MemberType member : globalConf.getMember()) {
					for(SubsystemType subSystem : member.getSubsystem()) {
						if(subSystem.getSubsystemCode().equalsIgnoreCase(config.getSubsystem())) {
							log.debug("Found DHX subsystem for member " + member.getMemberCode());
							if(!member.getMemberCode().equals(config.getMemberCode())) {
								members.add(new XroadMember(config.getXroadInstance(), member, config.getSubsystem()));
								break;
							}
						}
					}
				}
				for( GlobalGroupType group : globalConf.getGlobalGroup()) {
					log.debug("group " + group.getDescription());
					if(group.getGroupCode().equals(config.getDhxRepresentationGroupName())) {
						log.debug("Found representation group");
						for(XRoadClientIdentifierType client : group.getGroupMember()) {
							//excelude own representatives
							if(!client.getMemberCode().equals(config.getMemberCode())) {
								XroadMember member  = new XroadMember(client);
								log.debug("getting representatives for member:" + member.toString());
								try {
									members.addAll(getRepresentatives(member));
								}catch(DhxException ex) {
									log.error("Error occured while getting representationList for:"  + member.toString() + ex.getMessage(), ex);
								}
							}
						}
					}
				}
			}
		}
		catch(DhxException e){
			log.error(e.getMessage(), e);
		}
		setAddresseeList(members);
	}

	
	private List<XroadMember> getRepresentatives (XroadMember member) throws DhxException{
		RepresentationListResponse response = dhxGateway.getRepresentationList(member);
		if(response == null || response.getMemberCodes().getMemberCode().size()==0) {
			return null;
		} else {
			List<XroadMember> representatives = new ArrayList<XroadMember>();
			for(String representative : response.getMemberCodes().getMemberCode()) {
				representatives.add(new XroadMember(member, representative));
			}
			return representatives;
		}
	}
	
	
	private SharedParametersType getGlobalConf() throws DhxException{
		try{
	        URL url = new URL(config.getSecurityServer() + "/" + config.getGlobalConfLocation()); 
	        log.debug("global conf URL:" + url);
	        //response.setHeader("Content-Type", "text/csv");  
	        URLConnection connection = url.openConnection();
	        InputStream stream = connection.getInputStream();
	        log.debug("got file from URL");
	        InputStream confStream = FileUtil.zipUnpack(stream, config.getGlobalConfLocation() + "/" + config.getXroadInstance() + "/" + config.getGlobalConfFilename());
	        JAXBElement<SharedParametersType> globalConfElement = XsdUtil.unmarshallCapsule(confStream, unmarshaller);
	        return globalConfElement.getValue();
	    }
	    catch (MalformedURLException e) { 
	        log.error("Error occurrred in url", e);
	        throw new DhxException(DHXExceptionEnum.TECHNICAL_ERROR, "Error occured while getting global conf " + e.getMessage(), e);

	    } 
	    catch (IOException e) { 
	    	log.error("Error occurrred ", e);
	    	throw new DhxException(DHXExceptionEnum.TECHNICAL_ERROR, "Error occured while getting global conf " + e.getMessage(), e);
	    }
	}
	
	/**
	 * Method finds xroadmember in local list of addresses by memberCode
	 * @param memberCode - adressee code, might be either X-road member code or representee code.
	 * @return - return XroadMember object
	 * @throws DhxException
	 */
	public XroadMember getClientForMemberCode(String memberCode) throws DhxException{
		List<XroadMember> members = getAdresseeList();
		for(XroadMember member : members) {
			if (member.getMemberCode().equals(memberCode) && member.getRepresentativeCode()==null){
				return member;
			} else if (member.getRepresentativeCode() != null && member.getRepresentativeCode().equals(memberCode)) {
				return member;
			}
		}
		throw new DhxException(DHXExceptionEnum.WRONG_RECIPIENT, "Recipient is not found in address list. memberCode" + memberCode);
	}

}
