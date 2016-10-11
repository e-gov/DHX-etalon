package ee.bpw.dhx.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents recipient of the document.
 * 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
public class Recipient {

  private String code;
  private String system;

}
