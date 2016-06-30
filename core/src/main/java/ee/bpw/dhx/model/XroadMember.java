package ee.bpw.dhx.model;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.xroad.MemberType;

import lombok.Getter;
import lombok.Setter;

/**
 * Internal representation of X-road member. 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
public class XroadMember {

  /**
   * Create internal XroadMember from XRoadClientIdentifierType(X-road object).
   * @param xrdClient - x-road client
   */
  public XroadMember(XRoadClientIdentifierType xrdClient) {
    this.xroadInstance = xrdClient.getXRoadInstance();
    this.memberClass = xrdClient.getMemberClass();
    this.memberCode = xrdClient.getMemberCode();
    this.subsystemCode = xrdClient.getSubsystemCode();
  }

  /**
   * Create internal XroadMember.
   * @param xroadInstance - name of X-road instance
   * @param member - X-road member(X-road obvject)
   * @param subsytemCode - X-road subsystem
   */
  public XroadMember(String xroadInstance, MemberType member, String subsytemCode) {
    this.xroadInstance = xroadInstance;
    this.memberClass = member.getMemberClass().getCode();
    this.memberCode = member.getMemberCode();
    this.subsystemCode = subsytemCode;
  }

  /**
   * Create internal XroadMember from another XroadMember and representee.
   * @param member - XroadMember from which nto create new XroadMember
   * @param representee - representee to put to new XroadMember
   */
  public XroadMember(XroadMember member, Representee representee) {
    this.xroadInstance = member.getXroadInstance();
    this.memberClass = member.getMemberClass();
    this.memberCode = member.getMemberCode();
    this.subsystemCode = member.getSubsystemCode();
    this.representee = representee;
  }

  /**
   * Create internal XroadMember.
   * @param xroadInstance  - name of members X-road instance
   * @param memberClass - name of members X-road class
   * @param memberCode - members X-road member code
   * @param subsystemCode - name on X-road subsystem
   * @param representee - representee to put to new XroadMember
   */
  public XroadMember(String xroadInstance, String memberClass, String memberCode,
      String subsystemCode, Representee representee) {
    this.xroadInstance = xroadInstance;
    this.memberClass = memberClass;
    this.memberCode = memberCode;
    this.subsystemCode = subsystemCode;
    this.representee = representee;

  }

  private String xroadInstance;
  private String memberClass;
  private String memberCode;
  private String subsystemCode;

  private Representee representee;


  @Override
  public String toString() {
    return "addressee: " + (representee == null ? memberCode : representee.getMemberCode())
        + ", X-road member: " + xroadInstance + "/" + memberClass + "/" + memberCode + "/"
        + subsystemCode + ", is representee: " + (representee == null ? false : true);
  }

}
