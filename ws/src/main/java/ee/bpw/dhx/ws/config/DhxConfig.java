package ee.bpw.dhx.ws.config;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

@Getter
@Setter
// @ConfigurationProperties(prefix = "dhx")
@Configuration
@PropertySource("classpath:dhx-application.properties")
@Slf4j
/**
 * Main configuration of DHX webservice application
 * @author Aleksei Kokarev
 *
 */
public class DhxConfig {

  private final String marshallContextSeparator = ":";

  private Boolean capsuleValidate = true;
  private Boolean checkRecipient = true;
  private Boolean checkFilesize = false;
  private Boolean checkDuplicate = true;
  private Boolean parseCapsule = false;
  private String marshallContext =
      "ee.riik.schemas.deccontainer.vers_2_1:eu.x_road.dhx.producer:eu.x_road.xsd.identifiers"
          + ":eu.x_road.xsd.representation:eu.x_road.xsd.xroad";
  private Integer maxFileSize = 100; // in MB
  private String dateFormat;

  private String wsdlFile = "dhx.wsdl";
  private String endpointPath = "ws";

  private Jaxb2Marshaller dhxJaxb2Marshaller;

  private String[] marshallContextAsList;

  @Autowired
  Environment env;

  /**
   * Automatically initialize properties.
   */
  @PostConstruct
  public void init() {
    if (env.getProperty("casule-validate") != null) {
      setCapsuleValidate(Boolean
          .parseBoolean(env.getProperty(env.getProperty("casule-validate"))));
    }

    if (env.getProperty("dhx.capsule-validate") != null) {
      setCapsuleValidate(Boolean.parseBoolean(env.getProperty("dhx.capsule-validate")));
    }
    if (env.getProperty("dhx.check-recipient") != null) {
      setCheckRecipient(Boolean.parseBoolean(env.getProperty("dhx.check-recipient")));
    }
    if (env.getProperty("dhx.check-filesize") != null) {
      setCheckFilesize(Boolean.parseBoolean(env.getProperty("dhx.check-filesize")));
    }
    if (env.getProperty("dhx.check-duplicate") != null) {
      setCheckDuplicate(Boolean.parseBoolean(env.getProperty("dhx.check-duplicate")));
    }

    if (env.getProperty("dhx.parse-capsule") != null) {
      setParseCapsule(Boolean.parseBoolean(env.getProperty("dhx.parse-capsule")));
    }
    if (env.getProperty("dhx.marshall-context") != null) {
      setMarshallContext(env.getProperty("dhx.marshall-context"));
    }

    if (env.getProperty("dhx.marshall-context") != null) {
      setMarshallContext(env.getProperty("dhx.marshall-context"));
    }

    if (env.getProperty("dhx.max-file-size") != null) {
      setMaxFileSize(Integer.parseInt(env.getProperty("dhx.max-file-size")));
    }

    if (env.getProperty("dhx.date-format") != null) {
      setDateFormat(env.getProperty("dhx.date-format"));
    }

    if (env.getProperty("dhx.endpoint-path") != null) {
      setEndpointPath(env.getProperty("dhx.endpoint-path"));
    }

    if (env.getProperty("dhx.wsdl-file") != null) {
      setWsdlFile(env.getProperty("dhx.wsdl-file"));
    }

  }

  /**
   * Method return marshalling context as list.
   * 
   * @return array of package names for marshaller
   */
  public String[] getMarshallContextAsList() {
    if (marshallContextAsList == null) {
      String[] contextArray = marshallContext.split(marshallContextSeparator);
      marshallContextAsList = contextArray;
    }
    return marshallContextAsList;
  }

  public Integer getMaxFileSizeInBytes() {
    return maxFileSize * 1024 * 1024;
  }

  public String format(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    return sdf.format(date);
  }

  /**
   * Sets marshaller bean.
   * 
   * @return marshaller
   */
  @Bean
  public Jaxb2Marshaller getDhxJaxb2Marshaller() {
    if (this.dhxJaxb2Marshaller == null) {
      dhxJaxb2Marshaller = new Jaxb2Marshaller();
      dhxJaxb2Marshaller.setMtomEnabled(true);
      dhxJaxb2Marshaller.setContextPaths(getMarshallContextAsList());
    }
    return dhxJaxb2Marshaller;
  }


}
