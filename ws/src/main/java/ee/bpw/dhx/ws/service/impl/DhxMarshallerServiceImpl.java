package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.service.DhxMarshallerService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

@Slf4j
@Service("dhxMarshallerService")
public class DhxMarshallerServiceImpl implements DhxMarshallerService {


  @Getter
  @Setter
  Unmarshaller unmarshaller;

  @Getter
  @Setter
  Marshaller marshaller;

  @Getter
  Jaxb2Marshaller jaxbMarshaller;

  @Autowired
  @Setter
  DhxConfig config;

  /**
   * Postconstruct init method. Sets marshallers needed for that service.
   * 
   * @throws JAXBException - thrown when error occured
   */
  @PostConstruct
  public void init() throws JAXBException {
    jaxbMarshaller = config.getDhxJaxb2Marshaller();
    marshaller = jaxbMarshaller.getJaxbContext().createMarshaller();
    unmarshaller = jaxbMarshaller.getJaxbContext().createUnmarshaller();
  }

  /**
   * Method parses(unmarshalls) object.
   * 
   * @param source - source of the marshalled object
   * @return - unmarshalled object
   * @throws DhxException - thrown if error occurs while unmrashalling object
   */
  @Loggable
  public <T> T unmarshall(Source source) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      Object obj = (Object) unmarshaller.unmarshal(source);
      return (T) obj;
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }


  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param capsuleFile - file to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshall(File capsuleFile)
      throws DhxException {
    try {
      log.debug("Unmarshalling file: {}", capsuleFile.getAbsolutePath());
      return (T) unmarshall(new FileInputStream(capsuleFile));
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }

  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param capsuleStream - stream to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshall(final InputStream capsuleStream)
      throws DhxException {

    return unmarshallAndValidate(capsuleStream, null);
  }


  /**
   * Parses(unmarshalls) object from file. And does validation against XSD schema if schemaStream is
   * present.
   * 
   * @param capsuleStream - stream of to parse
   * @param schemaStream - stream on XSD schema against which to validate. No validation is done if
   *        stream is NULL
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshallAndValidate(final InputStream capsuleStream,
      InputStream schemaStream) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      setSchemaForUnmarshaller(schemaStream);
      return unmarshallNoValidation(capsuleStream);
    } finally {
      // wont set single schema for unmarshaller
      unmarshaller.setSchema(null);
    }
  }

  @Loggable
  protected <T> T unmarshallNoValidation(final InputStream capsuleStream) throws DhxException {
    try {
      Object obj = (Object) unmarshaller.unmarshal(capsuleStream);
      return (T) obj;
    } catch (JAXBException ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }

  @Loggable
  private void setSchemaForUnmarshaller(InputStream schemaStream) throws DhxException {
    try {
      if (schemaStream != null) {
        Source schemaSource = new StreamSource(schemaStream);
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaSource);
        unmarshaller.setSchema(schema);
      }
    } catch (SAXException ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while setting schema for unmarshaller. " + ex.getMessage(), ex);
    }
  }

  /**
   * Marshalls object to file.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public File marshall(Object container) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      File outputFile = FileUtil.createPipelineFile();
      marshaller.marshal(container, outputFile);

      return outputFile;
    } catch (IOException | JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }


  /**
   * Marshalls object to result.
   * 
   * @param obj - object to marshall
   * @param result - result into which object will be marshalled
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public void marshallToResult(Object obj, Result result) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling object");
      }
      marshaller.marshal(obj, result);
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }

  /**
   * Marshalls object to node.
   * 
   * @param obj - object to marshall
   * @param node - node into which object will be marshalled
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public void marshallToNode(Object obj, Node node) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling object");
      }
      marshaller.marshal(obj, node);
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }


  /**
   * Marshalls object to writer.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public StringWriter marshallToWriter(Object container) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      StringWriter writer = new StringWriter();
      marshaller.marshal(container, writer);
      return writer;
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    }
  }

  /**
   * Method validates file against XSD schema.
   * 
   * @param file - file to validate
   * @param schemaStream - stream caontaining XSD schema
   * @throws DhxException - thrown if error occurs
   */
  @Loggable
  public void validate(File file, InputStream schemaStream) throws DhxException {
    validate(FileUtil.getFileAsStream(file), schemaStream);
  }

  /**
   * Function validates file against XSD schema.
   * 
   * @param fileStream - stream to validate
   * @param schemaStream - stream containing schema against which to validate
   * @throws DhxException - thrown if file is not validated against XSD schema.
   */
  @Loggable
  public void validate(final InputStream fileStream, InputStream schemaStream)
      throws DhxException {
    try {
      log.info("Starting validating document capsule.");
      Source schemaSource = new StreamSource(schemaStream);
      // to prevent original inpustream closing crete a new one
      Source xmlFile = new StreamSource(fileStream);
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = schemaFactory.newSchema(schemaSource);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);
      log.info("Document capsule is validated.");
    } catch (Exception ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while validating capsule. " + ex.getMessage(), ex);
    }
  }
  
  
  /**
   * Method checks filesize againts maximum filesize. NOT IMPLEMENTED!
   * 
   * @param streamToCheck - stream that needs to be checked
   * @throws DhxException thrown if filesize is bigger that maximum filesize
   */
  @Loggable
  public void checkFileSize(InputStream streamToCheck) throws DhxException {
    if (config.getCheckFilesize()) {
      log.info("Checking filesize.");
      log.info("File size check not done because check is not implemented.");
      throw new DhxException(DhxExceptionEnum.NOT_IMPLEMENTED,
          "No filesize check is implemented!");
    } else {
      log.info("Checking filesize is disabled in configuration.");
    }
  }

  public void readBig(InputStream fileStream) {

  }


}
