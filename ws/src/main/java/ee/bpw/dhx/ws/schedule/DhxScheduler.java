package ee.bpw.dhx.ws.schedule;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;

import org.apache.axis.AxisFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;


@Slf4j
@Service
@Configurable
public class DhxScheduler {
  

  @Autowired
  AddressService addressService;
  
  @Autowired
 DhxImplementationSpecificService dhxImplementationSpecificService;
  
  /**
   * Sends documents periodically.
   * 
   * @throws DhxException - thrown if error occures while sending document
   */
  @Scheduled(cron = "${document-send-timeout}")
  public void sendDvkDocuments() throws DhxException {
      log.debug("sending DHX documents automatically");
      dhxImplementationSpecificService.resendFailedDocuments();
  }
  
  @Scheduled(cron = "${address-renew-timeout}")
  public void renewAddressList() throws DhxException {
      log.debug("updating address DHX list automatically");
      addressService.renewAddressList();
  }

}
