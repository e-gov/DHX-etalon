package ee.bpw.dhx.ws.config;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@ConfigurationProperties(prefix = "dhx")
@Configuration
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
  private Boolean parseCapsule = true;
  private String marshallContext;
  private Integer maxFileSize;
  private String dateFormat;

  private String wsdlFile;
  private String endpointPath;

  private Jaxb2Marshaller dhxJaxb2Marshaller;

  private String[] marshallContextAsList;

  /**
   * Method return marhllaing context as list.
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
