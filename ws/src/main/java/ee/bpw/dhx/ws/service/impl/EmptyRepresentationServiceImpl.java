package ee.bpw.dhx.ws.service.impl;

import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.ws.service.RepresentationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Empty implementation of RepresentationService. If there is no representation list to offer, then
 * that class might help
 * 
 * @author Aleksei Kokarev
 *
 */
public class EmptyRepresentationServiceImpl implements RepresentationService {

  @Override
  public List<Representee> getRepresentationList() {
    return new ArrayList<Representee>();
  }

}
