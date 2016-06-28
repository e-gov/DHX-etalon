package ee.bpw.dhx.client.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;

import com.vaadin.annotations.Theme;
import com.vaadin.event.UIEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import ee.bpw.dhx.client.CustomAppender;
import ee.bpw.dhx.client.config.DhxClientConfig;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;
import ee.bpw.dhx.ws.service.DocumentService;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocumentResponse;

@SpringUI(path = "ui")
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
	
	@Autowired
	DhxGateway dhxGateway;
	
	private final String mainAppLabel = "Dokumendivahetusprotokolli DHX etalonteostus.";
	private final String sendDocumentHelp = "<span style=\"white-space:normal;\"><ul>"
			+ "<li>Dokumendi saadetakse kapsli sees olevatele adressaatidele. "
					+ "Enne saatmist kontrollitakse saajat(kas on lokaalses aadressinimistus olemas) ja valideeritakse kapsel(konfitav). "
					+ "Saatmiseks kasutatakse sendDocument xtee teenust.</li>"
			+ "<li>Saadetise id võib tühjaks jääda, siis rakendus paneb automaatselt juhusliku stringi saadetise ID-na. "
					+ "Kui on vaja testida ntks dublikaatide kontrolli, "
					+ "siis tuleb saadetise ID käsitsi panna ja mitu korda ütirada saata sama saadetise ID-ga.</li>"
			+ "<li>Dokumendi kapsel peab olema XML fail mis vastab Elektroonilise andmevahetuse metaandmete loendile 2.1</li>"
			+ "</ul></span>";
	private final String representationListHelp = "<ul>"
			+ "<li><span style=\"white-space:normal;\">Leitakse sisestatud X-tee liikme poolt vahendatavate asutuste nimekirja."
				+ " Kasutatakse xtee teenust representationList </span>"
			+ "</li></ul>";
	private final String description = "Dokumendivahetustaristu hajusarhitektuurile üleviimise väljatöötamine" 
				 + "<ul><li>Riigi Infosüsteemi Amet, 2016</li></ul>";
	private class DhxStreamSource implements StreamSource {
		
		String filePath;
		
		public DhxStreamSource(String filePath) {
			this.filePath =  filePath;
		}
		
		@Override
		public InputStream getStream() {
			try{
				return FileUtil.getFileAsStream(filePath);
			}catch(DhxException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
		
	}
	
	@Override
	protected void init(VaadinRequest request) {
		setPollInterval(config.getLogRefresh());
		getTooltipConfiguration().setOpenDelay(5);
		Label mainLabel = new Label(mainAppLabel);
		mainLabel.setStyleName("h1");
		StreamResource source = new StreamResource(new DhxStreamSource("jar://EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg") , "help.png");
		Image image = new Image("", source);
		image.setStyleName("v-table-footer-container");
		GridLayout headerLayout = new GridLayout(2, 2);
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		//headerLayout.setMargin(true);
		headerLayout.addComponent(mainLabel);
		headerLayout.addComponent(image, 1, 0, 1, 1);
		headerLayout.addComponent(getInfo());
		headerLayout.setColumnExpandRatio(0, 0.85f);
		headerLayout.setColumnExpandRatio(1, 0.15f);
		//Layout info = getInfo();
		GridLayout formKonfLayout = new GridLayout(2, 1);
		//formKonfLayout.setMargin(true);
		formKonfLayout.setSpacing(true);
		formKonfLayout.setWidth(100, Unit.PERCENTAGE);
		formKonfLayout.addComponent(getActivityAsTab());	
		formKonfLayout.addComponent(getConfAsTab());	
		VerticalLayout mainLayout = new VerticalLayout(headerLayout, /*info,*/ formKonfLayout, getLog());
		setContent(mainLayout);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

	}
	
	private Layout getConfAsTab () {
		VerticalLayout layout = new VerticalLayout();
		Label settingsLabel = new Label("Seadistused");
		settingsLabel.setStyleName("h2");
		TabSheet settings = new TabSheet();
		settings.addTab(getConf(), "Rakenduse konf");
		settings.addTab(getAdresseeList(), "Lokaalne aadressiraamat");
		layout.addComponent(settingsLabel);
		layout.addComponent(settings);
		return layout;
	}
	
	private Layout getActivityAsTab () {
		VerticalLayout layout = new VerticalLayout();
		Label activityLabel = new Label("Tegevused");
		activityLabel.setStyleName("h2");
		TabSheet activity = new TabSheet();
		activity.addTab(getSendDocumentLayout(), "Dokumendi saatmine");
		activity.addTab(getRepresentationListLayout(), "Vahendatavate nimekiri");
		layout.addComponent(activityLabel);
		layout.addComponent(activity);
		return layout;
	}
	
	private Layout getInfo(){
		VerticalLayout layout = new VerticalLayout();
		Label mainLabel2 = new Label();
		mainLabel2.setCaptionAsHtml(true);
		mainLabel2.setCaption("<span style=\"white-space:normal;\">" + description + config.getInfo() + "</span>");
		//mainLabel2.setStyleName("h2");
		layout.addComponent(mainLabel2);
		Label info  = new Label("");
		info.setWidth(100, Unit.PERCENTAGE); 
		info.setCaptionAsHtml(true);
		//info.setValue(config.getInfo());
		layout.addComponent(info);
		StreamResource source = new StreamResource(new DhxStreamSource(config.getCapsuleTestFile()) , "capsule.xml");
		FileDownloader fileDownloader = new FileDownloader(source);
		Link link = new Link("Lae alla näidiskapsel testimiseks", null);
		fileDownloader.extend(link);
		//link.setTargetName("_blank");
		layout.addComponent(link);
		ExternalResource wsdlResource = new ExternalResource("/ws/dhx.wsdl");
		Link linkwsdl = new Link("Pakutavate teenuste wsdl ", wsdlResource);
		layout.addComponent(linkwsdl);
		return layout;
	}
	
	private Component addTooltip (Label component, String header, String description) {
		//GridLayout layout = new GridLayout(2, 1);
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(false);
		layout.setSpacing(false);
		//layout.setMargin(true);
		//StreamResource source = new StreamResource(new DhxStreamSource("jar://help.png") , "help.png");
		//Image image = new Image("", source);
		//image.setHeight(15, Unit.PIXELS);
		//.setWidth(15, Unit.PIXELS);
		component.setDescription("<div>" + description + "</div");
		//image.setStyleName("v-gridlayout-spacing");
		//layout.addComponent(component);
		//layout.addComponent(image);
		//component.setIcon(source);
		//new Label().setContentMode(ContentMode.HTML);
		return component;
	}
	
	private Layout getLog() { 
		try {
		VerticalLayout layout = new VerticalLayout();
		/*ClassResource res = new ClassResource("try.jpg");
		//ClassPathResource res2 =  new ClassPathResource("try.jpg");
		InputStream is = res.getStream().getStream();
		//InputStream is2 = res2.getInputStream();
		Image image = new Image("", res);
		FileResource ff = new FileResource(new File(""));
		ThemeResource res3 = new ThemeResource("../dhx/try.jpg");
		Image image2 = new Image("", res3);*/
		/******button*****/
		/*StreamResource reee = new StreamResource(new DhxStreamSource("jar://try.jpg") , "try.jpg");
		Image image3 = new Image("", reee);
		
		StreamResource reee3 = new StreamResource(new DhxStreamSource("/resources/quest.png") , "try2.png");
		Image image33 = new Image("", reee3);*/
		/*buttonClear
         .setDescription("<h2><img src=\"../VAADIN/themes/sampler/icons/comment_yellow.gif\"/>A richtext tooltip</h2>"
                 + "<ul>"
                 + "<li>HTML formatting</li><li>Images<br/>"
                 + "</li><li>etc...</li></ul>");*/
		/*************/
		
		/*******text area******/
		final TextArea text = new TextArea();
		text.setSizeFull();
		text.setValue(CustomAppender.getLastEvents());
		text.setEnabled(false);
		text.setHeight(500, Unit.PIXELS);
		Button buttonClear = new Button("Tühista logi");
		buttonClear.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		      CustomAppender.deleteLastEvents();
		      text.setValue("");
		    }
		});
		addPollListener(new UIEvents.PollListener() {
	        @Override
	        public void poll(UIEvents.PollEvent event) {
	           // log.error("Polling");
	            text.setValue(CustomAppender.getLastEvents());
	        }
	    });
		Label label = new Label("Viimased sündmused");
		label.setStyleName("h2");
		layout.addComponent(label);
		layout.addComponent(buttonClear);
		layout.addComponent(text);
		//layout.addComponent(image);
		//layout.addComponent(image2);
		/*layout.addComponent(image3);
		layout.addComponent(image33);*/
		return layout;
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	 private Layout getConf () {		
		GridLayout gridLayout = new GridLayout(2, 6);
		gridLayout.setMargin(true);
		gridLayout.setSpacing(true);
		gridLayout.addComponent(addTooltip(new Label("Sündmusi logitakse:"), "Sündmusi logitakse", "Mitu sündmust kuvatakse 'Viimased sündmused' regioonis"));
		gridLayout.addComponent(new Label(config.getLogMaxSize().toString()));
		gridLayout.addComponent(addTooltip(new Label("Logi uuendatakse(millisekundites): "), "Logi uuendatakse(millisekundites)", "Kui tihti uuendatakse regioon 'Viimased sündmused'"));
		gridLayout.addComponent(new Label(config.getLogRefresh().toString()));
		gridLayout.addComponent(addTooltip(new Label("Vahendatavate nimekiri: "), "Vahendatavate nimekir", "Nimekiri asutustest keda vahendab antud xtee liige"));
		gridLayout.addComponent(new Label(config.getRepresentatives()));

		
		gridLayout.addComponent(addTooltip(new Label("Kapsel valideeritakse: "), "Kapsel valideeritakse", "Kas kaspli on vaja valideerida vastu XSD skeema"));
		gridLayout.addComponent(new Label( dhxConfig.getCapsuleValidate().toString()));

		
		gridLayout.addComponent(addTooltip(new Label("Turvaserver: "), "Turvaserver", "Turvaserveri asukoht"));
		gridLayout.addComponent(new Label(soapConfig.getSecurityServer()));
		gridLayout.addComponent(addTooltip(new Label("Xtee liige: "), "Xtee liige", "Xtee liikme andmed"));
		gridLayout.addComponent(new Label(soapConfig.getXroadInstance() + "/" + soapConfig.getMemberClass() + "/" + soapConfig.getMemberCode() + "/" + soapConfig.getSubsystem()));
		
		//Label label = new Label("Rakenduse konf: ");
		//label.setStyleName("h3");	
		VerticalLayout layout = new VerticalLayout();
		//layout.addComponent(label);
		layout.addComponent(gridLayout);
		return layout;
	 }
	 	
	private Layout getSendDocumentLayout () {
	//	Label formLabel = new Label("Dokumendi saatmine");
	//	formLabel.setStyleName("h3");
		final Label chosenFile = new Label();
		/******upload field*****/
		final UploadField uploadField = new UploadField();
		uploadField.setBuffered(true);
		uploadField.setFieldType(FieldType.BYTE_ARRAY);
		uploadField.setDisplayUpload(false);
		uploadField.setCaption("Dokumendi kapsel");
		uploadField.setButtonCaption("Vali fail");
		uploadField.setFieldType(FieldType.FILE);
		uploadField.setFileFactory(new FileFactory() {
            public File createFile(String fileName, String mimeType) {
            	try{
            		log.debug("creating file for uploaded file.");
            		File f = FileUtil.createPipelineFile(0, "");
            		chosenFile.setValue("valitud fail: " + fileName);
	                return f;
            	} catch(IOException e) {
            		return null;
            	}
            }
        });
		final TextField consignmentId = new TextField();
		consignmentId.setCaption("Saadetise id");
		Button buttonSubmit = new Button("Saada document");
		buttonSubmit.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		    	try {
		    		List<SendDocumentResponse> responses = documentService.sendDocument(uploadField.getContentAsStream(), consignmentId.getValue());
		    		String statuses = "";
		    		for(SendDocumentResponse response : responses) {
		    			statuses += "Dokument saadetud. Status:" + response.getReceiptId()
								+ (response.getFault()==null?"":" faultCode:" + response.getFault().getFaultCode() + " faultString:" + response.getFault().getFaultString() + "\n'");
		    		//showNotification();
		    		}
		    		Notification notification = new Notification("Dokuemndi saatmise staatused:" + statuses, Notification.Type.HUMANIZED_MESSAGE);
		    		notification.setDelayMsec(-1);
		    		notification.show(Page.getCurrent());
		    	}catch(DhxException ex) {
		    		log.error("Error while sending document." + ex.getMessage(), ex);
		    		Notification notification = new Notification("Viga documendi saatmisel!" + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
		    		notification.setDelayMsec(-1);
		    		notification.show(Page.getCurrent());
		    		//showNotification("Viga documendi saatmisel!" + ex.getMessage());
		    	}
		    }
		});
		Label help = new Label();
		help.setCaptionAsHtml(true);
		help.setCaption(sendDocumentHelp);
		FormLayout formLayout = new FormLayout(/*formLabel,*/ help, consignmentId, uploadField, buttonSubmit, chosenFile);
		formLayout.setMargin(false);
		VerticalLayout vertLayout = new VerticalLayout();
		vertLayout.addComponent(help);
		vertLayout.addComponent(formLayout);
		return vertLayout;
	}
	
	private Layout getRepresentationListLayout () {
		final TextField regClass= new TextField();
		regClass.setCaption("Asutuse klass");
		final TextField regCode = new TextField();
		regCode.setCaption("Asutuse kood");
		Button button = new Button("Saada päring");
		button.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		    	try {
		    		log.debug("getting representation List");
		    		XroadMember member = new XroadMember(soapConfig.getXroadInstance(), regClass.getValue(), regCode.getValue(), soapConfig.getSubsystem(), null);
		    		RepresentationListResponse response = dhxGateway.getRepresentationList(member);
		    		String reprStr = "";
		    		for(String repr : response.getMemberCodes().getMemberCode()) {
		    			reprStr = reprStr + (reprStr.equals("")?"":", ") + repr;
		    		}
		    		Notification notification = new Notification("Representatives:" + reprStr, Notification.Type.HUMANIZED_MESSAGE);
		    		notification.setDelayMsec(-1);
		    		notification.show(Page.getCurrent());
		    	}catch(DhxException ex) {
		    		log.error("Error while sending document." + ex.getMessage(), ex);
		    		Notification notification = new Notification("Viga documendi saatmisel!" + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
		    		notification.setDelayMsec(-1);
		    		notification.show(Page.getCurrent());
		    	}
		    }
		});
		//Label label = new Label("Vahendatavate nimekiri");
		//label.setStyleName("h3");
		FormLayout formLayout = new FormLayout(/*label,*/ regClass, regCode, button);
		formLayout.setMargin(false);
		Label help = new Label();
		help.setCaptionAsHtml(true);
		help.setCaption(representationListHelp);
		VerticalLayout vertLayout = new VerticalLayout();
		vertLayout.addComponent(help);
		vertLayout.addComponent(formLayout);
		return vertLayout;
	}

	
	 private Layout getAdresseeList(){
		 VerticalLayout adresseeLayout = new VerticalLayout();
		 // Label label = new Label("Lokaalne aadressiraamat: \n");
		 //label.setStyleName("h3");
			//adresseeLayout.addComponent(label);
			List<XroadMember> members = addressService.getAdresseeList();
			final Label adrLabel = new Label("");
			adrLabel.setCaptionAsHtml(true);
			adrLabel.setCaption(getAdresseeString(members));
			adresseeLayout.addComponent(adrLabel);
			Button button = new Button("Uuenda");
			button.addListener(new Button.ClickListener() {
			    public void buttonClick(ClickEvent event) {
			    	
			    		log.debug("renewing address list");
			    		addressService.renewAddressList();
			    		List<XroadMember> members = addressService.getAdresseeList();
			    		adrLabel.setCaption(getAdresseeString(members));
			    		Notification notification = new Notification("Lokaalne aadressiraamat uuendatud", Notification.Type.HUMANIZED_MESSAGE);
			    		notification.setDelayMsec(-1);
			    		notification.show(Page.getCurrent());
			    	
			    }
			});
			adresseeLayout.addComponent(button);
			return adresseeLayout;		 
	 }
	 
	 private String getAdresseeString (List<XroadMember> members) {
		 String labelAdr = "";
			for(XroadMember member : members) {
				labelAdr += "</br>" + member.toString().replace("addressee", "adressaat").replace("X-road member", "X-tee liige").replace("is representee", "kas vahendatav") +"\n";
			}
		return labelAdr;
	 }

	
}
