 package ee.bpw.dhx.model;

import lombok.Getter;
import lombok.Setter;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.xroad.MemberType;

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
	
	public XroadMember (XroadMember member, Representee representee) {
		this.xRoadInstance = member.getXRoadInstance();
		this.memberClass = member.getMemberClass();
		this.memberCode = member.getMemberCode();
		this.subsystemCode = member.getSubsystemCode();
		this.representee = representee;
	}
	
	public XroadMember (String xRoadInstance, String memberClass, String memberCode, String subsystemCode, Representee representee) {
		this.xRoadInstance = xRoadInstance;
		this.memberClass = memberClass;
		this.memberCode = memberCode;
		this.subsystemCode = subsystemCode;
		this.representee = representee;
		
	}
	   private String xRoadInstance;
	   private String memberClass;
	   private String memberCode;
	   private String subsystemCode;
	   
	   private Representee representee;
	   
	   
	 @Override
	 public String toString() {
		 return "addressee: " + (representee==null?memberCode:representee.toString()) + ", X-road member: " + xRoadInstance + "/" + memberClass + "/" + memberCode + "/" + subsystemCode + ", is representee: " + (representee==null?false:true);
	 }

}
