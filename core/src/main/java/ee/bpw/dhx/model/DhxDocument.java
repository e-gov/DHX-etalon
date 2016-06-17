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
import ee.bpw.dhx.util.FileUtil;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import eu.x_road.dhx.producer.SendDocument;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DhxDocument {
	
	//public DhxDocument(){}
	
	public DhxDocument(String recipient, DecContainer container, File file, Boolean packFile)throws DhxException{
		try{
			File realFile;
			if(packFile) {
				realFile = FileUtil.gzipPackXML(file);
			} else {
				realFile = file;
			}
			InputStream stream = new FileInputStream(realFile);
			DataSource source = new ByteArrayDataSource(stream, "application/octet-stream");
			documentFile = new DataHandler(source);
			this.recipient = recipient;
			this.container = container;
		} catch(FileNotFoundException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR + ex.getMessage(), ex);
		} catch(IOException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR + ex.getMessage(), ex);
		}
		
	}
	
	private DhxDocument(InputStream stream, Boolean packFile) throws DhxException{
		try{
			InputStream realStream;
			if(packFile) {
				realStream = new FileInputStream(FileUtil.gzipPackXML(stream));
			} else {
				realStream = stream;
			}
			DataSource source = new ByteArrayDataSource(realStream, "application/octet-stream");
			documentFile = new DataHandler(source);	
		} catch(IOException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR + ex.getMessage(), ex);
		}
	}
	
	public DhxDocument(String recipient, InputStream stream, DecContainer container, Boolean packFile) throws DhxException{
		this(stream, packFile);
		this.recipient = recipient;
		this.container = container;
	}
	
	public DhxDocument (SendDocument document) {
		this.recipient = document.getRecipient();
		this.documentFile = document.getDocumentAttachment();
		this.externalConsignmentId = document.getConsignmentId();
	}
	
	private DataHandler documentFile;
	private String recipient;
	private DecContainer container;
	//if it is inbound document, then client is set
	private XroadClient client;
	/**
	 * external ID of the package.(for package receiving)
	 */
	private String externalConsignmentId;
	
	/**
	 * internal id of the package(for package sending). if not set, then random string is sent as consignment id
	 */
	private String internalConsignmentId;
}
