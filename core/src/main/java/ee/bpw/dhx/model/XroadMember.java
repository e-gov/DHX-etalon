 package ee.bpw.dhx.model;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.xroad.MemberType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XroadMember {
	
	public XroadMember (XRoadClientIdentifierType xrdClient) {
		this.xRoadInstance = xrdClient.getXRoadInstance();
		this.memberClass = xrdClient.getMemberClass();
		this.memberCode = xrdClient.getMemberCode();
		this.subsystemCode = xrdClient.getSubsystemCode();
	}
	
	public XroadMember (String xroadInstance, MemberType member, String subsytemCode) {
		this.xRoadInstance = xroadInstance;
		this.memberClass = member.getMemberClass().getCode();
		this.memberCode = member.getMemberCode();
		this.subsystemCode = subsytemCode;
	}
	
	public XroadMember (XroadMember member, String representativeCode) {
		this.xRoadInstance = member.getXRoadInstance();
		this.memberClass = member.getMemberClass();
		this.memberCode = member.getMemberCode();
		this.subsystemCode = member.getSubsystemCode();
		this.representativeCode = representativeCode;
	}
	
	public XroadMember (String xRoadInstance, String memberClass, String memberCode, String subsystemCode, String representativeCode) {
		this.xRoadInstance = xRoadInstance;
		this.memberClass = memberClass;
		this.memberCode = memberCode;
		this.subsystemCode = subsystemCode;
		this.representativeCode = representativeCode;
		
	}
	   private String xRoadInstance;
	   private String memberClass;
	   private String memberCode;
	   private String subsystemCode;
	   
	   private String representativeCode;
	   
	   
	 @Override
	 public String toString() {
		 return "addressee: " + (representativeCode==null?memberCode:representativeCode) + ", X-road member: " + xRoadInstance + "/" + memberClass + "/" + memberCode + "/" + subsystemCode + ", is representee: " + (representativeCode==null?false:true);
	 }

}
