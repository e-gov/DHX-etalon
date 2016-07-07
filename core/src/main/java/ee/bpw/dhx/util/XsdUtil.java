package ee.bpw.dhx.util;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Utility methods related to XSD and marshalling. e.g. marshalling, unmarshalling , validating
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class XsdUtil {


  /**
   * Method parses(unmarshalls) object.
   * 
   * @param source - source of the marshalled object
   * @param unmarshaller - unmarshaller to use
   * @return - unmarshalled object
   * @throws DhxException - thrown if error occurs while unmrashalling object
   */
  public static <T> T unmarshall(Source source, Unmarshaller unmarshaller) throws DhxException {
    log.debug(" <T> T unmarshall(Source source, Unmarshaller unmarshaller)");
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
   * @param unmarshaller - unmarshaller to use while
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  public static <T> T unmarshall(File capsuleFile, Unmarshaller unmarshaller)
      throws DhxException {
    log.debug("<T> T unmarshallCapsule(File capsuleFile, Unmarshaller unmarshaller)");
    try {
      log.debug("Unmarshalling file. " + capsuleFile.getAbsolutePath());
      return (T) unmarshall(new FileInputStream(capsuleFile), unmarshaller);
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
   * @param unmarshaller - unmarshaller to use while
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  public static <T> T unmarshall(final InputStream capsuleStream, Unmarshaller unmarshaller)
      throws DhxException {
    log.debug("<T> T unmarshallCapsule(final InputStream capsuleStream, Unmarshaller unmarshaller)");
    return unmarshallAndValidate(capsuleStream, null, unmarshaller);
  }


  /**
   * Parses(unmarshalls) object from file. And does validation against XSD schema if schemaStream is
   * present.
   * 
   * @param capsuleStream - stream of to parse
   * @param schemaStream - stream on XSD schema against which to validate. No validation is done if
   *        stream is NULL
   * @param unmarshaller - unmarshaller to use while
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  public static <T> T unmarshallAndValidate(final InputStream capsuleStream,
      InputStream schemaStream, Unmarshaller unmarshaller) throws DhxException {
    log.debug("<T> T unmarshallCapsuleAndValidate(final InputStream capsuleStream, "
        + "InputStream schemaStream, Unmarshaller unmarshaller)");
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      if (schemaStream != null) {
        Source schemaSource = new StreamSource(schemaStream);
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaSource);
        unmarshaller.setSchema(schema);
      }
      Object obj = (Object) unmarshaller.unmarshal(capsuleStream);
      return (T) obj;
    } catch (JAXBException | SAXException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    } finally {
      // wont set single schema for unmarshaller
      unmarshaller.setSchema(null);
    }
  }

  /**
   * Marshalls object to file.
   * 
   * @param container - object to marshall
   * @param marshaller - marshaller to use
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  public static File marshall(Object container, Marshaller marshaller) throws DhxException {
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
   * Method validates file against XSD schema.
   * 
   * @param file - file to validate
   * @param schemaStream - stream caontaining XSD schema
   * @throws DhxException - thrown if error occurs
   */
  public static void validate(File file, InputStream schemaStream) throws DhxException {
    validate(FileUtil.getFileAsStream(file), schemaStream);
  }

  /**
   * Function validates file against XSD schema.
   * 
   * @param fileStream - stream to validate
   * @param schemaStream - stream containing schema against which to validate
   * @throws DhxException - thrown if file is not validated against XSD schema.
   */
  public static void validate(final InputStream fileStream, InputStream schemaStream)
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
   * Method to fing adresssees from container. Method must return adressees for every existing
   * version of the container, bacause service which uses that method does not know anything about
   * container and just need adressees defined in it.
   * 
   * @param containerObject - container(capsule) object from which to find adressees
   * @return - list of the adresssees
   * @throws DhxException - thrown adressees parsing is not defined for given object (capsule
   *         version)
   */
  public static List<CapsuleAdressee> getAdresseesFromContainer(Object containerObject)
      throws DhxException {
    XsdVersionEnum version = XsdVersionEnum.forClass(containerObject.getClass());
    switch (version) {
      case V21:
        List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
        DecContainer container = (DecContainer) containerObject;
        if (container != null && container.getTransport() != null
            && container.getTransport().getDecRecipient() != null
            && container.getTransport().getDecRecipient().size() > 0) {
          File capsuleFile = null;
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
