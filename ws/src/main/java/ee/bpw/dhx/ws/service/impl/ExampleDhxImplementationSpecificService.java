package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class is an example implementation of DhxImplementationSpecificService interface. All data that
 * need to be stored is stored in memory.
 * 
 * @deprecated - its just an example implementation. real implementation should be done!
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Deprecated
public class ExampleDhxImplementationSpecificService implements DhxImplementationSpecificService {

  private List<DhxDocument> documents = new ArrayList<DhxDocument>();

  private List<XroadMember> members;

  @Override
  @Deprecated
  @Loggable
  public boolean isDuplicatePackage(XroadMember from, String consignmentId) {
    log.debug("Checking for duplicates. from memberCode: {}", from.toString()
        + " from consignmentId:" + consignmentId);
    if (documents != null && documents.size() > 0) {
      for (DhxDocument document : documents) {
        if (document.getExternalConsignmentId() != null
            && document.getExternalConsignmentId().equals(consignmentId)
            && (document.getClient().toString().equals(from.toString())
            || document.getClient().getRepresentee().getMemberCode().equals(from.toString()))) {
          return true;
        }
      }
    }
    return false;
  }


  @Override
  @Deprecated
  @Loggable
  public String receiveDocument(DhxDocument document) throws DhxException {
    log.debug("String receiveDocument(DhxDocument document) externalConsignmentId: {}",
        document.getExternalConsignmentId());
    String receiptId = UUID.randomUUID().toString();
    documents.add(document);
    return receiptId;
  }

  @Override
  @Deprecated
  @Loggable
  public List<Representee> getRepresentationList() {
    return new ArrayList<Representee>();
  }

  @Override
  @Deprecated
  public List<XroadMember> getAdresseeList() {
    return members;
  }

  @Override
  @Deprecated
  public void saveAddresseeList(List<XroadMember> members) {
    this.members = members;
  }

}
