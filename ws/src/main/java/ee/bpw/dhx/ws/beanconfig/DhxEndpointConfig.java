package ee.bpw.dhx.ws.beanconfig;


import ee.bpw.dhx.ws.config.DhxConfig;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

import java.util.ArrayList;
import java.util.List;

@Getter
@Configuration
/**
 * Creates beans needed for DHX webservices.
 * @author Aleksei Kokarev
 *
 */
public class DhxEndpointConfig {

  @Autowired
  DhxConfig config;

  /**
   * Injects DefaultMethodEndpointAdapter which supports SOAP message attachments. Sets proper
   * marshaller. That bean might iterfere with another same bean if it is defined(in that case most
   * probably code need to be changed to define single bean which will staisfy both needs).
   * 
   * @return DefaultMethodEndpointAdapter
   */
  @Bean
  public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
    DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
    List<MethodArgumentResolver> argumentResolvers = adapter.getMethodArgumentResolvers();
    List<MethodReturnValueHandler> returnValueHandlers =
        adapter.getCustomMethodReturnValueHandlers();
    if (argumentResolvers == null) {
      argumentResolvers = new ArrayList<MethodArgumentResolver>();
    }
    if (returnValueHandlers == null) {
      returnValueHandlers = new ArrayList<MethodReturnValueHandler>();
    }
    returnValueHandlers.addAll(methodProcessors());
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
  public List<MarshallingPayloadMethodProcessor> methodProcessors() {
    List<MarshallingPayloadMethodProcessor> retVal =
        new ArrayList<MarshallingPayloadMethodProcessor>();
    Jaxb2Marshaller marshallerMtom = config.getDhxJaxb2Marshaller();
    retVal.add(new MarshallingPayloadMethodProcessor(marshallerMtom));

    return retVal;
  }

}
