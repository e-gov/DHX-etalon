package ee.bpw.dhx.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents recipient of the document.
 * 
 * @author Aleksei Kokarev
 *
 */
@Setter
public class Recipient {

  private String code;
  private String system;
  private String dhxSubsystemPrefix;

  /**
   * Recipient consctructor.
   * 
   * @param code - code of the reciepint. might be either X-road memberCode or representees code
   * @param system - system of the recipient. migth be either X-road subSystemCode or representees
   *        system
   * @param dhxSubSytemPrefix - prefix for DHX subsystems, needed to check for equality if in some
   *        cases prefix is not added
   */
  public Recipient(String code, String system, String dhxSubSytemPrefix) {
    this.code = code;
    this.system = system;
    this.dhxSubsystemPrefix = dhxSubSytemPrefix;
  }

  /**
   * Recipient consctructor.
   * 
   * @param dhxSubSytemPrefix - prefix for DHX subsystems, needed to check for equality if in some
   *        cases prefix is not added
   */
  public Recipient(String dhxSubSytemPrefix) {
    this.dhxSubsystemPrefix = dhxSubSytemPrefix;
  }

  public String getCode() {
    return code.toUpperCase();
  }

  public String getSystem() {
    if(system == null) {
      return null;
    }
    return system.toUpperCase();
  }

  private String getNotNullSystem(){
    if(system == null) {
      return "";
    }
    return system.toUpperCase();
  }
  
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Recipient))
      return false;
    if (obj == this)
      return true;
    Recipient recipient = (Recipient) obj;
    if (recipient.getCode().equals(this.getCode())
        && recipient.getAdaptedSystem().equals(this.getAdaptedSystem())) {
      return true;
    }
    return false;
  }

  /**
   * add prefix to system if it is not present.
   * @return - adpted system
   */
  private String getAdaptedSystem() {
    String adaptedSystem = system;
    if (adaptedSystem == null) {
      adaptedSystem = dhxSubsystemPrefix;
    }
    if (!adaptedSystem.startsWith(dhxSubsystemPrefix + ".") && !adaptedSystem.equals(dhxSubsystemPrefix)) {
      adaptedSystem = dhxSubsystemPrefix + "." + adaptedSystem;
    }
    return adaptedSystem.toUpperCase();
  }

  /**
   * Function to check capsule recipient. Accepts if capsule recipient equals to code or system or
   * their combination either with or without DHX subsystem prefix.
   * 
   * @param capsuleRecipient
   * @return
   */
  public Boolean equalsToCapsuleRecipient(String capsuleRecipient) {
    String capsuleRecipientUp = capsuleRecipient.toUpperCase();
    if (capsuleRecipientUp.equals(getCode())
        || capsuleRecipientUp.equals(getNotNullSystem() + "." + getCode())
        || capsuleRecipientUp.equals(getAdaptedSystem() + "." + getCode())
        || capsuleRecipientUp.equals(getNotNullSystem())
        || capsuleRecipientUp.equals(getAdaptedSystem())) {
      return true;
    }
    return false;
  }
  
  @Override
  public String toString() {
    return "code: " + code
        + ", system: " + system;
  }


}
