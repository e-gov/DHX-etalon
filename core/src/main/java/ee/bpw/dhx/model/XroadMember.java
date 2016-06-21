package ee.bpw.dhx.model;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
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
		 return xRoadInstance + "/" + memberClass + "/" + memberCode + "/" + subsystemCode + " representative:" + representativeCode;
	 }

}
