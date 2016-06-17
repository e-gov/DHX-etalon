//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.15 at 05:50:29 PM EEST 
//


package eu.x_road.dhx.producer;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="recipient" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="consignmentId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="documentAttachment" type="{http://ws-i.org/profiles/basic/1.1/xsd}swaRef"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "recipient",
    "consignmentId",
    "documentAttachment"
})
@XmlRootElement(name = "sendDocument")
public class SendDocument {

    protected String recipient;
    @XmlElement(required = true)
    protected String consignmentId;
    @XmlElement(required = true, type = String.class)
    @XmlAttachmentRef
    protected DataHandler documentAttachment;

    /**
     * Gets the value of the recipient property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the value of the recipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecipient(String value) {
        this.recipient = value;
    }

    /**
     * Gets the value of the consignmentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsignmentId() {
        return consignmentId;
    }

    /**
     * Sets the value of the consignmentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsignmentId(String value) {
        this.consignmentId = value;
    }

    /**
     * Gets the value of the documentAttachment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DataHandler getDocumentAttachment() {
        return documentAttachment;
    }

    /**
     * Sets the value of the documentAttachment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentAttachment(DataHandler value) {
        this.documentAttachment = value;
    }

}