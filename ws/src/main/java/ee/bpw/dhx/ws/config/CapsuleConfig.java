package ee.bpw.dhx.ws.config;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.bpw.dhx.util.CapsuleVersionEnum;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "dhx.xsd")
@Configuration
public class CapsuleConfig {
  
  private String capsuleXsdFile21;
  //private String xsdFile;
  private CapsuleVersionEnum currentCapsuleVersion;
  

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
  public String getXsdForVersion(CapsuleVersionEnum version) throws DhxException {
    if(version==null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
        "Unable to find XSD file for given verion. version: null");
    }
    switch (version) {
      case V21:
        return getCapsuleXsdFile21();
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find XSD file for given verion. version:" + version.toString());
    }
  }
  

  /**
   * Method to find adresssees from container. Method must return adressees for every existing
   * version of the container, bacause service which uses that method does not know anything about
   * container and just needs adressees defined in it. Given implementation is able to find
   * adressees for capsule version 2.1
   * 
   * @param containerObject - container(capsule) object from which to find adressees
   * @return - list of the adresssees
   * @throws DhxException - thrown adressees parsing is not defined for given object (capsule
   *         version)
   */
  public List<CapsuleAdressee> getAdresseesFromContainer(Object containerObject)
      throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
    switch (version) {
      case V21:
        List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
        DecContainer container = (DecContainer) containerObject;
        if (container != null && container.getTransport() != null
            && container.getTransport().getDecRecipient() != null
            && container.getTransport().getDecRecipient().size() > 0) {
          for (DecRecipient recipient : container.getTransport().getDecRecipient()) {
            adressees.add(new CapsuleAdressee(recipient.getOrganisationCode()));
          }
          return adressees;
        }
        return null;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find XSD file for given verion. version:" + version.toString());
    }
  }

}
