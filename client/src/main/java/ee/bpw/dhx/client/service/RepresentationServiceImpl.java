package ee.bpw.dhx.client.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.ws.service.RepresentationService;

@Slf4j
public class RepresentationServiceImpl implements RepresentationService{

	@Autowired
	private DhxClientConfig dhxConfig;
	
	//get log4j logger to log events on custom level.
	final Logger logger = LogManager.getLogger();
	

	@Override
	public List<String> getRepresentationList() throws DhxException {
		String memberCodesStr ="";
		logger.log(Level.getLevel("EVENT"), "Staring returning representationList");
		List<String> list = dhxConfig.getRepresentativesList();
		for(String representative : list) {	
			memberCodesStr += (memberCodesStr==""?"":", ") + representative;
		}
		logger.log(Level.getLevel("EVENT"), "Returning representationList. " + memberCodesStr);
		return list;
	}


}
