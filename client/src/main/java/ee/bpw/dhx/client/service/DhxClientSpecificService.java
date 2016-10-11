package ee.bpw.dhx.client.service;

import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.InternalRepresentee;
import ee.bpw.dhx.ws.service.impl.ExampleDhxImplementationSpecificService;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DhxClientSpecificService extends ExampleDhxImplementationSpecificService {
  // get log4j logger to log events on custom level.
  final Logger logger = LogManager.getLogger();
  @Autowired
  private DhxClientConfig dhxConfig;


  @Override
  public String receiveDocument(DhxDocument document, MessageContext context) throws DhxException {
    String receiptId = super.receiveDocument(document, context);
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
  public List<InternalRepresentee> getRepresentationList() {
    String memberCodesStr = "";
    logger.log(Level.getLevel("EVENT"), "Staring returning representationList");
    List<String> list = dhxConfig.getRepresenteesList();
    List<String> listNames = dhxConfig.getRepresenteesNamesList();
    List<InternalRepresentee> representees = new ArrayList<InternalRepresentee>();
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        String representeeCode = list.get(i);
        String representeeName = listNames.get(i);
        memberCodesStr += (memberCodesStr == "" ? "" : ", ") + representeeCode;
        InternalRepresentee representee =
            new InternalRepresentee(representeeCode, new Date(), null, representeeName, null);
        representees.add(representee);
      }
    }
    logger.log(Level.getLevel("EVENT"), "Returning representationList. " + memberCodesStr);
    return representees;
  }

}
