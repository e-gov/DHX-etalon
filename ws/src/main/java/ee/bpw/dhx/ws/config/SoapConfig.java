package ee.bpw.dhx.ws.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix="soap")
@Configuration
public class SoapConfig {
		
	String targetnamespace;
	String securityServer;
	String xroadInstance;
	String memberClass;
	String memberCode;
	String subsystem;
	String userId;
	String protocolVersion;
	
	
	String serviceXroadInstance;
	String serviceMemberClass;
	String serviceSubsystem;
	String serviceCode;
	String serviceVersion;

}
