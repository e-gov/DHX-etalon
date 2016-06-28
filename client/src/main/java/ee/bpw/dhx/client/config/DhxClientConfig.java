package ee.bpw.dhx.client.config;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.bpw.dhx.client.service.DocumentClientService;
import ee.bpw.dhx.client.service.DocumentGateWayClient;
import ee.bpw.dhx.client.service.RepresentationServiceImpl;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix="dhx.client")
@Slf4j
public class DhxClientConfig {
	
	private String  representatives;
	private Integer logMaxSize;
	private Integer logRefresh;
	private String  capsuleTestFile;
	//private String  jobRecipient;
	private Integer binaryBufferSize;
	private String name;
	private String info;
		
	
	List<String> representativesList = null;
	
	public List<String> getRepresentativesList() {
		if(representativesList == null) {
			representativesList = Arrays.asList(representatives.split(","));
		}
		return representativesList;
	}
	
	@Bean
	RepresentationService representationService()
	{
	    return new RepresentationServiceImpl();
	}
	
	@Bean
	DocumentService documentService()
	{
		DocumentService service = new DocumentClientService();
	    return service;
	}
	
	@Bean 
	DhxGateway dhxGateway() {
		return new DocumentGateWayClient();
	}

}
