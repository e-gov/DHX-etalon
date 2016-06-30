package ee.bpw.dhx.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ee.bpw.dhx.exception.DHXExceptionEnum;
import ee.bpw.dhx.exception.DhxException;

public class ConversionUtil {
	
	public static XMLGregorianCalendar toGregorianCalendar(Date date) throws DhxException{
		try {
			GregorianCalendar gcalendar = new GregorianCalendar();
			gcalendar.setTime(date);
			XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
			return xmlDate;
		} catch(DatatypeConfigurationException ex) {
			throw new DhxException(DHXExceptionEnum.TECHNICAL_ERROR, "Error occured while converting date. " + ex.getMessage(), ex);
		}
	}
	
	public static Date toDate(XMLGregorianCalendar xmlDate) {
		return xmlDate.toGregorianCalendar().getTime();

	}

}
