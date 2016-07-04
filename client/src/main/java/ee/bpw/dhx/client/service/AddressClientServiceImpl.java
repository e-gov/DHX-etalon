package ee.bpw.dhx.client.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.ws.service.impl.AddressServiceImpl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AddressClientServiceImpl extends AddressServiceImpl {

  final Logger logger = LogManager.getLogger();

  @Override
  protected List<XroadMember> getRenewedAdresseesList() throws DhxException {
    logger.log(Level.getLevel("EVENT"), "Staring renewing local address list");
    try {
      List<XroadMember> list = super.getRenewedAdresseesList();
      String localList = "";
      if (list != null && list.size() > 0) {
        for (XroadMember member : list) {
          localList += "\n" + member.toString();
        }
      } else {
        localList = "empty list";
      }
      logger.log(Level.getLevel("EVENT"), "Local address list is renewed. List: " + localList);
      return list;
    } catch (DhxException ex) {
      logger.log(Level.getLevel("EVENT"),
          "Error occured while renewing local address list." + ex.getMessage(), ex);
      throw ex;
    }
  }

}
