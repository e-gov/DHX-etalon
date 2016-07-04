package ee.bpw.dhx.util;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum XsdVersionEnum {

  V21(DecContainer.class);

  private XsdVersionEnum(Class<? extends Object> containerClass) {
    this.containerClass = containerClass;
  }

  private Class<? extends Object> containerClass;

  public Class<? extends Object> getContainerClass() {
    return containerClass;
  }

  public static XsdVersionEnum forClass(Class<? extends Object> containerClass)
      throws DhxException {
    if (containerClass != null) {
      for (XsdVersionEnum version : XsdVersionEnum.values()) {
        if (containerClass.equals(version.getContainerClass())) {
          log.debug("Found XSD version for class. containerClass:" + containerClass.toString()
              + " version:" + version.toString());
          return version;
        }
      }
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Unknown class. No XSD version is found for that class. Class:"
              + containerClass.toString());
    }
    return null;
  }

}
