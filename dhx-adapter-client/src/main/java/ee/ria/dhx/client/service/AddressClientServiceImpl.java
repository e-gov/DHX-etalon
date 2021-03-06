package ee.ria.dhx.client.service;


import ee.ria.dhx.exception.DhxException;

import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.impl.AddressServiceImpl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension of AddressServiceImpl. Contains changes needed for client application. e.g. event
 * logging.
 * 
 * @author Aleksei Kokarev
 *
 */
@Service
public class AddressClientServiceImpl extends AddressServiceImpl {

  final Logger logger = LogManager.getLogger();

  @Autowired
  SoapConfig config;

  @Override
  protected List<InternalXroadMember> getRenewedAdresseesList() throws DhxException {
    logger.log(Level.getLevel("EVENT"), "Staring renewing local address list");
    try {
      List<InternalXroadMember> list = super.getRenewedAdresseesList();
      String localList = "";
      if (list != null && list.size() > 0) {
        for (InternalXroadMember member : list) {
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

  /**
   * Helper method to generate adressees list for select box.
   * 
   * @return - optimized for select box list of adresses
   * @throws DhxException - thrown if error occurs
   */
  public List<Map<String, String>> getAdresseesAsSelect() throws DhxException {
    List<Map<String, String>> select = new ArrayList<Map<String, String>>();
    Map<String, String> row = null;
    for (InternalXroadMember member : this.getAdresseeList()) {
      row = new HashMap<String, String>();
      String name = null;
      String value = null;
      if (member.getRepresentee() != null) {
        name =
            member.getRepresentee().getRepresenteeName()
                + " ("
                + member.getRepresentee().getRepresenteeCode()
                +
                (member.getRepresentee().getRepresenteeSystem() != null ? "."
                    + member.getRepresentee().getRepresenteeSystem() : "")
                + " Asutuse " + member.getMemberCode() + " vahendatav" + ")";
        value =
            (member.getRepresentee().getRepresenteeSystem() != null ?
                member.getRepresentee().getRepresenteeSystem() + ":" : "")
                + member.getRepresentee().getRepresenteeCode();
      } else {
        name =
            member.getName() + " (" + member.getMemberCode() + ", süsteem: "
                + member.getSubsystemCode() + ")";
        value = member.getSubsystemCode() + ":" + member.getMemberCode();
      }
      row.put("name", name);
      row.put("value", value);
      select.add(row);
    }
    return select;
  }
}
