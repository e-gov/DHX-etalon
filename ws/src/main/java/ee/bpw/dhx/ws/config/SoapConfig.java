package ee.bpw.dhx.ws.config;

import ee.bpw.dhx.model.XroadMember;

import eu.x_road.xsd.identifiers.ObjectFactory;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

@Getter
@Setter
// @ConfigurationProperties(prefix = "soap")
@Component
@PropertySource("classpath:dhx-application.properties")
/**
 * Configuration parameters needed for SOAP services.
 * @author Aleksei Kokarev
 *
 */
public class SoapConfig {

  private final String separator = ",";

  String targetnamespace = "http://dhx.x-road.eu/producer";
  String securityServer;
  String securityServerAppender = "/cgi-bin/consumer_proxy";
  String xroadInstance;
  String memberClass;
  String memberCode;
  String defaultSubsystem = "DHX";
  String userId;
  String protocolVersion = "4.0";

  String globalConfLocation = "verificationconf";
  String globalConfFilename = "shared-params.xml";
  String dhxRepresentationGroupName = "DHX vahendajad";
  String acceptedSubsystems = "DHX";
  List<String> acceptedSubsystemsAsList;

  /*
   * String serviceXroadInstance; String serviceMemberClass; String serviceSubsystem;
   */


  String sendDocumentServiceCode = "sendDocument";
  String sendDocumentServiceVersion = "v1";
  String representativesServiceCode = "representationList";
  String representativesServiceVersion = "v1";

  Integer connectionTimeout = 60000;
  Integer readTimeout = 60000;

  String dhxSubsystemPrefix = "DHX";

  @Autowired
  Environment env;

  public List<String> getAcceptedSubsystemsAsList() {
    if (acceptedSubsystemsAsList == null) {
      String[] contextArray = acceptedSubsystems.split(separator);
      acceptedSubsystemsAsList = Arrays.asList(contextArray);
    }
    return acceptedSubsystemsAsList;
  }

  /**
   * Automatically initialize properties.
   */
  @PostConstruct
  public void init() {
    if (env.getProperty("soap.targetnamespace") != null) {
      setTargetnamespace(env.getProperty("soap.targetnamespace"));
    }
    if (env.getProperty("soap.security-server") != null) {
      setSecurityServer(env.getProperty("soap.security-server"));
    }
    if (env.getProperty("soap.security-server-appender") != null) {
      setSecurityServerAppender(env.getProperty("soap.security-server-appender"));
    }
    if (env.getProperty("soap.xroad-instance") != null) {
      setXroadInstance(env.getProperty("soap.xroad-instance"));
    }
    if (env.getProperty("soap.member-class") != null) {
      setMemberClass(env.getProperty("soap.member-class"));
    }
    if (env.getProperty("soap.dhx-subsystem-prefix") != null) {
      setDhxSubsystemPrefix(env.getProperty("soap.dhx-subsystem-prefix"));
    }
    if (env.getProperty("soap.default-subsystem") != null) {
      setDefaultSubsystem(env.getProperty("soap.default-subsystem"));
    }
    if (env.getProperty("soap.user-id") != null) {
      setUserId(env.getProperty("soap.user-id"));
    }
    if (env.getProperty("soap.protocol-version") != null) {
      setProtocolVersion(env.getProperty("soap.protocol-version"));
    }
    if (env.getProperty("soap.member-code") != null) {
      setMemberCode(env.getProperty("soap.member-code"));
    }
    if (env.getProperty("soap.global-conf-location") != null) {
      setGlobalConfLocation(env.getProperty("soap.global-conf-location"));
    }
    if (env.getProperty("soap.global-conf-filename") != null) {
      setGlobalConfFilename(env.getProperty("soap.global-conf-filename"));
    }
    if (env.getProperty("soap.dhx-representation-group-name") != null) {
      setDhxRepresentationGroupName(env.getProperty("soap.dhx-representation-group-name"));
    }
    /*
     * if (env.getProperty("soap.service-xroad-instance") != null) {
     * setServiceXroadInstance(env.getProperty("soap.service-xroad-instance")); } if
     * (env.getProperty("soap.service-subsystem") != null) {
     * setServiceSubsystem(env.getProperty("soap.service-subsystem")); }
     */
    if (env.getProperty("soap.send-document-service-code") != null) {
      setSendDocumentServiceCode(env.getProperty("soap.send-document-service-code"));
    }
    if (env.getProperty("soap.representatives-service-code") != null) {
      setRepresentativesServiceCode(env.getProperty("soap.representatives-service-code"));
    }
    if (env.getProperty("soap.send-document-service-version") != null) {
      setSendDocumentServiceVersion(env.getProperty("soap.send-document-service-version"));
    }
    if (env.getProperty("soap.representatives-service-version") != null) {
      setRepresentativesServiceVersion(env.getProperty("soap.representatives-service-version"));
    }
    if (env.getProperty("soap.connection-timeout") != null) {
      setConnectionTimeout(Integer.parseInt(env.getProperty("soap.connection-timeout")));
    }
    if (env.getProperty("soap.read-timeout") != null) {
      setReadTimeout(Integer.parseInt(env.getProperty("soap.read-timeout")));
    }
  }

  public String getSecurityServerWithAppender() {
    return securityServer + securityServerAppender;
  }

  /**
   * Helper method to add prefix if no prefix found.
   * 
   * @param system - system name to add prefix to
   * @return - uppercase system with prefix added
   */
  public String addPrefixIfNeeded(String system) {
    if (system == null) {
      system = getDhxSubsystemPrefix();
    }
    if (!system.startsWith(getDhxSubsystemPrefix() + ".")) {
      system = getDhxSubsystemPrefix() + "." + system;
    }
    return system.toUpperCase();
  }

  public XroadMember getDefaultClient() {
    XroadMember client =
        new XroadMember(getXroadInstance(), getMemberClass(), getMemberCode(),
            getDefaultSubsystem(), "", null);
    return client;
  }

}
