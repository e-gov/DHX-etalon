package ee.bpw.dhx.client.config;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.bpw.dhx.client.service.DocumentClientService;
import ee.bpw.dhx.client.service.RepresentationServiceImpl;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.RepresentationService;

@Configuration
@Getter
@Setter
@Slf4j
public class DhxClientConfig {
	
	
	@Value("${dhx.representatives}")
	private String representatives;
	
	@Value("${log.max.size}")
	private Integer logMaxSize;
	
	@Value("${log.refresh}")
	private Integer logRefresh;
	
	
	@Value("${capsule.test.file}")
	private String capsuleTestFile;
		
	
	List<String> representativesList = null;
	
	public List<String> getRepresentativesList() {
		if(representativesList == null) {
			representativesList = Arrays.asList(representatives.split(","));
		}
		return representativesList;
	}
	
	/*@Bean
	DhxConfig getDhxConfig(){
		return new DhxConfig();
	}
	
	@Bean
	DhxWebServiceConfig getDhxWebServiceConfig(){
		return new DhxWebServiceConfig();
	}*/
	
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

}
