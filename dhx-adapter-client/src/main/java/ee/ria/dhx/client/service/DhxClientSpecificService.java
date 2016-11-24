package ee.ria.dhx.client.service;

import ee.ria.dhx.client.config.DhxClientConfig;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Recipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.ws.service.impl.ExampleDhxImplementationSpecificService;

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
  public String receiveDocument(IncomingDhxPackage document, MessageContext context)
      throws DhxException {
    String receiptId = super.receiveDocument(document, context);
    logger.log(Level.getLevel("EVENT"), "Document received." + " receipt:" + receiptId
        + " consignmentId: "
        + document.getExternalConsignmentId() + " Document sender: "
        + document.getClient().toString() + " Document reciever: "
        + document.getService().toString() + " Document recipient: "
        + document.getRecipient().toString());
    if (document.getParsedContainer() != null) {
      DecContainer container = (DecContainer) document.getParsedContainer();
      String addition = "";
      if (container.getDecMetadata() != null) {
        addition = " DecId: " + container.getDecMetadata().getDecId()
            + " DecFolder: " + container.getDecMetadata().getDecFolder();
      }
      if(container.getTransport().getDecRecipient() != null && container.getTransport().getDecRecipient().size()>0) {
        addition = addition + "\nDecRecipients: \n";
        for(DecRecipient recipient : container.getTransport().getDecRecipient()) {
          addition = addition + " DecRecipient organisationCode:"
              + recipient.getOrganisationCode() + "\n";             
        }
        addition = addition + "\n DecSender: organisationCode: " + container.getTransport().getDecSender().getOrganisationCode();
      }
      if(container.getRecipient() != null && container.getRecipient().size()>0) {
        for(Recipient recipient : container.getRecipient()) {
          if(recipient.getOrganisation() != null) {
          addition = addition + "\n Recipient oragnisation organisationCode: "
              + recipient.getOrganisation().getOrganisationCode();  
          }
        }
      }
      logger.log(Level.getLevel("EVENT"),
          "Document data from capsule: " + addition);

    }
    return receiptId;
  }

  @Override
  public List<DhxRepresentee> getRepresentationList() {
    String memberCodesStr = "";
    logger.log(Level.getLevel("EVENT"), "Staring returning representationList");
    List<String> list = dhxConfig.getRepresenteesList();
    List<String> listNames = dhxConfig.getRepresenteesNamesList();
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        String representeeCode = list.get(i);
        String representeeName = listNames.get(i);
        memberCodesStr += (memberCodesStr == "" ? "" : ", ") + representeeCode;
        DhxRepresentee representee =
            new DhxRepresentee(representeeCode, new Date(), null, representeeName, null);
        representees.add(representee);
      }
    }
    logger.log(Level.getLevel("EVENT"), "Returning representationList. " + memberCodesStr);
    return representees;
  }

}
