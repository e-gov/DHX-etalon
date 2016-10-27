package ee.bpw.dhx.client.config;

import ee.bpw.dhx.ws.config.DhxConfig;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

/**
 * Class creates beans needed for web services. Those beans are meant to use only if there is no
 * other web services in application. Otherwise those beans might interfere with the ones already
 * defined.
 * 
 * @author Aleksei Kokarev
 *
 */
@Configuration
public class DhxWebServiceConfig {

  @Autowired
  DhxClientConfig config;

  /**
   * Sets servlet registration bean. Registers web services on configured path
   * 
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  @Bean
  public ServletRegistrationBean dhxMessageDispatcherServlet(
      ApplicationContext applicationContext) {
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


}
