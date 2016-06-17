package ee.bpw.dhx.model;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XroadClient {
	
	public XroadClient (XRoadClientIdentifierType xrdClient) {
		this.xRoadInstance = xrdClient.getXRoadInstance();
		this.memberClass = xrdClient.getMemberClass();
		this.memberCode = xrdClient.getMemberCode();
		this.subsystemCode = xrdClient.getSubsystemCode();
	}
	   protected String xRoadInstance;
	   protected String memberClass;
	   protected String memberCode;
	   protected String subsystemCode;
	   
	 @Override
	 public String toString() {
		 return xRoadInstance + "/" + memberClass + "/" + memberCode + "/" + subsystemCode;
	 }

}
