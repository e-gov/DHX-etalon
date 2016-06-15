package ee.bpw.dhx.client.ui;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;

import com.vaadin.annotations.Theme;
import com.vaadin.event.UIEvents;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import ee.bpw.dhx.client.CustomAppender;
import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.service.DocumentService;
import eu.x_road.dhx.producer.SendDocumentResponse;

@SpringUI
@Theme("valo")
@Slf4j
public class DhxUI extends UI {

	@Autowired
	DocumentService documentService;
	

	@Autowired
	DhxClientConfig config;
	
	@Override
	protected void init(VaadinRequest request) {
		setPollInterval(config.getLogRefresh());
		/******upload field*****/
		final UploadField uploadField = new UploadField();
		uploadField.setBuffered(true);
		uploadField.setFieldType(FieldType.BYTE_ARRAY);
		uploadField.setDisplayUpload(false);
		uploadField.setCaption("Dokumendi kapsel");
		uploadField.setFileFactory(new FileFactory() {
            public File createFile(String fileName, String mimeType) {
            	try{
            		File f = FileUtil.createPipelineFile(0, "");
	                return f;
            	} catch(IOException e) {
            		return null;
            	}
            }
        });
		/******************/
		
		/******text field*****/
		final TextField field = new TextField();
		field.setCaption("Aadressaadi kood");
		/***************/
		
		/*******form*********/
		final Form form = new Form();
		form.setCaption("Dokumendi saatmine");
		form.addField("recipient", field);
		form.addField("file", uploadField);
		/*****************/
		
		
		/******button*****/
		Button buttonSubmit = new Button("Saada document");
		buttonSubmit.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        form.commit();
		    	Object value = uploadField.getValue();
                log.error("Value:" + value);
		    	log.error("SUBMITED!!!!");
		    	//text.setEnabled(false);
		    	log.error(value.getClass().getCanonicalName());
		    	try {
		    		SendDocumentResponse response = documentService.sendDocument(new DhxDocument(field.getValue(), uploadField.getContentAsStream()));
		    		showNotification("Dokument saadetud. Status:" + response.getReceiptId()
						+ (response.getFault()==null?"":" faultCode:" + response.getFault().getFaultCode() + " faultString:" + response.getFault().getFaultString()));
		    	}catch(DhxException ex) {
		    		log.error("Error while sending document." + ex.getMessage(), ex);
		    		showNotification("Viga documendi saatmisel!" + ex.getMessage());
		    	}
		    }
		});
		/*************/
		
		/*******text area******/
		final TextArea text = new TextArea();
		text.setSizeFull();
		text.setCaption("Viimased sündmused");
		text.setValue(CustomAppender.getLastEvents());
		text.setEnabled(false);
		text.setHeight(500, UNITS_PIXELS);
		addPollListener(new UIEvents.PollListener() {
	        @Override
	        public void poll(UIEvents.PollEvent event) {
	           // log.error("Polling");
	            text.setValue(CustomAppender.getLastEvents());
	        }
	    });
		/****************/
		
		VerticalLayout mainLayout = new VerticalLayout(form, buttonSubmit, text);
		setContent(mainLayout);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

	}

	
}
