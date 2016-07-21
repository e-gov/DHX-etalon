package ee.bpw.dhx.client.service;

import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.ws.service.impl.ExampleDhxImplementationSpecificService;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DhxClientSpecificService extends ExampleDhxImplementationSpecificService {
  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();
  @Autowired
  private DhxClientConfig dhxConfig;


  @Override
  public String receiveDocument(DhxDocument document) throws DhxException {
    String receiptId = super.receiveDocument(document);
    logger.log(Level.getLevel("EVENT"), "Document received. for: "
        + document.getClient().toString() + " receipt:" + receiptId + " consignmentId: "
        + document.getExternalConsignmentId());
    if (document.getParsedContainer() != null) {
      DecContainer container = (DecContainer) document.getParsedContainer();
      logger.log(Level.getLevel("EVENT"),
          "Document data from capsule: recipient organisationCode:"
              + container.getTransport().getDecRecipient().get(0).getOrganisationCode()
              + " sender organisationCode:"
              + container.getTransport().getDecSender().getOrganisationCode());
    }
    return receiptId;
  }

  @Override
  public List<Representee> getRepresentationList() {
    String memberCodesStr = "";
    logger.log(Level.getLevel("EVENT"), "Staring returning representationList");
    List<String> list = dhxConfig.getRepresenteesList();
    List<Representee> representees = new ArrayList<Representee>();
    if (list != null) {
      for (String representative : list) {
        memberCodesStr += (memberCodesStr == "" ? "" : ", ") + representative;
        Representee representee = new Representee(representative, new Date(), null);
        representees.add(representee);
      }
    }
    logger.log(Level.getLevel("EVENT"), "Returning representationList. " + memberCodesStr);
    return representees;
  }

}
