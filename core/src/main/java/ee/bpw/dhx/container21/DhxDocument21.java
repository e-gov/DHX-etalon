package ee.bpw.dhx.container21;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import eu.x_road.dhx.producer.SendDocument;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.InputStream;

/**
 * DhxDocument implementation specific for capsule version 2.1
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
public class DhxDocument21 extends DhxDocument {

  DecContainer container;

  /**
   * Create DhxDocument21(for capsule version 2.1)
   * @param service - XroadMember to whom document is mean to be sent
   * @param container - document capsule  of version 2.1
   * @param file - documents file
   * @param packFile - is file need to packed(true), or it is already packed(false)
   * @throws DhxException - thrown if error occurs while sending document
   */
  public DhxDocument21(XroadMember service, DecContainer container, File file, Boolean packFile)
      throws DhxException {
    super(service, file, packFile);
    this.container = container;

  }

  /*public DhxDocument21(XroadMember service, InputStream stream, DecContainer container,
      Boolean packFile) throws DhxException {
    super(service, stream, packFile);
    this.container = container;
  }*/

  public DhxDocument21(XroadMember client, SendDocument document, DecContainer container) {
    super(client, document);
    this.container = container;
  }

}
