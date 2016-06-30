package ee.bpw.dhx.ws.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;

@Service
/**
 * Returns list of members that are represented by that member or empty list of there are no representatives exist
 * @author Aleksei Kokarev
 *
 */
public interface RepresentationService {

	/**
	 * Method returns list of representees. 
	 * @return
	 * @throws DhxException
	 */
	public abstract List<Representee> getRepresentationList() throws DhxException;

}
