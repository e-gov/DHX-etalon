package ee.bpw.dhx.model;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.ConversionUtil;

import eu.x_road.dhx.producer.Member;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class Representee {

  private String memberCode;
  private Date startDate;
  private Date endDate;

  /**
   * Create Representee from member(which is representationList service output).
   * 
   * @param member - member from which to create representee
   */
  public Representee(Member member) {
    this.memberCode = member.getMemberCode();
    if (member.getStartDate() != null) {
      this.startDate = ConversionUtil.toDate(member.getStartDate());
    }
    if (member.getEndDate() != null) {
      this.endDate = ConversionUtil.toDate(member.getEndDate());
    }
  }

  /**
   * Create Representee.
   * 
   * @param memberCode - X-road member code
   * @param startDate - representees start date
   * @param endDate - representees end date
   */
  public Representee(String memberCode, Date startDate, Date endDate) {
    this.memberCode = memberCode;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  public String toString() {
    return "memberCode: " + memberCode + " startDate: " + startDate + " endDate: " + endDate;
  }

  /**
   * Converts representee to member(for representationList service output).
   * 
   * @return - member
   * @throws DhxException - thrown if error occurs while converting
   */
  public Member convertToMember() throws DhxException {
    Member member = new Member();
    member.setMemberCode(memberCode);
    if (this.getStartDate() != null) {
      member.setStartDate(ConversionUtil.toGregorianCalendar(startDate));
    }
    if (this.getEndDate() != null) {
      member.setEndDate(ConversionUtil.toGregorianCalendar(endDate));
    }
    return member;
  }


}
