package ee.bpw.dhx.client.config;

import ee.bpw.dhx.client.service.AddressClientServiceImpl;
import ee.bpw.dhx.client.service.DhxClientGateWay;
import ee.bpw.dhx.client.service.DocumentClientServiceImpl;
import ee.bpw.dhx.client.service.RepresentationServiceImpl;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.RepresentationService;
import ee.bpw.dhx.ws.service.impl.DhxMarshallerServiceImpl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Configuration of client application.
 * 
 * @author Aleksei Kokarev
 *
 */
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "dhx.client")
@Slf4j
public class DhxClientConfig {

  private String representees;
  private Integer logMaxSize;
  private Integer logRefresh;
  // private String capsuleTestFile;
  // private String jobRecipient;
  private Integer binaryBufferSize;
  private String name;
  // private String info;

  /*
   * private String testFile1; private String testFile2;
   * 
   * private String sendDocumentHelp; private String representationListHelp; private String
   * logEventsHelp; private String logRefreshHelp; private String representativesHelp; private
   * String validateCapsuleHelp; private String securityServerHelp; private String xroadMemberHelp;
   * private String maxFileSizeHelp;
   */

  private String capsuleCorrect;
  private String capsuleInvalid;
  private String capsuleNotxml;
  private String capsuleWrongAdressee;

  private String capsuleAddressateSelect;
  private String capsuleSelect;

  List<String> representeesList = null;

  /**
   * Returns list of representatives from configuration.
   * 
   * @return - list of representatives
   */
  public List<String> getRepresenteesList() {
    if (representeesList == null && representees != null && !representees.equals("")) {
      representeesList = Arrays.asList(representees.split(","));
    }
    return representeesList;
  }

  public List<Map<String, String>> getCapsuleSelect() {
    return getSelect(capsuleSelect);
  }

  public List<Map<String, String>> getCapsuleAddressateSelect() {
    return getSelect(capsuleAddressateSelect);
  }

  private List<Map<String, String>> getSelect(String selectString) {
    List<Map<String, String>> select = new ArrayList<Map<String, String>>();
    Map<String, String> row = null;
    for (String part : selectString.split(";")) {
      if (row == null) {
        row = new HashMap<String, String>();
        row.put("name", part);
      } else if (row.get("value") == null) {
        row.put("value", part);
        select.add(row);
        row = null;
      }
    }
    return select;
  }

  @Bean
  RepresentationService representationService() {
    return new RepresentationServiceImpl();
  }

  @Bean
  DocumentClientServiceImpl documentClientServiceImpl() {
    DocumentClientServiceImpl service = new DocumentClientServiceImpl();
    return service;
  }

  @Bean
  AddressService addressService() {
    AddressService service = new AddressClientServiceImpl();
    return service;
  }

  @Bean
  DhxMarshallerService dhxMarshallerService() {
    return new DhxMarshallerServiceImpl();
  }

  @Bean
  DhxGateway dhxGateway() {
    return new DhxClientGateWay();
  }

  @Bean
  DhxConfig dhxConfig() {
    return new DhxConfig();
  }

  @Bean
  SoapConfig soapConfig() {
    return new SoapConfig();
  }

  @Bean
  /**
   * Creates localeResolver
   * @return localeResolver
   */
  public LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(new Locale("et"));
    return resolver;
  }

  @Bean
  /**
   * Creates messageSource
   * @return messageSource
   */
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:i18/messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(true);
    return messageSource;
  }


}
