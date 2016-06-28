package ee.bpw.dhx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.extern.slf4j.Slf4j;

import org.apache.axis.AxisFault;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamProperties;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationException;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.springframework.core.io.ClassPathResource;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

@Slf4j
public class XsdUtil {

	static boolean validateAgainstXSD(InputStream xml, InputStream xsd) {
		try {
			SchemaFactory factory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(xsd));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(xml));
			return true;
		} catch (Exception ex) {
			//logging on info level, bacause error is expected if capsule iw invalid
			log.info(ex.getMessage(), ex);
			return false;
		}
	}

	public static void validateDVKContainerWithLocalSchema(File containerFile,
			File schemaFile) throws DhxException {
		if (schemaFile != null && schemaFile.exists()) {
			XMLValidationSchemaFactory schemaFactory = XMLValidationSchemaFactory
					.newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA);
			XMLValidationSchema schema = null;
			XMLInputFactory2 xmlIF = (XMLInputFactory2) XMLInputFactory
					.newInstance();
			xmlIF.configureForLowMemUsage();
			XMLStreamReader2 reader = null;
			try {
				schema = schemaFactory.createSchema(schemaFile);
			} catch (Exception ex) {
				throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error initializing validation schema: "
						+ ex.getMessage(), ex);
			}

			try {
				reader = xmlIF.createXMLStreamReader(containerFile);
				reader.validateAgainst(schema);
				while (reader.hasNext()) {
					reader.next();
				}
			} catch (XMLValidationException ex) {
				throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Document container XML is invalid: "
						+ ex.getMessage(), ex);
			} catch (Exception ex) {
				throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error initializing validator: "
						+ ex.getMessage(), ex);
			} finally {
				if (reader != null) {
					try {
						reader.closeCompletely();
					} catch (Exception ex) {
					}
					reader = null;
				}
			}
		}
	}
	
	
	public static <T> T unmarshallCapsule (File capsuleFile, Unmarshaller unmarshaller) throws DhxException{
		try{
			return (T)unmarshallCapsule(new FileInputStream(capsuleFile), unmarshaller);	
		}catch(FileNotFoundException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}
	/**
	 * Parses capsule
	 * @param capsuleFile
	 * @return
	 * @throws DhxException
	 */
	public static <T> T unmarshallCapsule (InputStream capsuleStream, Unmarshaller unmarshaller) throws DhxException{
		try{
			if (log.isDebugEnabled()) {
				log.debug("unmarshalling file" );
			}
			/*JAXBContext unmarshalContext = JAXBContext.newInstance("ee.riik.schemas.deccontainer.vers_2_1");
			Unmarshaller unmarshaller = unmarshalContext.createUnmarshaller();	*/
			Object obj = (Object) unmarshaller.unmarshal(capsuleStream);
				return (T)obj;
			/*} else {
				log.info("Got unknown unmarshalled object");
				return null;
			}*/
		}catch(JAXBException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}
	
	/**
	 * Parses capsule
	 * @param capsuleFile
	 * @return
	 * @throws DhxException
	 */
	public static File marshallCapsule (Object container, Marshaller marshaller) throws DhxException{
		try{
			if (log.isDebugEnabled()) {
				log.debug("marshalling container" );
			}
			File outputFile = FileUtil.createPipelineFile(0, "");
			marshaller.marshal(container, outputFile);
			return outputFile;
		}catch(IOException|JAXBException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}
	
	public static void validate (File file, InputStream schemaStream) throws DhxException{
		validate(FileUtil.getFileAsStream(file), schemaStream);
	}
	/**
	 * Function validates file against XSD schema
	 * @param file - file to validate
	 * @param schemaFileStream - stream containing schema against which to validate
	 * @throws DhxException
	 */
	public static void validate (InputStream fileStream, InputStream schemaStream) throws DhxException{
		try {
			Source schemaSource = new StreamSource(schemaStream);
			Source xmlFile = new StreamSource(fileStream);
			SchemaFactory schemaFactory = SchemaFactory
			    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(schemaSource);
			Validator validator = schema.newValidator();
		    validator.validate(xmlFile);
		  log.info( "Document capsule is validated.");
		} catch (Exception e) {
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while validating capsule. " + e.getMessage(), e);
		}
	}

	public static <T>T unmarshall (Source source, Unmarshaller unmarshaller) throws DhxException{
		try{
			if (log.isDebugEnabled()) {
				log.debug("unmarshalling file" );
			}
			Object obj = (Object) unmarshaller.unmarshal(source);
				return (T)obj;
		}catch(JAXBException ex) {
			log.error(ex.getMessage(), ex);
			throw new DhxException(DHXExceptionEnum.CAPSULE_VALIDATION_ERROR, "Error occured while creating object from capsule. " + ex.getMessage(), ex);
		}
	}

}
