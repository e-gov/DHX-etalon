package ee.bpw.dhx.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.service.DocumentService;

@Component
@Slf4j
public class ScheduledClient {

	@Autowired
	DocumentService documentService;
	
	@Autowired
	DhxClientConfig config;

	
    @Scheduled(fixedDelayString = "${dhx.client.auto-send-frequency}")
    public void sendValidDocument() throws DhxException, IOException, FileNotFoundException{
    	InputStream stream = null;
    	try{
    		stream = FileUtil.getFileAsStream(config.getCapsuleTestFile());
    		log.debug("sending document automatically");
	    	// DhxDocument document = new DhxDocument(config.getJobRecipient(), stream, false);
	    	 documentService.sendDocument(stream, null);
    	}catch (DhxException e) {
    		log.error(e.getMessage(), e);
    	}finally {
    		FileUtil.safeCloseStream(stream);
    	}
    }
}