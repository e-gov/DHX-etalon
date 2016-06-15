package ee.bpw.dhx.ws.config;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

@EnableWs
@Getter
@Slf4j
@Configuration
public class DhxWebServiceConfig extends WsConfigurationSupport {

	@Autowired
	DhxConfig config;
	
	@Bean
	public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean(servlet, "/" + config.getEndpointPath() + "/*");
	}

	@Bean(name = "dhx")
	public SimpleWsdl11Definition defaultWsdl11Definition() {
		Resource wsdlResource = new ClassPathResource(config.getWsdlFile());
		SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
		return wsdlDef;
	}
	
	@Bean
    @Override
    public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
		
        List<MethodArgumentResolver> argumentResolvers = new ArrayList<MethodArgumentResolver>();
        argumentResolvers.addAll(methodProcessors());

        List<MethodReturnValueHandler> returnValueHandlers = new ArrayList<MethodReturnValueHandler>();
        returnValueHandlers.addAll(methodProcessors());

        DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
        adapter.setMethodArgumentResolvers(argumentResolvers);
        adapter.setMethodReturnValueHandlers(returnValueHandlers);
        return adapter;
    }

    @Bean
    public List<MarshallingPayloadMethodProcessor> methodProcessors() {
        List<MarshallingPayloadMethodProcessor> retVal = new ArrayList<MarshallingPayloadMethodProcessor>();
        Jaxb2Marshaller marshallerMTOM =marshaller();
        retVal.add(new MarshallingPayloadMethodProcessor(marshallerMTOM));

        return retVal;
    }
    
	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setMtomEnabled(true);
		marshaller.setContextPaths("eu.x_road.dhx.producer", "eu.x_road.xsd.identifiers", "eu.x_road.xsd.xroad");
		return marshaller;
	}
    
    @Bean
	public Unmarshaller getUnmarshaller() {
    	try {
	    	JAXBContext unmarshalContext = JAXBContext.newInstance("ee.riik.schemas.deccontainer.vers_2_1");
			Unmarshaller unmarshaller = unmarshalContext.createUnmarshaller();
			return unmarshaller;
    	} catch(JAXBException ex) {
    		log.error(ex.getMessage(), ex);
    	}
    	return null;
	}
    

}
