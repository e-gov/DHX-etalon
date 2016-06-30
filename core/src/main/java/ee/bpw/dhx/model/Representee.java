package ee.bpw.dhx.model;

import java.util.Date;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.XsdUtil;
import eu.x_road.dhx.producer.Member;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Representee {
		
	private String memberCode;
	private Date startDate;
	private Date endDate;
	
	public Representee (Member member) {
		this.memberCode = member.getMemberCode();
		this.startDate = XsdUtil.toDate(member.getStartDate());
		this.endDate = XsdUtil.toDate(member.getEndDate());
	}
	
	public Representee (String memberCode, Date startDate, Date endDate) {
		this.memberCode = memberCode;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	@Override
	public String toString(){
		return "memberCode: " + memberCode + " startDate: " + startDate +  " endDate: " + endDate;
	}
	
	public Member convertToMember () throws DhxException{
		Member member = new Member();
		member.setMemberCode(memberCode);
		member.setStartDate(XsdUtil.toGregorianCalendar(startDate));
		member.setEndDate(XsdUtil.toGregorianCalendar(endDate));
		return member;
	}
	

}
