package ee.bpw.dhx.model;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.util.FileUtil;

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


@Getter
@Setter
public class DhxDocument {

  /**
   * Create DhxDocument.
   * @param service - XroadMember to whom document is mean to be sent
   * @param file - documents file
   * @param packFile - is file need to packed(true), or it is already packed(false)
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  public DhxDocument(XroadMember service, File file, Boolean packFile)
      throws DhxException {
    try {
      if (packFile) {
        FileUtil.gzipPackXml(file);
      } 
      InputStream stream = new FileInputStream(file);
      DataSource source = new ByteArrayDataSource(stream, "application/octet-stream");
      documentFile = new DataHandler(source);
      this.service = service;
    } catch (FileNotFoundException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    }

  }

  /*
   * private DhxDocument(InputStream stream, Boolean packFile) throws DhxException { try {
   * InputStream realStream; if (packFile) { realStream = new
   * FileInputStream(FileUtil.gzipPackXml(stream)); } else { realStream = stream; } DataSource
   * source = new ByteArrayDataSource(realStream, "application/octet-stream"); documentFile = new
   * DataHandler(source); } catch (IOException ex) { throw new
   * DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex); } }
   */

  /*public DhxDocument(/XroadMember service, InputStream stream,
      Boolean packFile) throws DhxException {
    this(stream, packFile);
    // this.representativeCode = recipient;
    // this.container = container;
    this.service = service;
  }*/

  /**
   * Create DhxDocument.
   * @param client - XroadMember from who the document is being sent
   * @param document - document to send
   */
  public DhxDocument(XroadMember client, SendDocument document) {
    // this.representativeCode = document.getRecipient();
    this.documentFile = document.getDocumentAttachment();
    this.externalConsignmentId = document.getConsignmentId();
    this.client = client;
  }

  //packed document file capsule
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
}
