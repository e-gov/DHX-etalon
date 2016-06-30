package ee.bpw.dhx.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import lombok.Getter;
import lombok.Setter;
import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.FileUtil;
import eu.x_road.dhx.producer.SendDocument;


@Getter
@Setter
public class DhxDocument {
	
	//public DhxDocument(){}
	
	public DhxDocument(/*String recipient, */XroadMember service, File file, Boolean packFile)throws DhxException{
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
			//this.representativeCode = recipient;
			//this.container = container;
			this.service = service;
		} catch(FileNotFoundException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
		} catch(IOException ex) {
			throw new DhxException(DHXExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
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
			throw new DhxException(DHXExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
		}
	}
	
	public DhxDocument(/*String recipient, */XroadMember service, InputStream stream, Boolean packFile) throws DhxException{
		this(stream, packFile);
		//this.representativeCode = recipient;
		//this.container = container;
		this.service = service;
	}
	
	public DhxDocument (XroadMember client, SendDocument document) {
		//this.representativeCode = document.getRecipient();
		this.documentFile = document.getDocumentAttachment();
		this.externalConsignmentId = document.getConsignmentId();
		this.client = client;
	}
	
	private DataHandler documentFile;
//	private String representativeCode;
	//private DecContainer container;
	//if it is inbound docuemnt, then client is the one who sent the document
	private XroadMember client;
	
	//it it is outbound document, then the one to whom the document is being sent
	private XroadMember service;
	/**
	 * external ID of the package.(for package receiving)
	 */
	private String externalConsignmentId;
	
	/**
	 * internal id of the package(for package sending). if not set, then random string is sent as consignment id
	 */
	private String internalConsignmentId;
}
