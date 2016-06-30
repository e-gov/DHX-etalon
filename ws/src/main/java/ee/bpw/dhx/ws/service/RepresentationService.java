package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.model.Representee;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * Returns list of members that are represented by that member 
 * or empty list of there are no representatives exist
 * @author Aleksei Kokarev
 *
 */
public interface RepresentationService {

  /**
   * Method returns list of representees.
   * 
   * @return List of representees that are represented by that X-road member or empty List if there
   *         are no representees.
   */
  public abstract List<Representee> getRepresentationList();

}
