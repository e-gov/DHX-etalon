package ee.bpw.dhx.client.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxGateway;

import eu.x_road.dhx.producer.Member;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * Extension of DhxGateway. Contains changes needed for client application. e.g. event logging.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class DhxClientGateWay extends DhxGateway {

  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();

  @Override
  public SendDocumentResponse sendDocument(DhxDocument document) throws DhxException {
    if (document.getInternalConsignmentId() == null) {
      document.setInternalConsignmentId(UUID.randomUUID().toString());
    }
    logger.log(Level.getLevel("EVENT"), "Sending document to: "
        + document.getService().toString()
        + " internalConsignmentId: " + document.getInternalConsignmentId());
    SendDocumentResponse response = null;
    try {
      log.info("Sending document to {}", document.getService().toString());
      response = super.sendDocument(document);
      log.info("Sending document done");
      logger.log(Level.getLevel("EVENT"), "Document sent to: "
          + document.getService().toString()
          + " ReceiptId: "
          + response.getReceiptId()
          + (response.getFault() == null ? "" : " faultCode: "
              + response.getFault().getFaultCode() + " faultString: "
              + response.getFault().getFaultString()));
    } catch (DhxException ex) {
      log.error("Error occured while sending document. {}. {}", document.getService().toString(),
          ex.getMessage(), ex);
      logger.log(Level.getLevel("EVENT"),
          "Error occured while sending document. recipient: {}. {}", document.getService()
              .toString(), ex.getMessage());
      throw ex;
    }
    return response;

  }

  @Override
  public RepresentationListResponse getRepresentationList(XroadMember member) throws DhxException {
    RepresentationListResponse response = null;
    logger.log(Level.getLevel("EVENT"), "Getting representation list from: " + member.toString());
    try {
      response = super.getRepresentationList(member);

      if (response.getMembers() != null && response.getMembers().getMember() != null
          && response.getMembers().getMember().size() > 0) {
        String representatives = "";
        for (Member memberResponse : response.getMembers().getMember()) {
          representatives += ("\n") + new Representee(memberResponse).toString();
        }
        logger.log(Level.getLevel("EVENT"), "Representation list received: " + representatives);
      } else {
        logger.log(Level.getLevel("EVENT"), "Representation list received: empty list");
      }
    } catch (DhxException ex) {
      logger.log(Level.getLevel("EVENT"), "Error occured while getting representation list for: "
          + member.toString() + ". " + ex.getMessage());
      throw ex;
    }
    return response;
  }

}
