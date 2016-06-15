package ee.bpw.dhx.client.config;


import java.util.Properties;

import javax.xml.soap.SOAPException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

@EnableWs
@Configuration
public class WebServiceConfig /*extends ee.bpw.dhx.config.DhxWebServiceConfig*/{
	
	//get log4j logger to log events on custom level.
	final Logger logger = LogManager.getLogger();
	
	private class DhxClientSoapFaultTranslatorExceptionResolver extends SoapFaultMappingExceptionResolver {
		final Logger logger = LogManager.getLogger();
	    @Override
	    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
			logger.log(Level.getLevel("EVENT"), "Error occured while using SOAP service." + ex.getMessage(), ex );
	        super.customizeFault(endpoint, ex, fault);
	    }
	}
		
	@Bean
    public SoapFaultMappingExceptionResolver exceptionResolver(){
        SoapFaultMappingExceptionResolver exceptionResolver = new DhxClientSoapFaultTranslatorExceptionResolver();
        SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
        faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
        exceptionResolver.setDefaultFault(faultDefinition);

        Properties errorMappings = new Properties();
        errorMappings.setProperty(Exception.class.getName(), SoapFaultDefinition.SERVER.toString());
        errorMappings.setProperty(SOAPException.class.getName(), SoapFaultDefinition.SERVER.toString());
        exceptionResolver.setExceptionMappings(errorMappings);
        exceptionResolver.setOrder(1);
        return exceptionResolver;
    }


}
