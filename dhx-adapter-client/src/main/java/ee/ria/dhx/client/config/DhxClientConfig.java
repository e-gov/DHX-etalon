package ee.ria.dhx.client.config;

import ee.ria.dhx.client.service.DhxClientSpecificService;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.impl.DhxMarshallerServiceImpl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource("classpath:dhx-client.properties")
@Slf4j
public class DhxClientConfig {

  private String representees;
  private String representeesNames;
  private Integer logMaxSize;
  private Integer logRefresh;
  private Integer binaryBufferSize;
  private String name;

  private String capsuleCorrect;
  private String capsuleInvalid;
  private String capsuleNotxml;
  private String capsuleWrongAdressee;

  private String capsuleAddressateSelect;
  private String capsuleAddressateSelectRepresentation;
  private String capsuleSelect;

  List<String> representeesList = null;
  List<String> representeesNamesList = null;


  private String endpointPath = "ws";

  /**
   * Returns list of representatives from configuration.
   * 
   * @return - list of representatives
   */
  public List<String> getRepresenteesList() {
    if (representeesList == null && !StringUtil.isNullOrEmpty(representees)) {
      representeesList = Arrays.asList(representees.split(","));
    }
    return representeesList;
  }

  /**
   * Returns list of representatives names from configuration.
   * 
   * @return - list of representatives names
   */
  public List<String> getRepresenteesNamesList() {
    if (representeesNamesList == null && !StringUtil.isNullOrEmpty(representeesNames)) {
      representeesNamesList = Arrays.asList(representeesNames.split(","));
    }
    return representeesNamesList;
  }

  public List<Map<String, String>> getCapsuleSelect() {
    return getSelect(capsuleSelect);
  }

  public List<Map<String, String>> getCapsuleAddressateSelect() {
    return getSelect(capsuleAddressateSelect);
  }

  public List<Map<String, String>> getCapsuleAddressateSelectRepresentation() {
    return getSelect(capsuleAddressateSelectRepresentation);
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
  DhxImplementationSpecificService dhxImplementationSpecificService() {
    return new DhxClientSpecificService();
  }

  @Bean
  DhxMarshallerService dhxMarshallerService() {
    return new DhxMarshallerServiceImpl();
  }

  @Bean
  LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(new Locale("et"));
    return resolver;
  }


  @Bean
  MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:i18/messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

}
