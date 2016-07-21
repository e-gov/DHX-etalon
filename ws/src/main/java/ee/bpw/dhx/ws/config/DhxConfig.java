package ee.bpw.dhx.ws.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@ConfigurationProperties(prefix = "dhx")
@Configuration
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
  private String wsdlFile;
  private String endpointPath;
  private String marshallContext;
  private Integer maxFileSize;
  private String dateFormat;

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


}
