//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.05 at 11:54:11 AM EEST 
//


package eu.x_road.xsd.identifiers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for XRoadSecurityServerIdentifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XRoadSecurityServerIdentifierType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://x-road.eu/xsd/identifiers}XRoadIdentifierType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://x-road.eu/xsd/identifiers}xRoadInstance"/&gt;
 *         &lt;element ref="{http://x-road.eu/xsd/identifiers}memberClass"/&gt;
 *         &lt;element ref="{http://x-road.eu/xsd/identifiers}memberCode"/&gt;
 *         &lt;element ref="{http://x-road.eu/xsd/identifiers}serverCode"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://x-road.eu/xsd/identifiers}objectType use="required" fixed="SERVER""/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XRoadSecurityServerIdentifierType")
public class XRoadSecurityServerIdentifierType
    extends XRoadIdentifierType
{


}
