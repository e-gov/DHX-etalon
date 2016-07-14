package ee.bpw.dhx.model;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.CapsuleVersionEnum;

import eu.x_road.dhx.producer.SendDocument;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

/**
 * Document object. Contains information needed for sending the document and for receiving the
 * document.
 * 
 * @author Aleksei Kokarev
 *
 */
public class DhxDocument {

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is mean to be sent
   * @param file - documents file
   * @param packFile - is file need to packed(true), or it is already packed(false)
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  public DhxDocument(XroadMember service, File file, Boolean packFile) throws DhxException {
    try {
      InputStream stream = new FileInputStream(file);
      DataSource source = new ByteArrayDataSource(stream, "text/xml; charset=UTF-8");
      documentFile = new DataHandler(source);
      this.service = service;
    } catch (FileNotFoundException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    }

  }

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is mean to be sent
   * @param stream - documents stream
   * @param packFile - is file need to packed(true), or it is already packed(false)
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  public DhxDocument(XroadMember service, InputStream stream, Boolean packFile)
      throws DhxException {
    try {
      InputStream realStream;
      realStream = stream;
      DataSource source = new ByteArrayDataSource(realStream, "application/octet-stream");
      documentFile = new DataHandler(source);
      this.service = service;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    }
  }


  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is mean to be sent
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param file - documents file
   * @param internalConsignmentId - consingment id for sending document
   * @param packFile - is file need to packed(true), or it is already packed(false)
   * @throws DhxException - thrown if error occurs while sending document
   */
  public DhxDocument(XroadMember service, Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, File file, String internalConsignmentId,
      Boolean packFile) throws DhxException {
    this(service, file, packFile);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
    this.internalConsignmentId = internalConsignmentId;

  }

  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is mean to be sent
   * @param stream - documents stream
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param internalConsignmentId - consignment id for sending document
   * @param packFile - is file need to packed(true), or it is already packed(false)
   * @throws DhxException - thrown if error occurs while sending document
   */
  public DhxDocument(XroadMember service, InputStream stream, Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, String internalConsignmentId, Boolean packFile)
      throws DhxException {
    this(service, stream, packFile);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
    this.internalConsignmentId = internalConsignmentId;
  }



  /**
   * Create DhxDocument. For document receiving
   * 
   * @param client - XroadMember from who the document is being sent
   * @param document - document to send
   */
  public DhxDocument(XroadMember client, SendDocument document) {
    // this.representativeCode = document.getRecipient();
    this.documentFile = document.getDocumentAttachment();
    this.externalConsignmentId = document.getConsignmentId();
    this.client = client;
  }

  /**
   * Create DhxDocument. For document receiving.
   * 
   * @param client - XroadMember from who the document is being sent
   * @param document - document to send
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   */
  public DhxDocument(XroadMember client, SendDocument document, Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion) {
    this(client, document);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
  }

  // packed document file capsule
  private DataHandler documentFile;

  private XroadMember client;

  // it it is outbound document, then the one to whom the document is being sent
  private XroadMember service;
  /**
   * external ID of the package.(for package receiving)
   */
  private String externalConsignmentId;

  /**
   * internal id of the package(for package sending).
   */
  private String internalConsignmentId;

  private Object parsedContainer;

  private CapsuleVersionEnum parsedContainerVersion;

  /**
   * getDocumentFile.
   * 
   * @return - datahandler of the file being sent or received
   */
  public DataHandler getDocumentFile() {
    return documentFile;
  }

  /**
   * setDocumentFile.
   * 
   * @param documentFile - datahandler of the file being sent or received
   */
  public void setDocumentFile(DataHandler documentFile) {
    this.documentFile = documentFile;
  }

  /**
   * If it is inbound document, then client is the one who sent the document, otherwise NULL.
   * 
   * @return client
   */
  public XroadMember getClient() {
    return client;
  }

  /**
   * /** If it is inbound document, then client is the one who sent the document, otherwise NULL.
   * 
   * @param client
   */
  public void setClient(XroadMember client) {
    this.client = client;
  }

  /**
   * it it is outbound document, then the one to whom the document is being sent, otherwise NULL.
   * 
   * @return service
   */
  public XroadMember getService() {
    return service;
  }

  /**
   * it it is outbound document, then the one to whom the document is being sent, otherwise NULL.
   * 
   * @param service
   */
  public void setService(XroadMember service) {
    this.service = service;
  }

  /**
   * External ID of the package.(for package receiving).
   * 
   * @return externalConsignmentId
   */
  public String getExternalConsignmentId() {
    return externalConsignmentId;
  }

  /**
   * External ID of the package.(for package receiving).
   * 
   * @param externalConsignmentId
   */
  public void setExternalConsignmentId(String externalConsignmentId) {
    this.externalConsignmentId = externalConsignmentId;
  }

  /**
   * internal id of the package(for package sending).
   * 
   * @return internalConsignmentId
   */
  public String getInternalConsignmentId() {
    return internalConsignmentId;
  }

  /**
   * internal id of the package(for package sending).
   * 
   * @param internalConsignmentId
   */
  public void setInternalConsignmentId(String internalConsignmentId) {
    this.internalConsignmentId = internalConsignmentId;
  }

  /**
   * Parsed container of the document(capsule). Is of object type, because different capsule
   * versions might be sent with different object types. Container is parsed only if container
   * parsing is turned on in cofiguration, otherwise parsedConrtainer is NULL.
   * 
   * @return parsedContainer
   */
  public Object getParsedContainer() {
    return parsedContainer;
  }

  /**
   * Parsed container of the document(capsule). Is of object type, because different capsule
   * versions might be sent with different object types. Container is parsed only if container
   * parsing is turned on in cofiguration, otherwise parsedConrtainer is NULL.
   * 
   * @param parsedContainer
   */
  public void setParsedContainer(Object parsedContainer) {
    this.parsedContainer = parsedContainer;
  }

  /**
   * Version of the container that is parsed. Only filled if container parsing is turned on in
   * configuration, otherwise is NULL.
   * 
   * @return
   */
  public CapsuleVersionEnum getParsedContainerVersion() {
    return parsedContainerVersion;
  }

  /**
   * Version of the container that is parsed. Only filled if container parsing is turned on in
   * configuration, otherwise is NULL.
   * @param parsedContainerVersion
   */
  public void setParsedContainerVersion(CapsuleVersionEnum parsedContainerVersion) {
    this.parsedContainerVersion = parsedContainerVersion;
  }
}
