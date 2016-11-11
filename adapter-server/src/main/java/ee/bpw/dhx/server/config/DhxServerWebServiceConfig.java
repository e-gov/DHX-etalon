package ee.bpw.dhx.server.config;

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
public class DhxServerWebServiceConfig {


  /**
   * Sets servlet registration bean. Registers web services on configured path
   * 
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  /*@Bean(name = "dhxServlet")
  public ServletRegistrationBean dhxMessageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    servlet.setContextConfigLocation("ee.bpw.dhx.ws.beanconfig.DhxEndpointConfig");
    return new ServletRegistrationBean(servlet, "/" + "ws" + "/*");
  }*/
  
  /**
   * Sets servlet registration bean. Registers web services on configured path
   * 
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  @Bean(name = "dispatcherServlet")
  public ServletRegistrationBean dhxServerMessageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    servlet.setContextConfigLocation("ee.bpw.dhx.server.config.DhxServerEndpointConfig");
    return new ServletRegistrationBean(servlet, "/" + "wsServer" + "/*");
  }


}