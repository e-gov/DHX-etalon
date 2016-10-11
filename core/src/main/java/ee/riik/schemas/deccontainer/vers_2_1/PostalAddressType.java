//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.10.04 at 11:43:08 AM EEST 
//


package ee.riik.schemas.deccontainer.vers_2_1;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PostalAddressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostalAddressType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="County" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="LocalGovernment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="AdministrativeUnit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="SmallPlace" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="LandUnit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Street" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="HouseNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="BuildingPartNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="PostalCode" type="{http://www.riik.ee/schemas/deccontainer/vers_2_1/}PostalCodeType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostalAddressType", propOrder = {
    "country",
    "county",
    "localGovernment",
    "administrativeUnit",
    "smallPlace",
    "landUnit",
    "street",
    "houseNumber",
    "buildingPartNumber",
    "postalCode"
})
public class PostalAddressType {

    @XmlElement(name = "Country")
    protected String country;
    @XmlElement(name = "County")
    protected String county;
    @XmlElement(name = "LocalGovernment")
    protected String localGovernment;
    @XmlElement(name = "AdministrativeUnit")
    protected String administrativeUnit;
    @XmlElement(name = "SmallPlace")
    protected String smallPlace;
    @XmlElement(name = "LandUnit")
    protected String landUnit;
    @XmlElement(name = "Street")
    protected String street;
    @XmlElement(name = "HouseNumber")
    protected String houseNumber;
    @XmlElement(name = "BuildingPartNumber")
    protected String buildingPartNumber;
    @XmlElement(name = "PostalCode")
    protected BigInteger postalCode;

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the county property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the value of the county property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCounty(String value) {
        this.county = value;
    }

    /**
     * Gets the value of the localGovernment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalGovernment() {
        return localGovernment;
    }

    /**
     * Sets the value of the localGovernment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalGovernment(String value) {
        this.localGovernment = value;
    }

    /**
     * Gets the value of the administrativeUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdministrativeUnit() {
        return administrativeUnit;
    }

    /**
     * Sets the value of the administrativeUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdministrativeUnit(String value) {
        this.administrativeUnit = value;
    }

    /**
     * Gets the value of the smallPlace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmallPlace() {
        return smallPlace;
    }

    /**
     * Sets the value of the smallPlace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmallPlace(String value) {
        this.smallPlace = value;
    }

    /**
     * Gets the value of the landUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLandUnit() {
        return landUnit;
    }

    /**
     * Sets the value of the landUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLandUnit(String value) {
        this.landUnit = value;
    }

    /**
     * Gets the value of the street property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the value of the street property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreet(String value) {
        this.street = value;
    }

    /**
     * Gets the value of the houseNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Sets the value of the houseNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHouseNumber(String value) {
        this.houseNumber = value;
    }

    /**
     * Gets the value of the buildingPartNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuildingPartNumber() {
        return buildingPartNumber;
    }

    /**
     * Sets the value of the buildingPartNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuildingPartNumber(String value) {
        this.buildingPartNumber = value;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPostalCode(BigInteger value) {
        this.postalCode = value;
    }

}
