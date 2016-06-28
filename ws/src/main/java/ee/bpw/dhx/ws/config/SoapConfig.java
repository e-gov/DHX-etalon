package ee.bpw.dhx.ws.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix="soap")
@Configuration
/**
 * Configuration parameters needed for SOAP services.
 * @author Aleksei Kokarev
 *
 */
public class SoapConfig {
		
	String targetnamespace;
	String securityServer;
	String securityServerAppender;
	String xroadInstance;
	String memberClass;
	String memberCode;
	String subsystem;
	String userId;
	String protocolVersion;
	
	String globalConfLocation;
	String globalConfFilename;
	String dhxRepresentationGroupName;
	
	
	String serviceXroadInstance;
	String serviceMemberClass;
	String serviceSubsystem;
	
	
	String sendDocumentServiceCode;
	String sendDocumentServiceVersion;
	String representativesServiceCode;
	String representativesServiceVersion;
	
	Integer connectionTimeout;
	Integer readTimeout;

	
	
	public String getSecurityServerWithAppender() {
		return securityServer+securityServerAppender;
	}

}
