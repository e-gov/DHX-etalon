package ee.bpw.dhx.client.service;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxGateway;
import eu.x_road.dhx.producer.Member;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocumentResponse;

@Slf4j
public class DocumentGateWayClient  extends DhxGateway{
	
	//get log4j logger to log events on custom level.
	final Logger logger = LogManager.getLogger();
	
	@Override
	public SendDocumentResponse sendDocument(DhxDocument document) throws DhxException{
			if(document.getInternalConsignmentId() == null) {
				String consignmentId = UUID.randomUUID().toString();
				document.setInternalConsignmentId(consignmentId);
			}
			logger.log(Level.getLevel("EVENT"), "Sending document to:" + document.getService().toString() + " internalConsignmentId:" + document.getInternalConsignmentId());
			SendDocumentResponse response = null;
			try{
				log.info("Sending document to " + document.getService().toString());
				response = super.sendDocument(document);
				log.info("Sending document done");
				logger.log(Level.getLevel("EVENT"), "Document sent to :" + document.getService().toString() + " ReceiptId:" + response.getReceiptId()
						+ (response.getFault()==null?"":" faultCode:" + response.getFault().getFaultCode() + " faultString:" + response.getFault().getFaultString()));
			} catch(DhxException e) {
				log.error("Error occured while sending document. :" + document.getService().toString() + ". " + e.getMessage(), e);
				logger.log(Level.getLevel("EVENT"),"Error occured while sending document. recipient:" + document.getService().toString() + ". " + e.getMessage());
				throw e;
			}
		return response;

	}
	
	@Override
	public RepresentationListResponse getRepresentationList(XroadMember member) throws DhxException{
		RepresentationListResponse response = null;
		logger.log(Level.getLevel("EVENT"), "Getting representation list from:" + member.toString());
			try{
				response = super.getRepresentationList(member);
				
				if(response.getMembers() != null && response.getMembers().getMember() != null && response.getMembers().getMember().size()>0) {
					String representatives = "";
					for(Member memberResponse : response.getMembers().getMember()) {
						representatives += (representatives.equals("")?"":", ") + new Representee(memberResponse).toString();
					}
					logger.log(Level.getLevel("EVENT"), "Representation list received: " + representatives);
				} else {
					logger.log(Level.getLevel("EVENT"), "Representation list received: empty list");
				}
			} 
			catch(DhxException e) {
				logger.log(Level.getLevel("EVENT"),"Error occured while getting representation list for:" + member.toString() + ". " + e.getMessage());
				throw e;
			}
			return response;
	}

}
