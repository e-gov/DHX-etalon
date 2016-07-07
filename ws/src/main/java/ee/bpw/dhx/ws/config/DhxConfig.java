package ee.bpw.dhx.ws.config;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.util.XsdVersionEnum;

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
  private Boolean checkFileSize = false;
  private Boolean checkDuplicate = true;
  private Boolean parseCapsule = true;
  private String capsuleXsdFile21;
  private String xsdFile;
  private String wsdlFile;
  private String endpointPath;
  private String sendRetryCount;
  private String marshallContext;
  private Integer maxFileSize;
  private String dateFormat;
  private XsdVersionEnum currentCapsuleVersion;

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

  public String getCurrentXsd() throws DhxException {
    return getXsdForVersion(currentCapsuleVersion);
  }

  /**
   * Method finds config parameter which contains link to XSD for given version.
   * 
   * @param version - version for which to find XSD
   * @return - link to XSD file for given version
   * @throws DhxException - thrown then no XSD file is defined for given version
   */
  public String getXsdForVersion(XsdVersionEnum version) throws DhxException {
    switch (version) {
      case V21:
        return getCapsuleXsdFile21();
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find XSD file for given verion. version:" + version.toString());
    }
  }
}
