//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.05 at 11:54:11 AM EEST 
//


package ee.riik.schemas.deccontainer.vers_2_1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContactDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContactDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Adit" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="Phone" type="{http://www.riik.ee/schemas/deccontainer/vers_2_1/}PhoneType" minOccurs="0"/&gt;
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="WebPage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MessagingAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="PostalAddress" type="{http://www.riik.ee/schemas/deccontainer/vers_2_1/}PostalAddressType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactDataType", propOrder = {
    "adit",
    "phone",
    "email",
    "webPage",
    "messagingAddress",
    "postalAddress"
})
public class ContactDataType {

    @XmlElement(name = "Adit")
    protected Boolean adit;
    @XmlElement(name = "Phone")
    protected String phone;
    @XmlElement(name = "Email")
    protected String email;
    @XmlElement(name = "WebPage")
    protected String webPage;
    @XmlElement(name = "MessagingAddress")
    protected String messagingAddress;
    @XmlElement(name = "PostalAddress")
    protected PostalAddressType postalAddress;

    /**
     * Gets the value of the adit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAdit() {
        return adit;
    }

    /**
     * Sets the value of the adit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdit(Boolean value) {
        this.adit = value;
    }

    /**
     * Gets the value of the phone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the webPage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebPage() {
        return webPage;
    }

    /**
     * Sets the value of the webPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebPage(String value) {
        this.webPage = value;
    }

    /**
     * Gets the value of the messagingAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessagingAddress() {
        return messagingAddress;
    }

    /**
     * Sets the value of the messagingAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessagingAddress(String value) {
        this.messagingAddress = value;
    }

    /**
     * Gets the value of the postalAddress property.
     * 
     * @return
     *     possible object is
     *     {@link PostalAddressType }
     *     
     */
    public PostalAddressType getPostalAddress() {
        return postalAddress;
    }

    /**
     * Sets the value of the postalAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostalAddressType }
     *     
     */
    public void setPostalAddress(PostalAddressType value) {
        this.postalAddress = value;
    }

}
