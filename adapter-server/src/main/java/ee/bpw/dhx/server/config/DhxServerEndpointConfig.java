package ee.bpw.dhx.server.config;


import ee.bpw.dhx.ws.config.DhxConfig;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

import java.util.ArrayList;
import java.util.List;


/**
 * Creates beans needed for DHX webservices.
 * 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Configuration
@ComponentScan(basePackages = "ee.bpw.dhx.server.endpoint")
public class DhxServerEndpointConfig extends WsConfigurationSupport {

  @Autowired
  DhxConfig config;

 
  /**
   * Defines WSDL.
   * 
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhxServer")
  public SimpleWsdl11Definition defaultWsdl11Definition() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFile());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }

}
