package ee.bpw.dhx.ws.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ee.bpw.dhx.exception.DhxException;

@Service
/**
 * Returns list of members that are represented by that member or empty list of there are no representatives exist
 * @author bpw
 *
 */
public interface RepresentationService {

	public List<String> getRepresentationList() throws DhxException;
}
