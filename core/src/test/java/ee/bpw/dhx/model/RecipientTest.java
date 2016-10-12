package ee.bpw.dhx.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecipientTest {
  
  
  @Test
  public void testEquals () {
    Recipient first = new Recipient ("code", "system", "DHX");
    Recipient second = new Recipient ("code", "system", "DHX");
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
    
    second.setSystem("DHX.system");   
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
    
    first.setSystem("DHX.system");
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
    
    first.setSystem("TEST");
    assertFalse(first.equals(second));
    assertFalse(second.equals(first));
    
    first.setSystem(null);
    assertFalse(first.equals(second));
    assertFalse(second.equals(first));
    
    second.setSystem(null);
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
  }
  
  @Test
  public void testEqualsToCapsuleRecipient () {
    Recipient first = new Recipient ("code", "system", "DHX");
    String capsuleRecipient = "code";
    
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
   
    capsuleRecipient = "system";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "DHX.system";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "system.code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "DHX.system.code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    
    capsuleRecipient = "code1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "system1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "DHX.system1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "system.code1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "DHX.system1.code";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    first.setSystem(null);
    capsuleRecipient = "code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
    
    capsuleRecipient = "DHX.code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient));
  }

}
