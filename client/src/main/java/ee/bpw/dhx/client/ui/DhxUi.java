package ee.bpw.dhx.client.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.UIEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
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
import ee.bpw.dhx.client.service.DocumentClientServiceImpl;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.Representee;
import ee.bpw.dhx.model.XroadMember;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxGateway;

import eu.x_road.dhx.producer.Member;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;


@SpringUI
@Theme("valo")
@Slf4j
public class DhxUi extends UI {
  private static final long serialVersionUID = -6857112166321059475L;

  @Autowired
  DocumentClientServiceImpl documentClientService;


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
  private final String description =
      "Dokumendivahetustaristu hajusarhitektuurile üleviimise väljatöötamine"
          + "<ul><li>Riigi Infosüsteemi Amet, 2016</li></ul>";

  private class DhxStreamSource implements StreamSource {

    private static final long serialVersionUID = -6857112166321059475L;

    String filePath;

    public DhxStreamSource(String filePath) {
      this.filePath = filePath;
    }

    @Override
    public InputStream getStream() {
      try {
        return FileUtil.getFileAsStream(filePath);
      } catch (DhxException ex) {
        log.error(ex.getMessage(), ex);
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
    StreamResource source =
        new StreamResource(new DhxStreamSource(
            "jar://EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg"), "help.png");
    Image image = new Image("", source);
    image.setStyleName("v-table-footer-container");
    GridLayout headerLayout = new GridLayout(2, 2);
    headerLayout.setWidth(100, Unit.PERCENTAGE);
    // headerLayout.setMargin(true);
    headerLayout.addComponent(mainLabel);
    headerLayout.addComponent(image, 1, 0, 1, 1);
    headerLayout.addComponent(getInfo());
    headerLayout.setColumnExpandRatio(0, 0.85f);
    headerLayout.setColumnExpandRatio(1, 0.15f);
    // Layout info = getInfo();
    GridLayout formKonfLayout = new GridLayout(2, 1);
    // formKonfLayout.setMargin(true);
    formKonfLayout.setSpacing(true);
    formKonfLayout.setWidth(100, Unit.PERCENTAGE);
    formKonfLayout.addComponent(getActivityAsTab());
    formKonfLayout.addComponent(getConfAsTab());
    VerticalLayout mainLayout =
        new VerticalLayout(headerLayout, /* info, */formKonfLayout, getLog());
    setContent(mainLayout);
    mainLayout.setMargin(true);
    mainLayout.setSpacing(true);

  }

  private Layout getConfAsTab() {
    Label settingsLabel = new Label("Seadistused");
    settingsLabel.setStyleName("h2");
    TabSheet settings = new TabSheet();
    settings.addTab(getConf(), "Rakenduse konf");
    settings.addTab(getAdresseeList(), "Lokaalne aadressiraamat");
    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(settingsLabel);
    layout.addComponent(settings);
    return layout;
  }

  private Layout getActivityAsTab() {
    Label activityLabel = new Label("Tegevused");
    activityLabel.setStyleName("h2");
    TabSheet activity = new TabSheet();
    activity.addTab(getSendDocumentLayout(), "Dokumendi saatmine");
    activity.addTab(getRepresentationListLayout(), "Vahendatavate nimekiri");
    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(activityLabel);
    layout.addComponent(activity);
    return layout;
  }

  private Layout getInfo() {
    VerticalLayout layout = new VerticalLayout();
    Label mainLabel2 = new Label();
    mainLabel2.setCaptionAsHtml(true);
    mainLabel2.setCaption("<span style=\"white-space:normal;\">" + description + config.getInfo()
        + "</span>");
    // mainLabel2.setStyleName("h2");
    layout.addComponent(mainLabel2);
    Label info = new Label("");
    info.setWidth(100, Unit.PERCENTAGE);
    info.setCaptionAsHtml(true);
    // info.setValue(config.getInfo());
    layout.addComponent(info);
    StreamResource source =
        new StreamResource(new DhxStreamSource(config.getCapsuleCorrect()),
            "DVKkapsel_korrektne.xml");
    FileDownloader fileDownloader = new FileDownloader(source);
    Link link = new Link("Lae alla korrektselt kapseldatud fail", null);
    fileDownloader.extend(link);
    // link.setTargetName("_blank");
    layout.addComponent(link);

    StreamResource source2 =
        new StreamResource(new DhxStreamSource(config.getCapsuleInvalid()), "DVKkapsel_vale.xml");
    FileDownloader fileDownloader2 = new FileDownloader(source2);
    Link link2 = new Link("Lae alla valesti kapseldatud fail", null);
    fileDownloader2.extend(link2);
    // link.setTargetName("_blank");
    layout.addComponent(link2);
    ExternalResource wsdlResource = new ExternalResource("/dhx/ws/dhx.wsdl");
    Link linkwsdl = new Link("Pakutavate teenuste wsdl ", wsdlResource);
    linkwsdl.setTargetName("_blank");
    layout.addComponent(linkwsdl);
    return layout;
  }

  private Component addTooltip(Label component, String header, String description) {
    // GridLayout layout = new GridLayout(2, 1);
    HorizontalLayout layout = new HorizontalLayout();
    layout.setMargin(false);
    layout.setSpacing(false);
    // layout.setMargin(true);
    // StreamResource source = new StreamResource(new DhxStreamSource("jar://help.png") ,
    // "help.png");
    // Image image = new Image("", source);
    // image.setHeight(15, Unit.PIXELS);
    // .setWidth(15, Unit.PIXELS);
    component.setDescription("<div>" + description + "</div");
    // image.setStyleName("v-gridlayout-spacing");
    // layout.addComponent(component);
    // layout.addComponent(image);
    // component.setIcon(source);
    // new Label().setContentMode(ContentMode.HTML);
    return component;
  }

  private Layout getLog() {
    try {
      final TextArea text = new TextArea();
      text.setSizeFull();
      text.setValue(CustomAppender.getLastEvents());
      text.setEnabled(false);
      text.setHeight(500, Unit.PIXELS);
      Button buttonClear = new Button("Tühista logi");
      buttonClear.addClickListener(new ClickListener() {
        private static final long serialVersionUID = -6857112166321059475L;

        @Override
        public void buttonClick(ClickEvent event) {
          CustomAppender.deleteLastEvents();
          text.setValue("");
        }
      });
      /*
       * buttonClear.addListener(new Button.ClickListener() { public void buttonClick(ClickEvent
       * event) { CustomAppender.deleteLastEvents(); text.setValue(""); } });
       */
      addPollListener(new UIEvents.PollListener() {
        private static final long serialVersionUID = -6857112166321059475L;

        @Override
        public void poll(UIEvents.PollEvent event) {
          // log.error("Polling");
          text.setValue(CustomAppender.getLastEvents());
        }
      });
      Label label = new Label("Viimased sündmused");
      label.setStyleName("h2");
      VerticalLayout layout = new VerticalLayout();
      layout.addComponent(label);
      layout.addComponent(buttonClear);
      layout.addComponent(text);
      // layout.addComponent(image);
      // layout.addComponent(image2);
      /*
       * layout.addComponent(image3); layout.addComponent(image33);
       */
      return layout;
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }

  private Layout getConf() {
    GridLayout gridLayout = new GridLayout(2, 6);
    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);
    gridLayout.addComponent(addTooltip(new Label("Sündmusi logitakse:"), "Sündmusi logitakse",
        config.getLogEventsHelp()));
    gridLayout.addComponent(new Label(config.getLogMaxSize().toString()));
    gridLayout.addComponent(addTooltip(new Label("Logi uuendatakse(millisekundites): "),
        "Logi uuendatakse(millisekundites)", config.getLogRefreshHelp()));
    gridLayout.addComponent(new Label(config.getLogRefresh().toString()));
    gridLayout.addComponent(addTooltip(new Label("Vahendatavate nimekiri: "),
        "Vahendatavate nimekiri", config.getRepresentativesHelp()));
    gridLayout.addComponent(new Label(config.getRepresentatives()));


    gridLayout.addComponent(addTooltip(new Label("Kapsel valideeritakse: "),
        "Kapsel valideeritakse", config.getValidateCapsuleHelp()));
    gridLayout.addComponent(new Label(dhxConfig.getCapsuleValidate().toString()));


    gridLayout.addComponent(addTooltip(new Label("Turvaserver: "), "Turvaserver",
        config.getSecurityServerHelp()));
    gridLayout.addComponent(new Label(soapConfig.getSecurityServer()));
    gridLayout.addComponent(addTooltip(new Label("Xtee liige: "), "Xtee liige",
        config.getXroadMemberHelp()));
    gridLayout.addComponent(new Label(soapConfig.getXroadInstance() + "/"
        + soapConfig.getMemberClass() + "/" + soapConfig.getMemberCode() + "/"
        + soapConfig.getSubsystem()));

    gridLayout.addComponent(addTooltip(new Label("Maksimaalne faili suurus: "),
        "Maksimaalne faili suurus", config.getMaxFileSizeHelp()));
    gridLayout.addComponent(new Label(dhxConfig.getMaxFileSize().toString()));

    // Label label = new Label("Rakenduse konf: ");
    // label.setStyleName("h3");
    VerticalLayout layout = new VerticalLayout();
    // layout.addComponent(label);
    layout.addComponent(gridLayout);
    return layout;
  }

  private Layout getSendDocumentLayout() {
    final Label chosenFile = new Label();
    final TextField consignmentId = new TextField();
    consignmentId.setEnabled(false);
    consignmentId.setCaption("Saadetise id");
    CheckBox consignmentCheck = new CheckBox("Genereeri saadetise ID automaatselt");
    consignmentCheck.setValue(true);
    consignmentCheck.addValueChangeListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = -6857112166321059475L;

      public void valueChange(ValueChangeEvent event) {
        boolean value = (Boolean) event.getProperty().getValue();
        if (value) {
          consignmentId.setEnabled(false);
          consignmentId.setRequired(false);
          consignmentId.setValue("");
        } else {
          consignmentId.setEnabled(true);
          consignmentId.setRequired(true);
        }
      }
    });
    final ComboBox capsules = getSelect(config.getCapsuleSelect(), "vali dokument");
    capsules.setWidth(400, Unit.PIXELS);
    capsules.setRequired(true);
    final ComboBox adressees = getSelect(config.getCapsuleAddressateSelect(), "Vali adressaat");
    adressees.setWidth(400, Unit.PIXELS);
    adressees.setRequired(true);
    Button buttonSubmit = new Button("Saada document");
    buttonSubmit.addClickListener(
    /* buttonSubmit.addListener( */new Button.ClickListener() {
      private static final long serialVersionUID = -6857112166321059475L;

      public void buttonClick(ClickEvent event) {
        if (adressees.getValue() == null || capsules.getValue() == null) {
          Notification notification =
              new Notification("Täitke kõik kohustuslikud väljad! ",
                  Notification.Type.WARNING_MESSAGE);
          notification.setDelayMsec(-1);
          notification.show(Page.getCurrent());
        } else {
          log.info("got request. addressees: " + adressees.getValue() + " capsule: "
              + capsules.getValue());
          try {
            // File attachment = FileUtil.createFileAndWrite(uploadField.getContentAsStream());
            List<SendDocumentResponse> responses =
                documentClientService.sendDocument(capsules.getValue().toString(), adressees
                    .getValue().toString(), consignmentId.getValue());
            String statuses = "";
            for (SendDocumentResponse response : responses) {
              statuses +=
                  "Dokument saadetud. Status: "
                      + response.getReceiptId()
                      + (response.getFault() == null ? "" : " faultCode: "
                          + response.getFault().getFaultCode() + " faultString: "
                          + response.getFault().getFaultString() + "\n'");
              // showNotification();
            }
            Notification notification =
                new Notification("Dokumendi saatmise staatused: " + statuses,
                    Notification.Type.HUMANIZED_MESSAGE);
            notification.setDelayMsec(-1);
            notification.show(Page.getCurrent());
          } catch (DhxException ex) {
            log.error("Error while sending document." + ex.getMessage(), ex);
            Notification notification =
                new Notification("Viga dokumendi saatmisel! " + ex.getMessage(),
                    Notification.Type.WARNING_MESSAGE);
            notification.setDelayMsec(-1);
            notification.show(Page.getCurrent());
            // showNotification("Viga documendi saatmisel!" + ex.getMessage());
          }
        }
      }
    });
    Label help = new Label();
    help.setCaptionAsHtml(true);
    help.setCaption("<span style=\"white-space:normal;\">" + config.getSendDocumentHelp()
        + "</span>");
    FormLayout formLayout =
        new FormLayout(help, consignmentCheck, consignmentId, capsules, adressees, buttonSubmit,
            chosenFile);
    formLayout.setMargin(false);
    VerticalLayout vertLayout = new VerticalLayout();
    vertLayout.addComponent(help);
    vertLayout.addComponent(formLayout);
    return vertLayout;
  }

  private ComboBox getSelect(List<Map<String, String>> selectString, String caption) {
    ComboBox select = new ComboBox(caption);
    for (Map<String, String> row : selectString) {
      select.addItem(row.get("value"));
      select.setItemCaption(row.get("value"), row.get("name"));
    }
    return select;
  }

  private void sendDocumentOnClick() {

  }

  private Layout getRepresentationListLayout() {
    /*
     * final TextField regClass = new TextField(); regClass.setCaption("Asutuse klass"); final
     * TextField regCode = new TextField(); regCode.setCaption("Asutuse kood");
     */
    final ComboBox adressees = getSelect(config.getCapsuleAddressateSelect(), "Vali adressaat");
    adressees.setWidth(400, Unit.PIXELS);
    adressees.setRequired(true);
    Button button = new Button("Saada päring");
    button.addClickListener(
    /* button.addListener( */new Button.ClickListener() {
      private static final long serialVersionUID = -6857112166321059475L;

      public void buttonClick(ClickEvent event) {
        if (adressees.getValue() != null) {
          try {
            log.debug("getting representation List");
            /*
             * XroadMember member = new XroadMember(soapConfig.getXroadInstance(),
             * regClass.getValue(), regCode .getValue(), soapConfig.getSubsystem(), null);
             */
            XroadMember member =
                addressService.getClientForMemberCode(adressees.getValue().toString());
            if (member.getRepresentee() == null) {
              RepresentationListResponse response = dhxGateway.getRepresentationList(member);
              String reprStr = "";
              if (response.getMembers() != null && response.getMembers().getMember() != null
                  && response.getMembers().getMember().size() > 0) {
                for (Member repr : response.getMembers().getMember()) {
                  SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                  String startDateStr = "";
                  String endDateStr = "";
                  Representee representee = new Representee(repr);
                  if (representee.getStartDate() != null) {
                    startDateStr = sdf.format(representee.getStartDate());
                  }
                  if (representee.getEndDate() != null) {
                    endDateStr = sdf.format(representee.getEndDate());
                  }
                  reprStr =
                      reprStr
                          + (reprStr.equals("") ? "" : ", ")
                          + (repr.getMemberCode() + " algus: " + startDateStr 
                              + " lõpp:" + endDateStr);
                }
              }
              Notification notification =
                  new Notification("Representatives:" + reprStr,
                      Notification.Type.HUMANIZED_MESSAGE);
              notification.setDelayMsec(-1);
              notification.show(Page.getCurrent());
            } else {
              Notification notification =
                  new Notification("Valitud adressaat on vahendatav. "
                      + "Ei ole võimalik leida tema poolt vahendatavate nimekirja.",
                      Notification.Type.WARNING_MESSAGE);
              notification.setDelayMsec(-1);
              notification.show(Page.getCurrent());
            }
          } catch (DhxException ex) {
            log.error("Error while sending document." + ex.getMessage(), ex);
            Notification notification =
                new Notification("Viga documendi saatmisel!" + ex.getMessage(),
                    Notification.Type.WARNING_MESSAGE);
            notification.setDelayMsec(-1);
            notification.show(Page.getCurrent());
          }
        } else {
          Notification notification =
              new Notification("Täitke kõik kohustuslikud väljad!",
                  Notification.Type.WARNING_MESSAGE);
          notification.setDelayMsec(-1);
          notification.show(Page.getCurrent());
        }
      }
    });
    // Label label = new Label("Vahendatavate nimekiri");
    // label.setStyleName("h3");
    FormLayout formLayout = new FormLayout(adressees, button);
    formLayout.setMargin(false);
    Label help = new Label();
    help.setCaptionAsHtml(true);
    help.setCaption("<span style=\"white-space:normal;\">" + config.getRepresentationListHelp()
        + "</span>");
    VerticalLayout vertLayout = new VerticalLayout();
    vertLayout.addComponent(help);
    vertLayout.addComponent(formLayout);
    return vertLayout;
  }


  private Layout getAdresseeList() {
    VerticalLayout adresseeLayout = new VerticalLayout();
    // Label label = new Label("Lokaalne aadressiraamat: \n");
    // label.setStyleName("h3");
    // adresseeLayout.addComponent(label);
    List<XroadMember> members = addressService.getAdresseeList();
    final Label adrLabel = new Label("");
    adrLabel.setCaptionAsHtml(true);
    adrLabel.setCaption(getAdresseeString(members));
    adresseeLayout.addComponent(adrLabel);
    Button button = new Button("Uuenda");
    button.addClickListener(
    /* button.addListener( */new Button.ClickListener() {
      private static final long serialVersionUID = -6857112166321059475L;

      public void buttonClick(ClickEvent event) {

        log.debug("renewing address list");
        addressService.renewAddressList();
        List<XroadMember> members = addressService.getAdresseeList();
        adrLabel.setCaption("<span style=\"white-space:normal;\">" + getAdresseeString(members)
            + "</span>");
        Notification notification =
            new Notification("Lokaalne aadressiraamat uuendatud",
                Notification.Type.HUMANIZED_MESSAGE);
        notification.setDelayMsec(-1);
        notification.show(Page.getCurrent());

      }
    });
    adresseeLayout.addComponent(button);
    return adresseeLayout;
  }

  private String getAdresseeString(List<XroadMember> members) {
    String labelAdr = "";
    for (XroadMember member : members) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      String startDateStr = "";
      String endDateStr = "";
      if (member.getRepresentee() != null) {
        if (member.getRepresentee().getStartDate() != null) {
          startDateStr = sdf.format(member.getRepresentee().getStartDate());
        }
        if (member.getRepresentee().getEndDate() != null) {
          endDateStr = sdf.format(member.getRepresentee().getEndDate());
        }
      }
      labelAdr +=
          "</br>"
              + member.toString().replace("addressee", "adressaat")
                  .replace("X-road member", "X-tee liige")
                  .replace("is representee", "kas vahendatav")
              + (member.getRepresentee() == null ? "" : " vahendamise algus: " + startDateStr
                  + " lõpp:" + endDateStr) + "\n";
    }
    return labelAdr;
  }


}
