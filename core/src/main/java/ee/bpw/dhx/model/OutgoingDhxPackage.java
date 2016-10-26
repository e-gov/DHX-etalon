package ee.bpw.dhx.model;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.CapsuleVersionEnum;

import java.io.File;
import java.io.InputStream;

public class OutgoingDhxPackage extends DhxPackage {

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param file - documents file
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client, File file,
      String internalConsignmentId) throws DhxException {
    super(service, client, file);
    this.internalConsignmentId = internalConsignmentId;

  }

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param stream - documents stream
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client,
      InputStream stream,
      String internalConsignmentId)
      throws DhxException {
    super(service, client, stream);
    this.internalConsignmentId = internalConsignmentId;
  }


  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param file - documents file
   * @param internalConsignmentId - consingment id for sending document
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client,
      Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, File file, String internalConsignmentId)
      throws DhxException {
    super(service, client, parsedContainer, parsedContainerVersion, file);
    this.internalConsignmentId = internalConsignmentId;
  }

  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param internalConsignmentId - consignment id for sending document
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client,
      InputStream stream,
      Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, String internalConsignmentId)
      throws DhxException {
    super(service, client, stream, parsedContainer, parsedContainerVersion);
    this.internalConsignmentId = internalConsignmentId;
  }


  /**
   * internal id of the package(for package sending).
   */
  private String internalConsignmentId;

  /**
   * internal id of the package(for package sending).
   * 
   * @return internalConsignmentId - internal id of the package(for package sending).
   */
  public String getInternalConsignmentId() {
    return internalConsignmentId;
  }

  /**
   * internal id of the package(for package sending).
   * 
   * @param internalConsignmentId - internal id of the package(for package sending).
   */
  public void setInternalConsignmentId(String internalConsignmentId) {
    this.internalConsignmentId = internalConsignmentId;
  }


  @Override
  public String toString() {
    String objString = super.toString();
    if (getInternalConsignmentId() != null) {
      objString += "internalConsignmentId: " + getInternalConsignmentId();
    }
    return objString;
  }

}