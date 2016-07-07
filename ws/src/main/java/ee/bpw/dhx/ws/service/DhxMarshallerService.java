package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.CapsuleAdressee;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Source;

/**
 * Methods related to XSD and marshalling. e.g. marshalling, unmarshalling , validating
 * 
 * @author Aleksei Kokarev
 *
 */
@Service
public interface DhxMarshallerService {


  /**
   * Method parses(unmarshalls) object.
   * 
   * @param source - source of the marshalled object
   * @return - unmarshalled object
   * @throws DhxException - thrown if error occurs while unmrashalling object
   */
  public <T> T unmarshall(Source source) throws DhxException;


  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param capsuleFile - file to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  public <T> T unmarshall(File capsuleFile)
      throws DhxException;

  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param capsuleStream - stream to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  public <T> T unmarshall(final InputStream capsuleStream)
      throws DhxException;


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
  public <T> T unmarshallAndValidate(final InputStream capsuleStream,
      InputStream schemaStream) throws DhxException;

  /**
   * Marshalls object to file.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  public File marshall(Object container) throws DhxException;

  /**
   * Marshalls object to writer.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  public StringWriter marshallToWriter(Object container) throws DhxException;

  /**
   * Method validates file against XSD schema.
   * 
   * @param file - file to validate
   * @param schemaStream - stream caontaining XSD schema
   * @throws DhxException - thrown if error occurs
   */
  public void validate(File file, InputStream schemaStream) throws DhxException;

  /**
   * Function validates file against XSD schema.
   * 
   * @param fileStream - stream to validate
   * @param schemaStream - stream containing schema against which to validate
   * @throws DhxException - thrown if file is not validated against XSD schema.
   */
  public void validate(final InputStream fileStream, InputStream schemaStream)
      throws DhxException;

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
  public List<CapsuleAdressee> getAdresseesFromContainer(Object containerObject)
      throws DhxException;


  /**
   * Method returns marshaller
   * 
   * @return marshaller
   */
  public Jaxb2Marshaller getJaxbMarshaller();


}
