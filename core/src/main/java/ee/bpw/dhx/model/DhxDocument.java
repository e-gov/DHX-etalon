package ee.bpw.dhx.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import eu.x_road.dhx.producer.SendDocument;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DhxDocument {
	
	public DhxDocument(){}
	
	public DhxDocument(File file)throws DhxException{
		try{
			InputStream stream = new FileInputStream(file);
			DataSource source = new ByteArrayDataSource(stream, "application/octet-stream");
			documentFile = new DataHandler(source);
		} catch(FileNotFoundException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR + ex.getMessage(), ex);
		} catch(IOException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR + ex.getMessage(), ex);
		}
		
	}
	
	public DhxDocument(InputStream stream) throws DhxException{
		try{
			DataSource source = new ByteArrayDataSource(stream, "application/octet-stream");
			documentFile = new DataHandler(source);	
		} catch(IOException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR + ex.getMessage(), ex);
		}
	}
	
	public DhxDocument(String recipient, InputStream stream) throws DhxException{
		this(stream);
		this.recipient = recipient;
	}
	
	public DhxDocument (SendDocument document) {
		this.recipient = document.getRecipient();
		this.documentFile = document.getDocumentAttachment();
	}
	
	private DataHandler documentFile;
	private String recipient;
	private DecContainer container;
	/**
	 * unqiue ID of the document or of the package to send
	 */
	private String id;
}
