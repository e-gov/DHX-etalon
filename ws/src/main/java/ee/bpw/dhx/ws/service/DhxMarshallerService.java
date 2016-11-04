package ee.bpw.dhx.ws.service;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.w3c.dom.Node;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Methods related to XSD and marshalling. e.g. marshalling, unmarshalling , validating
 * 
 * @author Aleksei Kokarev
 *
 */
public interface DhxMarshallerService {


  /**
   * Method parses(unmarshalls) object.
   * 
   * @param <T> - type of the capsule being unmarshalled
   * @param source - source of the marshalled object
   * @return - unmarshalled object
   * @throws DhxException - thrown if error occurs while unmrashalling object
   */
  public <T> T unmarshall(Source source) throws DhxException;


  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param <T> - type of the capsule being unmarshalled
   * @param capsuleFile - file to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  public <T> T unmarshall(File capsuleFile)
      throws DhxException;

  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param <T> - type of the capsule being unmarshalled
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
   * @param <T> - type of the capsule being unmarshalled
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
   * Marshalls object to result.
   * 
   * @param obj - object to marshall
   * @param result - result into which object will be marshalled
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  public void marshallToResult(Object obj, Result result) throws DhxException;

  /**
   * Marshalls object to node.
   * 
   * @param obj - object to marshall
   * @param node - node into which object will be marshalled
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  public void marshallToNode(Object obj, Node node) throws DhxException;

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
   * Method returns marshaller.
   * 
   * @return marshaller
   */
  public Marshaller getMarshaller() throws DhxException;
  
  /**
   * Method returns unmarshaller.
   * 
   * @return marshaller
   */
  public Unmarshaller getUnmarshaller() throws DhxException;

  
  /**
   * Method checks filesize againts maximum filesize. NOT IMPLEMENTED!
   * 
   * @param streamToCheck - stream that needs to be checked
   * @throws DhxException thrown if filesize is bigger that maximum filesize
   */
  public void checkFileSize(InputStream streamToCheck) throws DhxException;

}
