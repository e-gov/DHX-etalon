package ee.bpw.dhx.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.ConversionUtil;
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
		if(member.getStartDate() != null) {
			this.startDate = ConversionUtil.toDate(member.getStartDate());
		}
		if(member.getEndDate() != null) {
			this.endDate = ConversionUtil.toDate(member.getEndDate());
		}
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
		if(this.getStartDate() != null) {
			member.setStartDate(ConversionUtil.toGregorianCalendar(startDate));
		}
		if(this.getEndDate() != null) {
			member.setEndDate(ConversionUtil.toGregorianCalendar(endDate));
		}
		return member;
	}
	

}
