package ee.bpw.dhx.ws.config;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

@Getter
@Setter
@Configuration
/**
 * Main configuration of DHX webservice application
 * @author Aleksei Kokarev
 *
 */
public class DhxConfig {

  private final String marshallContextSeparator = ":";
  private final String separator = ",";

  @Value("${dhx.capsule-validate:true}")
  private Boolean capsuleValidate = true;
  
  @Value("${dhx.check-recipient:true}")
  private Boolean checkRecipient = true;
  
  @Value("${dhx.check-filesize:false}")
  private Boolean checkFilesize = false;
  
  @Value("${dhx.check-duplicate:true}")
  private Boolean checkDuplicate = true;
  
  @Value("${dhx.parse-capsule:true}")
  private Boolean parseCapsule = true;
  
  //list of timeout in seconds, delimited by comma.
  @Value("${dhx.document-resend-template:30,120,1200}")
  private String documentResendTemplate;
  
  @Value("${dhx.wsdl-file:dhx.wsdl}")
  private String wsdlFile;
  
  @Value("${dhx.marshall-context:ee.riik.schemas.deccontainer.vers_2_1:eu.x_road.dhx.producer:eu.x_road.xsd.identifiers:eu.x_road.xsd.representation:eu.x_road.xsd.xroad}")  
  private String marshallContext;
  
  private JAXBContext jaxbContext;
  
  private Jaxb2Marshaller dhxJaxb2Marshaller;
  
  //private Jaxb2Marshaller dhxJaxb2Marshaller;

  private String[] marshallContextAsList;
  

  private List<Integer> documentResendTimes;
  


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
  
  /**
   * Method return marshalling context as list.
   * 
   * @return array of package names for marshaller
   */
  public List<Integer> getDocumentResendTimes() {
    if (documentResendTimes == null) {
      String[] timesArray = documentResendTemplate.split(separator);
      Integer[] times = new Integer[timesArray.length];
      for (int i=0; i<timesArray.length; i++){
        String time = timesArray[i];
        Integer timeInt = Integer.parseInt(time);
        times[i]= timeInt;
      }
      documentResendTimes = Arrays.asList(times);
    }
    return documentResendTimes;
  }


  /**
   * Sets marshaller bean.
   * 
   * @return marshaller
   */
  @Bean
  public JAXBContext getJaxbContext() throws JAXBException{
    if (this.jaxbContext == null) {
      this.jaxbContext = JAXBContext.newInstance(marshallContext);
    }
    return jaxbContext;
  }
  
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
