package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.DhxPackageService;

import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.xroad.GlobalGroupType;
import eu.x_road.xsd.xroad.MemberType;
import eu.x_road.xsd.xroad.SharedParametersType;
import eu.x_road.xsd.xroad.SubsystemType;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;

/**
 * Implementation of AddressService for creating and storing of address list. Stores address list in
 * memory at the moment. Refreshes address list at startup. Might be needed scheduled job to refresh
 * list periodically
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Service("addressService")
public class AddressServiceImpl implements AddressService {

  @Autowired
  @Setter
  SoapConfig config;


  @Autowired
  @Setter
  private DhxPackageService documentService;

  @Autowired
  @Setter
  DhxGateway dhxGateway;

  @Autowired
  @Setter
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  @Setter
  DhxImplementationSpecificService dhxImplementationSpecificService;


  public AddressServiceImpl() {}


  /**
   * Returns list of adressees.
   */
  public List<InternalXroadMember> getAdresseeList() throws DhxException {
    List<InternalXroadMember> adressees = dhxImplementationSpecificService.getAdresseeList();
    // obviously not initialized yet
    if (adressees == null) {
      renewAddressList();
      adressees = dhxImplementationSpecificService.getAdresseeList();
    }
    return adressees;
  }


  private void setAddresseeList(List<InternalXroadMember> members) throws DhxException {
    dhxImplementationSpecificService.saveAddresseeList(members);
  }

  /**
   * Postconstruct method.
   */
  @PostConstruct
  public void init() {
    try {
      renewAddressList();
      //catch throwable not to create exceptions if renew gives error
    } catch (Throwable ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * Method refreshes local list of addresses. Firstly shared parameters are fetched from security
   * server. In shared parameters members with subsystem DHX are found, those members are direct
   * adressees. Secondly DHX representation group is found and for every group member
   * representationList service request is invoked to find representees for every representor. Total
   * local addressees list is done from direct addressees and list of all representees found through
   * representationList service requests.
   */
  @Override
  public void renewAddressList() throws DhxException{
    List<InternalXroadMember> members = getRenewedAdresseesList();
    if( members != null && members.size()>0) {
      setAddresseeList(getRenewedAdresseesList());
    }
  }

  @Loggable
  protected List<InternalXroadMember> getRenewedAdresseesList() throws DhxException {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SharedParametersType globalConf = getGlobalConf();
    if (globalConf != null) {
      for (MemberType member : globalConf.getMember()) {
        for (SubsystemType subSystem : member.getSubsystem()) {
          // find DHX subsystem. if found, then member is ready to use DHX protocol
          if (subSystem.getSubsystemCode().toUpperCase()
              .startsWith(config.getDhxSubsystemPrefix().toUpperCase())) {
            log.debug("Found DHX subsystem for member: {}", member.getMemberCode());
            // do we have to add self to address list??
            members.add(new InternalXroadMember(config.getXroadInstance(), member, subSystem
                .getSubsystemCode(), member.getName()));
            // break;
          }
        }
      }
      for (GlobalGroupType group : globalConf.getGlobalGroup()) {
        log.debug("group: {}", group.getDescription());
        if (group.getGroupCode().equals(config.getDhxRepresentationGroupName())) {
          log.debug("Found representation group");
          for (XRoadClientIdentifierType client : group.getGroupMember()) {
            // exclude own representatives
            if (!client.getMemberCode().equals(config.getMemberCode())) {
              InternalXroadMember member = new InternalXroadMember(client);
              InternalXroadMember parentMember = findMember(client, members);
              if (parentMember != null) {
                parentMember.setRepresentor(true);
              }
              log.debug("getting representatives for member: {}", member.toString());
              try {
                List<InternalXroadMember> representeeMembers = getRepresentees(member);
                if (representeeMembers != null && representeeMembers.size() > 0) {
                  members.addAll(representeeMembers);
                }
              } catch (DhxException ex) {
                log.error(
                    "Error occured while getting representationList for: " + member.toString()
                        + ex.getMessage(), ex);
              }
              // include own representatives not from x-road servicce, but from local method
            } else {
              InternalXroadMember member = new InternalXroadMember(client);
              List<DhxRepresentee> representees =
                  dhxImplementationSpecificService.getRepresentationList();
              List<InternalXroadMember> representeesmembers =
                  new ArrayList<InternalXroadMember>();
              InternalXroadMember parentMember = findMember(client, members);
              for (DhxRepresentee representee : representees) {
                if (parentMember != null) {
                  parentMember.setRepresentor(true);
                }
                representeesmembers.add(new InternalXroadMember(member, representee));
              }
              members.addAll(representeesmembers);
            }
          }
        }
      }
    }
    return members;
  }

  private InternalXroadMember findMember(XRoadClientIdentifierType xroadClient,
      List<InternalXroadMember> members) {
    for (InternalXroadMember member : members) {
      if (member.getMemberCode().equalsIgnoreCase(xroadClient.getMemberCode())
          && member.getSubsystemCode().equalsIgnoreCase(xroadClient.getSubsystemCode())) {
        return member;
      }
    }
    return null;
  }

  @Loggable
  private List<InternalXroadMember> getRepresentees(InternalXroadMember member)
      throws DhxException {
    RepresentationListResponse response = dhxGateway.getRepresentationList(member);
    if (response == null || response.getRepresentees() == null
        || response.getRepresentees().getRepresentee() == null
        || response.getRepresentees().getRepresentee().size() == 0) {
      return null;
    } else {
      List<InternalXroadMember> representees = new ArrayList<InternalXroadMember>();
      for (eu.x_road.dhx.producer.Representee representee : response.getRepresentees()
          .getRepresentee()) {
        representees.add(new InternalXroadMember(member, new DhxRepresentee(representee)));
      }
      return representees;
    }
  }


  /**
   * Read global configuration shared parameters from secyrity server.
   * 
   * @return unmarshalled shared parameters Object
   * @throws DhxException throws if error occurs while getting global configuration
   */
  private SharedParametersType getGlobalConf() throws DhxException {
    try {
      URL url = new URL(config.getSecurityServer() + "/" + config.getGlobalConfLocation());
      log.debug("global conf URL: {}", url);
      URLConnection connection = url.openConnection();
      InputStream stream = connection.getInputStream();
      log.debug("got file from URL");
      InputStream confStream =
          FileUtil.zipUnpack(
              stream,
              config.getGlobalConfLocation() + "/" + config.getXroadInstance() + "/"
                  + config.getGlobalConfFilename());
      JAXBElement<SharedParametersType> globalConfElement =
          dhxMarshallerService.unmarshall(confStream);
      confStream.close();
      stream.close();
      return globalConfElement.getValue();
    } catch (MalformedURLException ex) {
      log.error("Error occurrred in url", ex);
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting global conf. " + ex.getMessage(), ex);

    } catch (IOException ex) {
      log.error("Error occurrred ", ex);
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting global conf. " + ex.getMessage(), ex);
    }
  }

  /**
   * Method finds xroadmember in local list of addresses by memberCode
   * 
   * @param memberCode - adressee code, might be either X-road member code or representee code.
   * @param system - adressees system. default system is NULL.
   * @return - return XroadMember object
   * @throws DhxException - thrown if recipient is not found
   */
  @Loggable
  public InternalXroadMember getClientForMemberCode(String memberCode, String system)
      throws DhxException {
    log.debug("getClientForMemberCode(String memberCode) memberCode: {}", memberCode);
    List<InternalXroadMember> members = getAdresseeList();
    Date curDate = new Date();
    if (members != null && members.size() > 0) {
      log.debug("local adressee list size: {}", members.size());
      for (InternalXroadMember member : members) {
        if (member.getMemberCode().equals(memberCode)
            && (member.getRepresentee() == null
            || member.getRepresentee().getMemberCode() == null)
            // check if adressees system is also chosen and is right
            && ((system == null)
            || (config.addPrefixIfNeeded(system).equals(config.addPrefixIfNeeded(member
                .getSubsystemCode()))))) {
          return member;
        } else if (member.getRepresentee() != null
            && member.getRepresentee().getMemberCode().equals(memberCode)
            && (member.getRepresentee().getStartDate().getTime() <= curDate.getTime() && (member
                .getRepresentee().getEndDate() == null || member.getRepresentee().getEndDate()
                .getTime() >= curDate.getTime()))
            && ((system == null && member.getRepresentee().getSystem() == null)
            || (system != null && member.getRepresentee().getSystem() != null
            && config.addPrefixIfNeeded(member.getRepresentee().getSystem())
                .equals(config.addPrefixIfNeeded(system))))) {
          return member;
        }
      }
      //as an exception searching by system, using memberCode as system. 
      //In older DVK packages adresssee might be adressees system, not real adressee
      InternalXroadMember memberToReturn = null;
      for (InternalXroadMember member : members) {
        if (config.addPrefixIfNeeded(memberCode).equals(config.addPrefixIfNeeded(member
          .getSubsystemCode()))
            && (member.getRepresentee() == null
            || member.getRepresentee().getMemberCode() == null)
         ) {
          //because we are searching by system, not involving memberCode, then return found member ONLY if single member found.
          if ( memberToReturn == null) {
            memberToReturn = member;
          } else{
            memberToReturn = null;
            break;
          }
        } else if (member.getRepresentee() != null
            && (member.getRepresentee().getStartDate().getTime() <= curDate.getTime() && (member
                .getRepresentee().getEndDate() == null || member.getRepresentee().getEndDate()
                .getTime() >= curDate.getTime()))
            && (
            (member.getRepresentee().getSystem() != null
            && config.addPrefixIfNeeded(member.getRepresentee().getSystem())
                .equals(config.addPrefixIfNeeded(memberCode))))) {
          //because we are searching by system, not involving memberCode, then return found member ONLY if single member found.
          if ( memberToReturn == null) {
            memberToReturn = member;
          } else{
            memberToReturn = null;
            break;
          }
        }
      }
      if (memberToReturn != null) {
        return memberToReturn;
      }
    }
    log.info("Membercode not found in local adressee list memberCode: {}, system: {}",
        memberCode, system);
    throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
        "Recipient is not found in address list. memberCode: " + memberCode);
  }


}
