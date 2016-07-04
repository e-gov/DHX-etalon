package ee.bpw.dhx.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapsuleAdressee {

  public CapsuleAdressee(String adresseeCode) {
    this.adresseeCode = adresseeCode;
  }

  String adresseeCode;

}
