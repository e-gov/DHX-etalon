package ee.bpw.dhx.model;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdVersionEnum;

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

@Getter
@Setter
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
      /*
       * if (packFile) { // TODO: delete original file or not??? file = FileUtil.gzipPackXml(file);
       * }
       */
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
      XsdVersionEnum parsedContainerVersion, File file, String internalConsignmentId,
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
      XsdVersionEnum parsedContainerVersion, String internalConsignmentId, Boolean packFile)
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
      XsdVersionEnum parsedContainerVersion) {
    this(client, document);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
  }

  // packed document file capsule
  private DataHandler documentFile;
  // private String representativeCode;
  // private DecContainer container;
  // if it is inbound docuemnt, then client is the one who sent the document
  private XroadMember client;

  // it it is outbound document, then the one to whom the document is being sent
  private XroadMember service;
  /**
   * external ID of the package.(for package receiving)
   */
  private String externalConsignmentId;

  /**
   * internal id of the package(for package sending). if not set, then random string is sent as
   * consignment id
   */
  private String internalConsignmentId;

  private Object parsedContainer;

  private XsdVersionEnum parsedContainerVersion;
}
