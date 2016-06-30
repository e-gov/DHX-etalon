package ee.bpw.dhx.client;

import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.service.DocumentService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component
@Slf4j
public class ScheduledClient {

  @Autowired
  DocumentService documentService;

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
      file = FileUtil.getFile(config.getCapsuleTestFile());
      log.debug("sending document automatically");
      // DhxDocument document = new DhxDocument(config.getJobRecipient(), stream, false);
      documentService.sendDocument(file, null);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
    }
  }
}
