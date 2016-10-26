package ee.bpw.dhx.model;

import lombok.Getter;

import java.util.Date;
import lombok.Setter;

@Getter
@Setter
public class AsyncDhxSendDocumentResult {
  
  public AsyncDhxSendDocumentResult(DhxSendDocumentResult result) {
    this.result = result;
    this.tryDate = new Date();
  }
  
  DhxSendDocumentResult result;
  Date tryDate;  

}
