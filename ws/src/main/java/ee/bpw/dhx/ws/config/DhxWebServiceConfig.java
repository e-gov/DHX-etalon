package ee.bpw.dhx.ws.config;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@EnableWs
@Getter
@Slf4j
@Configuration
/**
 * Creates beans needed for DHX werservices
 * @author Aleksei Kokarev
 *
 */
public class DhxWebServiceConfig extends WsConfigurationSupport {

  @Autowired
  DhxConfig config;

  /**
   * Sets servlet registration bean.
   * 
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  @Bean
  public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean(servlet, "/" + config.getEndpointPath() + "/*");
  }

  /**
   * Defines WSDL.
   * 
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhx")
  public SimpleWsdl11Definition defaultWsdl11Definition() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFile());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }

  @Bean
  @Override
  public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
    List<MethodReturnValueHandler> returnValueHandlers =
        new ArrayList<MethodReturnValueHandler>();
    returnValueHandlers.addAll(methodProcessors());

    DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
    List<MethodArgumentResolver> argumentResolvers = adapter.getMethodArgumentResolvers();
    if (argumentResolvers == null) {
      argumentResolvers = new ArrayList<MethodArgumentResolver>();
    }
    argumentResolvers.addAll(methodProcessors());
    argumentResolvers.add(new MessageContextMethodArgumentResolver());
    adapter.setMethodArgumentResolvers(argumentResolvers);
    adapter.setMethodReturnValueHandlers(returnValueHandlers);
    return adapter;
  }


  /**
   * Method returns bean List of MarshallingPayloadMethodProcessors.
   * 
   * @return bean List of MarshallingPayloadMethodProcessors
   */
  @Bean
  public List<MarshallingPayloadMethodProcessor> methodProcessors() {
    List<MarshallingPayloadMethodProcessor> retVal =
        new ArrayList<MarshallingPayloadMethodProcessor>();
    Jaxb2Marshaller marshallerMtom = marshaller();
    retVal.add(new MarshallingPayloadMethodProcessor(marshallerMtom));

    return retVal;
  }

  /**
   * Sets marshaller bean.
   * 
   * @return marshaller
   */
  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setMtomEnabled(true);
    marshaller.setContextPaths(config.getMarshallContextAsList());
    return marshaller;
  }

  /**
   * sets unmarshaller bean.
   * 
   * @return unmarshaller
   */
  @Bean
  public Unmarshaller getUnmarshaller() {
    try {
      JAXBContext unmarshalContext = JAXBContext.newInstance(config.getMarshallContext());
      Unmarshaller unmarshaller = unmarshalContext.createUnmarshaller();
      return unmarshaller;
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }


}
