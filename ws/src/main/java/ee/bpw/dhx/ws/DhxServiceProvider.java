package ee.bpw.dhx.ws;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.CapsuleVersionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.CapsuleConfig;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.bpw.dhx.ws.service.impl.AddressServiceImpl;
import ee.bpw.dhx.ws.service.impl.DhxMarshallerServiceImpl;
import ee.bpw.dhx.ws.service.impl.DocumentServiceImpl;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

/**
 * If application is not using spring, then provide services as manually initialized objects.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class DhxServiceProvider {

  private static DhxConfig dhxConfig;
  private static SoapConfig soapConfig;
  private static CapsuleConfig capsuleConfig;

  private static AddressService addressService;
  private static DhxGateway dhxGateway;
  private static DhxMarshallerService dhxMarshallerService;
  private static DocumentService documentService;
  private static DhxImplementationSpecificService dhxImplementationSpecificService;

  private static Properties configFile;


  @Loggable
  public static void init(String configFileName,
      Class<DhxImplementationSpecificService> dhxSpecificService) throws IOException,
      DhxException, JAXBException, SOAPException, InstantiationException, IllegalAccessException {
    log.debug("starting to initialize services");
    configFile = new Properties();
    InputStream propertyFile = null;
    try {
      propertyFile = FileUtil.getFileAsStream(configFileName);
      configFile.load(propertyFile);
      getDhxConfig();
      getSoapConfig();
      getCapsuleConfig();
      getAddressService(dhxSpecificService);
      getDhxGateway();
      getDhxMarshallerService();
      getDocumentService(dhxSpecificService);
      getDhxImplementationSpecificService(dhxSpecificService);
    } catch (IOException | DhxException ex) {
      log.error(ex.getMessage(), ex);
      configFile = null;
      throw ex;
    } finally {
      FileUtil.safeCloseStream(propertyFile);
      propertyFile = null;
    }

  }

  public static DhxConfig getDhxConfig() {
    if (dhxConfig == null) {
      dhxConfig = new DhxConfig();
      if (configFile.getProperty("dhx.capsule-validate") != null) {
        dhxConfig.setCapsuleValidate(Boolean.parseBoolean(configFile
            .getProperty("dhx.capsule-validate")));
      }
      if (configFile.getProperty("dhx.check-recipient") != null) {
        dhxConfig.setCheckRecipient(Boolean.parseBoolean(configFile
            .getProperty("dhx.check-recipient")));
      }
      if (configFile.getProperty("dhx.check-filesize") != null) {
        dhxConfig.setCheckFilesize(Boolean.parseBoolean(configFile
            .getProperty("dhx.check-filesize")));
      }
      if (configFile.getProperty("dhx.check-duplicate") != null) {
        dhxConfig.setCheckDuplicate(Boolean.parseBoolean(configFile
            .getProperty("dhx.check-duplicate")));
      }

      if (configFile.getProperty("dhx.parse-capsule") != null) {
        dhxConfig.setParseCapsule(Boolean.parseBoolean(configFile
            .getProperty("dhx.parse-capsule")));
      }
      if (configFile.getProperty("dhx.marshall-context") != null) {
        dhxConfig.setMarshallContext(configFile.getProperty("dhx.marshall-context"));
      }

      if (configFile.getProperty("dhx.marshall-context") != null) {
        dhxConfig.setMarshallContext(configFile.getProperty("dhx.marshall-context"));
      }

      if (configFile.getProperty("dhx.max-file-size") != null) {
        dhxConfig.setMaxFileSize(Integer.parseInt(configFile.getProperty("dhx.max-file-size")));
      }

      if (configFile.getProperty("dhx.date-format") != null) {
        dhxConfig.setDateFormat(configFile.getProperty("dhx.date-format"));
      }

      if (configFile.getProperty("dhx.endpoint-path") != null) {
        dhxConfig.setEndpointPath(configFile.getProperty("dhx.endpoint-path"));
      }

      if (configFile.getProperty("dhx.wsdl-file") != null) {
        dhxConfig.setWsdlFile(configFile.getProperty("dhx.wsdl-file"));
      }
    }
    return dhxConfig;
  }

  public static SoapConfig getSoapConfig() {
    if (soapConfig == null) {
      soapConfig = new SoapConfig();
      if (configFile.getProperty("soap.targetnamespace") != null) {
        soapConfig.setTargetnamespace(configFile.getProperty("soap.targetnamespace"));
      }
      if (configFile.getProperty("soap.security-server") != null) {
        soapConfig.setSecurityServer(configFile.getProperty("soap.security-server"));
      }
      if (configFile.getProperty("soap.security-server-appender") != null) {
        soapConfig.setSecurityServerAppender(configFile
            .getProperty("soap.security-server-appender"));
      }
      if (configFile.getProperty("soap.xroad-instance") != null) {
        soapConfig.setXroadInstance(configFile.getProperty("soap.xroad-instance"));
      }
      if (configFile.getProperty("soap.member-class") != null) {
        soapConfig.setMemberClass(configFile.getProperty("soap.member-class"));
      }
      if (configFile.getProperty("soap.subsystem") != null) {
        soapConfig.setSubsystem(configFile.getProperty("soap.subsystem"));
      }
      if (configFile.getProperty("soap.user-id") != null) {
        soapConfig.setUserId(configFile.getProperty("soap.user-id"));
      }
      if (configFile.getProperty("soap.protocol-version") != null) {
        soapConfig.setProtocolVersion(configFile.getProperty("soap.protocol-version"));
      }
      if (configFile.getProperty("soap.member-code") != null) {
        soapConfig.setMemberCode(configFile.getProperty("soap.member-code"));
      }
      if (configFile.getProperty("soap.global-conf-location") != null) {
        soapConfig.setGlobalConfLocation(configFile.getProperty("soap.global-conf-location"));
      }
      if (configFile.getProperty("soap.global-conf-filename") != null) {
        soapConfig.setGlobalConfFilename(configFile.getProperty("soap.global-conf-filename"));
      }
      if (configFile.getProperty("soap.dhx-representation-group-name") != null) {
        soapConfig.setDhxRepresentationGroupName(configFile
            .getProperty("soap.dhx-representation-group-name"));
      }
      if (configFile.getProperty("soap.service-xroad-instance") != null) {
        soapConfig.setServiceXroadInstance(configFile.getProperty("soap.service-xroad-instance"));
      }
      if (configFile.getProperty("soap.service-subsystem") != null) {
        soapConfig.setServiceSubsystem(configFile.getProperty("soap.service-subsystem"));
      }
      if (configFile.getProperty("soap.send-document-service-code") != null) {
        soapConfig.setSendDocumentServiceCode(configFile
            .getProperty("soap.send-document-service-code"));
      }
      if (configFile.getProperty("soap.representatives-service-code") != null) {
        soapConfig.setRepresentativesServiceCode(configFile
            .getProperty("soap.representatives-service-code"));
      }
      if (configFile.getProperty("soap.send-document-service-version") != null) {
        soapConfig.setSendDocumentServiceVersion(configFile
            .getProperty("soap.send-document-service-version"));
      }
      if (configFile.getProperty("soap.representatives-service-version") != null) {
        soapConfig.setRepresentativesServiceVersion(configFile
            .getProperty("soap.representatives-service-version"));
      }
      if (configFile.getProperty("soap.connection-timeout") != null) {
        soapConfig.setConnectionTimeout(Integer.parseInt(configFile
            .getProperty("soap.connection-timeout")));
      }
      if (configFile.getProperty("soap.read-timeout") != null) {
        soapConfig.setReadTimeout(Integer.parseInt(configFile.getProperty("soap.read-timeout")));
      }

    }
    return soapConfig;
  }

  public static CapsuleConfig getCapsuleConfig() {
    if (capsuleConfig == null) {
      capsuleConfig = new CapsuleConfig();
      if (configFile.getProperty("dhx.xsd.capsule-xsd-file21") != null) {
        capsuleConfig.setCapsuleXsdFile21(configFile.getProperty("dhx.xsd.capsule-xsd-file21"));
      }
      if (configFile.getProperty("dhx.xsd.current-capsule-version") != null) {
        capsuleConfig.setCurrentCapsuleVersion(CapsuleVersionEnum.valueOf(configFile
            .getProperty("dhx.xsd.current-capsule-version")));
      }
    }
    return capsuleConfig;
  }


  public static AddressService getAddressService(
      Class<DhxImplementationSpecificService> dhxSpecificService) throws JAXBException,
      SOAPException, InstantiationException, IllegalAccessException {
    if (addressService == null) {
      // init
      AddressServiceImpl addressServiceImpl = new AddressServiceImpl();
      addressServiceImpl.setConfig(getSoapConfig());
      addressServiceImpl.setDhxGateway(getDhxGateway());
      addressServiceImpl.setDhxMarshallerService(getDhxMarshallerService());
      addressServiceImpl.setDocumentService(getDocumentService(dhxSpecificService));
      addressServiceImpl
          .setDhxImplementationSpecificService(
          getDhxImplementationSpecificService(dhxSpecificService)
          );
      addressServiceImpl.init();
      addressService = addressServiceImpl;
    }
    return addressService;
  }

  public static DhxGateway getDhxGateway() throws JAXBException, SOAPException {
    if (dhxGateway == null) {
      dhxGateway = new DhxGateway();
      dhxGateway.setConfig(getDhxConfig());
      dhxGateway.setSoapConfig(getSoapConfig());
      dhxGateway.setDhxMarshallerService(getDhxMarshallerService());
      dhxGateway.init();
    }
    return dhxGateway;
  }

  public static DhxMarshallerService getDhxMarshallerService() throws JAXBException {
    if (dhxMarshallerService == null) {
      DhxMarshallerServiceImpl dhxMarshallerServiceImpl = new DhxMarshallerServiceImpl();
      dhxMarshallerServiceImpl.setConfig(getDhxConfig());
      dhxMarshallerServiceImpl.init();
      dhxMarshallerService = dhxMarshallerServiceImpl;
    }
    return dhxMarshallerService;
  }

  public static DocumentService getDocumentService(
      Class<DhxImplementationSpecificService> dhxSpecificService) throws JAXBException,
      SOAPException, IllegalAccessException, InstantiationException {
    if (documentService == null) {
      DocumentServiceImpl documentServiceImpl = new DocumentServiceImpl();
      documentServiceImpl.setCapsuleConfig(getCapsuleConfig());
      documentServiceImpl.setConfig(getDhxConfig());
      documentServiceImpl
          .setDhxImplementationSpecificService(
          getDhxImplementationSpecificService(dhxSpecificService)
          );
      documentServiceImpl.setDocumentGateway(getDhxGateway());
      documentServiceImpl.setSoapConfig(getSoapConfig());
      documentService = documentServiceImpl;
      documentServiceImpl.setAddressService(getAddressService(dhxSpecificService));
    }
    return documentService;
  }

  public static DhxImplementationSpecificService getDhxImplementationSpecificService(
      Class<DhxImplementationSpecificService> dhxSpecificService) throws IllegalAccessException,
      InstantiationException {
    if (dhxImplementationSpecificService == null) {
      DhxImplementationSpecificService exampleDhxImplementationSpecificService =
          dhxSpecificService.newInstance();
      dhxImplementationSpecificService = exampleDhxImplementationSpecificService;
    }
    return dhxImplementationSpecificService;
  }

}
