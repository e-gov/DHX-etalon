//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.21 at 02:49:54 PM EEST 
//


package eu.x_road.dhx.producer;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.x_road.dhx.producer package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.x_road.dhx.producer
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RepresentationListResponse }
     * 
     */
    public RepresentationListResponse createRepresentationListResponse() {
        return new RepresentationListResponse();
    }

    /**
     * Create an instance of {@link MemberCodes }
     * 
     */
    public MemberCodes createMemberCodes() {
        return new MemberCodes();
    }

    /**
     * Create an instance of {@link RepresentationList }
     * 
     */
    public RepresentationList createRepresentationList() {
        return new RepresentationList();
    }

    /**
     * Create an instance of {@link SendDocument }
     * 
     */
    public SendDocument createSendDocument() {
        return new SendDocument();
    }

    /**
     * Create an instance of {@link SendDocumentResponse }
     * 
     */
    public SendDocumentResponse createSendDocumentResponse() {
        return new SendDocumentResponse();
    }

    /**
     * Create an instance of {@link Fault }
     * 
     */
    public Fault createFault() {
        return new Fault();
    }

}
