package ee.ria.dhx.client;

import ee.ria.dhx.client.config.DhxClientConfig;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.ws.service.DhxPackageService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Periodically sends documents.
 * 
 * @author Aleksei Kokarev
 *
 */
@Component
@Slf4j
public class ScheduledClient {

  @Autowired
  DhxPackageService documentService;

  @Autowired
  DhxPackageProviderService dhxPackageProviderService;

  @Autowired
  DhxClientConfig config;


  /**
   * Sends documents periodically.
   * 
   * @throws DhxException - thrown if error occures while sending document
   */
  @Scheduled(fixedDelayString = "${dhx.client.auto-send-frequency}")
  public void sendValidDocument() throws DhxException {
    File file = null;
    try {
      file = FileUtil.getFile(config.getCapsuleCorrect());
      log.debug("sending document automatically");
      documentService.sendMultiplePackages(dhxPackageProviderService.getOutgoingPackage(file,
          null));
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
    }
  }
}
