package ee.bpw.dhx.ws.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix="dhx")
@Configuration
public class DhxConfig {
	
	private Boolean capsuleValidate;
	private String  capsuleXsdFile;
	private String xsdFile;
	private String wsdlFile;
	private String endpointPath;
	private String sendRetryCount;

}
