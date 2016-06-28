package ee.bpw.dhx.ws.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;

@Service
/**
 * Returns list of members that are represented by that member or empty list of there are no representatives exist
 * @author Aleksei Kokarev
 *
 */
public abstract class RepresentationService {
	
	@Autowired
	DhxGateway dhxGateway;

	public abstract List<String> getRepresentationList() throws DhxException;
	
	public List<String> getRepresentationListForEndpoint (MessageContext messageContext) throws DhxException {
		dhxGateway.getXroadCLientAndSetRersponseHeader(messageContext);
		return getRepresentationList();
	} 
}
