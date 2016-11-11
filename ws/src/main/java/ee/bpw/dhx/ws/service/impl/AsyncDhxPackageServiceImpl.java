package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;





import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.AsyncDhxSendDocumentResult;
import ee.bpw.dhx.model.DhxSendDocumentResult;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.model.OutgoingDhxPackage;
import ee.bpw.dhx.util.CapsuleVersionEnum;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.service.AsyncDhxPackageService;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxPackageService;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class AsyncDhxPackageServiceImpl implements AsyncDhxPackageService{

  @Autowired
  DhxPackageService dhxPackageService;
  
  @Autowired
  DhxImplementationSpecificService dhxImplementationSpecificService;
  
  private List<Integer> resendTimeouts = new ArrayList<Integer>();
  
  @Autowired
  DhxConfig config;
  
  @PostConstruct
  public void init () {
    this.resendTimeouts = config.getDocumentResendTimes();
  }
  
  
  private Integer getNextTimeout (Integer retryCount){
    if(resendTimeouts != null && resendTimeouts.size()>retryCount) {
      return resendTimeouts.get(retryCount);
    }
    return null;
  }
  
  @Async
  @Loggable
  @Override
  public void sendPackage(OutgoingDhxPackage outgoingPackage){
    Integer currentRetry = 0;
    Integer currentTimeout;
    List<AsyncDhxSendDocumentResult> results = new ArrayList<AsyncDhxSendDocumentResult>();
    DhxSendDocumentResult result;
    try {
      do {
        log.info("Trying to send package in async mode");
        result = sendPackageTry(outgoingPackage);
        results.add(new AsyncDhxSendDocumentResult(result));
        if(result.getOccuredException() != null 
            && result.getOccuredException() instanceof DhxException
            && ((DhxException)result.getOccuredException()).getExceptionCode().isBusinessException()) {
          log.info("Business exception occured while doing asynchronous sending. No need to continue retries, doing callback.");
          dhxImplementationSpecificService.saveSendResult(result, results);
          return;
        }
        if (result.getResponse().getFault() == null) {
          log.info("Calling callback method. Total retry count: " + currentRetry);
          dhxImplementationSpecificService.saveSendResult(result, results);
          return;
        }
        currentTimeout = getNextTimeout(currentRetry);
        currentRetry++;
        if ( currentTimeout != null) {
          Thread.sleep(currentTimeout*1000L);
        }
      } while (currentTimeout != null);
      log.info("Sending was unsuccessfull. Calling callback method. Total retry count: " + currentRetry);
      dhxImplementationSpecificService.saveSendResult(result, results);
      return;
    }catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      SendDocumentResponse response = new SendDocumentResponse();
      Fault fault = new Fault();
      fault.setFaultCode(DhxExceptionEnum.TECHNICAL_ERROR.getCodeForService());
      fault.setFaultString("Error occured while sending document acynchronously. " + ex.getMessage());
      response.setFault(fault);
      result = new DhxSendDocumentResult(outgoingPackage, response);
      results.add(new AsyncDhxSendDocumentResult(result));
      result.setOccuredException(ex);
      dhxImplementationSpecificService.saveSendResult(result, results);
    }
  }


  @Loggable
  private DhxSendDocumentResult sendPackageTry (OutgoingDhxPackage outgoingPackage) {
    DhxSendDocumentResult result = null; 
    try {
      result = dhxPackageService.sendPackage(outgoingPackage);
    } catch (Exception ex) {
      log.info("Package sending ended with error" + ex.getMessage(), ex);
      DhxExceptionEnum faultCode = DhxExceptionEnum.TECHNICAL_ERROR;
      if (ex instanceof DhxException) {
        if (((DhxException) ex).getExceptionCode() != null) {
          faultCode = ((DhxException) ex).getExceptionCode();
        }
      }
      SendDocumentResponse response = new SendDocumentResponse();
      Fault fault = new Fault();
      fault.setFaultCode(faultCode.getCodeForService());
      fault.setFaultString(ex.getMessage());
      response.setFault(fault);
      result = new DhxSendDocumentResult(outgoingPackage, response);
      result.setOccuredException(ex);
    }
    return result;
  }

  @Override
  @Loggable
  public void sendMultiplePackages(
      List<OutgoingDhxPackage> outgoingPackages) {
    for (OutgoingDhxPackage outPackage : outgoingPackages) {
      sendPackage(outPackage);
    }
  }
}
