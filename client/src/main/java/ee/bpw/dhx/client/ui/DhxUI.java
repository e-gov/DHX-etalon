package ee.bpw.dhx.client.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import ee.bpw.dhx.client.CustomAppender;
import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.util.XsdUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DocumentService;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import eu.x_road.dhx.producer.SendDocumentResponse;

@SpringUI
@Theme("valo")
@Slf4j
public class DhxUI extends UI {

	@Autowired
	DocumentService documentService;
	

	@Autowired
	DhxClientConfig config;
	
	@Autowired
	DhxConfig dhxConfig;
	
	@Autowired
	SoapConfig soapConfig;
	
	@Autowired
	AddressService addressService;
	
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
		final TextField consignmentId = new TextField();
		consignmentId.setCaption("Consignment id");
		/***************/

		
		
		/******button*****/
		Button buttonSubmit = new Button("Saada document");
		buttonSubmit.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		    	try {
		    		List<SendDocumentResponse> responses = documentService.sendDocument(uploadField.getContentAsStream(), consignmentId.getValue());
		    		for(SendDocumentResponse response : responses) {
		    		showNotification("Dokument saadetud. Status:" + response.getReceiptId()
						+ (response.getFault()==null?"":" faultCode:" + response.getFault().getFaultCode() + " faultString:" + response.getFault().getFaultString()));
		    		}
		    	}catch(DhxException ex) {
		    		log.error("Error while sending document." + ex.getMessage(), ex);
		    		showNotification("Viga documendi saatmisel!" + ex.getMessage());
		    	}
		    }
		});
		
		/******button*****/
		Button buttonClear = new Button("Tühista logi");
		buttonClear.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		      CustomAppender.deleteLastEvents();
		    }
		});
		/*************/
		
		/*******text area******/
		final TextArea text = new TextArea();
		text.setSizeFull();
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
		Label textArea = new Label("Viimased sündmused");
		textArea.setStyleName("h3");
		Label formLabel = new Label("Dokumendi saatmine");
		formLabel.setStyleName("h3");
		/****************/

		Label mainLabel = new Label("Rakendus: " + config.getName());
		mainLabel.setStyleName("h2");
		Label label = new Label("Rakenduse konf: ");
		label.setStyleName("h3");
			
		
		GridLayout gridLayout = new GridLayout(2, 6);
		gridLayout.setMargin(true);
		gridLayout.setSpacing(true);
		
		gridLayout.addComponent(new Label("Sündmusi logitakse:"));
		gridLayout.addComponent(new Label(config.getLogMaxSize().toString()));
		gridLayout.addComponent(new Label("Logi uuendatakse(millisekundites): "));
		gridLayout.addComponent(new Label(config.getLogRefresh().toString()));
		gridLayout.addComponent(new Label("Vahendatavate nimekiri: "));
		gridLayout.addComponent(new Label(config.getRepresentatives()));

		
		gridLayout.addComponent(new Label("Kapsel valideeritakse: "));
		gridLayout.addComponent(new Label( dhxConfig.getCapsuleValidate().toString()));

		
		gridLayout.addComponent(new Label("Turvaserver: "));
		gridLayout.addComponent(new Label(soapConfig.getSecurityServer()));
		gridLayout.addComponent(new Label("Xtee memberCode: "));
		gridLayout.addComponent(new Label(soapConfig.getMemberCode()));
		
		gridLayout.addComponent(new Label("Adressaatide list(static): "));
		VerticalLayout adresseeLayout = new VerticalLayout();
		List<XroadMember> members = addressService.getAdresseeList();
		for(XroadMember member : members) {
			adresseeLayout.addComponent(new Label(member.toString()));
		}
		gridLayout.addComponent(adresseeLayout);
		
		VerticalLayout confLayout = new VerticalLayout();
		confLayout.addComponent(label);
		confLayout.addComponent(gridLayout);
		
		FormLayout formLayout = new FormLayout(formLabel, consignmentId, uploadField, buttonSubmit);
		
		GridLayout formKonfLayout = new GridLayout(2, 1);
		formKonfLayout.setMargin(true);
		formKonfLayout.setSpacing(true);
		formKonfLayout.setWidth(100, UNITS_PERCENTAGE);
		formKonfLayout.addComponent(formLayout);
		formKonfLayout.addComponent(confLayout);
		
		VerticalLayout mainLayout = new VerticalLayout(mainLabel, formKonfLayout, textArea, buttonClear, text);
		setContent(mainLayout);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

	}

	
}
