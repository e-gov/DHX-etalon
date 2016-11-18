package ee.ria.dhx.client.ui;


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
import com.vaadin.shared.ui.combobox.FilteringMode;
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

import ee.ria.dhx.client.CustomAppender;
import ee.ria.dhx.client.config.DhxClientConfig;
import ee.ria.dhx.client.service.AddressClientServiceImpl;
import ee.ria.dhx.client.service.DhxClientPackageServiceImpl;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.RepresentationListResponse;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Representee;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.impl.DhxGateway;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main user interface of the DHX client application. Contains all needed to monitor and test DHX
 * core and ws applications
 * 
 * @author Aleksei Kokarev
 *
 */
@SpringUI
@Theme("valo")
@Slf4j
public class DhxUi extends UI {
  private static final long serialVersionUID = -6857112166321059475L;

  @Autowired
  DhxClientPackageServiceImpl documentClientService;


  @Autowired
  DhxClientConfig config;

  @Autowired
  DhxConfig dhxConfig;

  @Autowired
  SoapConfig soapConfig;

  @Autowired
  AddressService addressService;

  @Autowired
  MessageSource messageSource;

  @Autowired
  DhxGateway dhxGateway;


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

  private String getMessage(String code) {
    return messageSource.getMessage(code, null, getLocale());
  }

  private String getMessageForDhxException(DhxException exception) {
    DhxExceptionEnum exceptionEnum = exception.getExceptionCode();
    String messageCode;
    switch (exceptionEnum) {
      case CAPSULE_VALIDATION_ERROR:
        messageCode = "error.dhx.capsule-validation";
        break;
      case DUPLICATE_PACKAGE:
        messageCode = "error.dhx.duplicate-package";
        break;
      case EXTRACTION_ERROR:
        messageCode = "error.dhx.extraction-error";
        break;
      case FILE_ERROR:
        messageCode = "error.dhx.file-error";
        break;
      case OVER_MAX_SIZE:
        messageCode = "error.dhx.over-max-size";
        break;

      case TECHNICAL_ERROR:
        messageCode = "error.dhx.technical-error";
        break;

      case WRONG_RECIPIENT:
        messageCode = "error.dhx.wrong-recipient";
        break;

      case WS_ERROR:
        messageCode = "error.dhx.ws-error";
        break;

      default:
        messageCode = null;
    }
    return (messageCode == null ? "" : getMessage(messageCode)) + " <br/>"
        + exception.getMessage() + "";
  }

  @Override
  protected void init(VaadinRequest request) {
    Page.getCurrent().setTitle(getMessage("main.app.label"));
    setPollInterval(config.getLogRefresh());
    getTooltipConfiguration().setOpenDelay(5);
    Label mainLabel = new Label(getMessage("main.app.label"));
    mainLabel.setStyleName("h1");
    StreamResource source =
        new StreamResource(new DhxStreamSource(
            "jar://images/EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg"), "help.png");
    Image image = new Image("", source);
    image.setStyleName("v-table-footer-container");
    GridLayout headerLayout = new GridLayout(2, 2);
    headerLayout.setWidth(100, Unit.PERCENTAGE);
    headerLayout.addComponent(mainLabel);
    headerLayout.addComponent(image, 1, 0, 1, 1);
    headerLayout.addComponent(getInfo());
    headerLayout.setColumnExpandRatio(0, 0.85f);
    headerLayout.setColumnExpandRatio(1, 0.15f);
    GridLayout formKonfLayout = new GridLayout(2, 1);
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
    Label settingsLabel = new Label(getMessage("settings.label"));
    settingsLabel.setStyleName("h2");
    TabSheet settings = new TabSheet();
    settings.addTab(getConf(), getMessage("settings.app-settings"));
    settings.addTab(getAdresseeList(), getMessage("settings.local-addresse-list"));
    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(settingsLabel);
    layout.addComponent(settings);
    return layout;
  }

  private Layout getActivityAsTab() {
    Label activityLabel = new Label(getMessage("activity.label"));
    activityLabel.setStyleName("h2");
    TabSheet activity = new TabSheet();
    activity.addTab(getSendDocumentLayout(), getMessage("activity.send-document"));
    activity.addTab(getRepresentationListLayout(), getMessage("activity.representation-list"));
    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(activityLabel);
    layout.addComponent(activity);
    return layout;
  }

  private Layout getInfo() {
    VerticalLayout layout = new VerticalLayout();
    Label mainLabel2 = new Label();
    mainLabel2.setCaptionAsHtml(true);
    mainLabel2.setCaption("<span style=\"white-space:normal;\">" + getMessage("main.description")
        + getMessage("main.info")
        + "</span>");
    layout.addComponent(mainLabel2);
    Label info = new Label(config.getName() + " "
        + getMessage("main.document-administration-system"));
    info.setWidth(100, Unit.PERCENTAGE);
    info.setCaptionAsHtml(true);
    info.setStyleName("h2");
    layout.addComponent(info);
    StreamResource source =
        new StreamResource(new DhxStreamSource(config.getCapsuleCorrect()),
            "DVKkapsel_korrektne.xml");
    FileDownloader fileDownloader = new FileDownloader(source);
    Link link = new Link(getMessage("main.download-correct"), null);
    fileDownloader.extend(link);
    layout.addComponent(link);

    StreamResource source2 =
        new StreamResource(new DhxStreamSource(config.getCapsuleInvalid()), "DVKkapsel_vale.xml");
    FileDownloader fileDownloader2 = new FileDownloader(source2);
    Link link2 = new Link(getMessage("main.download-invalid"), null);
    fileDownloader2.extend(link2);
    layout.addComponent(link2);
    ExternalResource wsdlResource = new ExternalResource("ws/dhx.wsdl");
    Link linkwsdl = new Link(getMessage("main.wsdl"), wsdlResource);
    linkwsdl.setTargetName("_blank");
    layout.addComponent(linkwsdl);
    return layout;
  }

  private Component addTooltip(Label component, String header, String description) {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setMargin(false);
    layout.setSpacing(false);
    component.setDescription("<div>" + description + "</div");
    return component;
  }

  private Layout getLog() {
    try {
      final TextArea text = new TextArea();
      text.setSizeFull();
      text.setValue(CustomAppender.getLastEvents());
      text.setEnabled(false);
      text.setHeight(500, Unit.PIXELS);
      Button buttonClear = new Button(getMessage("log.remove-log"));
      buttonClear.addClickListener(new ClickListener() {
        private static final long serialVersionUID = -6857112166321059475L;

        @Override
        public void buttonClick(ClickEvent event) {
          CustomAppender.deleteLastEvents();
          text.setValue("");
        }
      });
      addPollListener(new UIEvents.PollListener() {
        private static final long serialVersionUID = -6857112166321059475L;

        @Override
        public void poll(UIEvents.PollEvent event) {
          text.setValue(CustomAppender.getLastEvents());
        }
      });
      Label label = new Label(getMessage("log.last-log"));
      label.setStyleName("h2");
      VerticalLayout layout = new VerticalLayout();
      layout.addComponent(label);
      layout.addComponent(buttonClear);
      layout.addComponent(text);
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
    gridLayout.addComponent(addTooltip(new Label(getMessage("settings.log-events-count") + ": "),
        getMessage("settings.log-events-count"),
        getMessage("settings.log-events-help")));
    gridLayout.addComponent(new Label(config.getLogMaxSize().toString()));
    gridLayout.addComponent(addTooltip(new Label(getMessage("settings.log-refresh") + ": "),
        getMessage("settings.log-refresh"), getMessage("settings.log-refresh-help")));
    gridLayout.addComponent(new Label(config.getLogRefresh().toString()));
    gridLayout.addComponent(addTooltip(new Label(getMessage("settings.representation-list")
        + ": "),
        getMessage("settings.representation-list"), getMessage("settings.representatives-help")));
    gridLayout.addComponent(new Label(config.getRepresentees()));


    gridLayout.addComponent(addTooltip(new Label(getMessage("settings.validate-capsule") + ": "),
        getMessage("settings.validate-capsule"), getMessage("settings.validate-capsule-help")));
    gridLayout.addComponent(new Label(dhxConfig.getCapsuleValidate().toString()));


    gridLayout.addComponent(addTooltip(new Label(getMessage("settings.securityServer") + ": "),
        getMessage("settings.securityServer"),
        getMessage("settings.security-server-help")));
    gridLayout.addComponent(new Label(soapConfig.getSecurityServer()));
    gridLayout.addComponent(addTooltip(new Label(getMessage("settings.xroad-member") + ": "),
        getMessage("settings.xroad-member"),
        getMessage("settings.xroad-member-help")));
    gridLayout.addComponent(new Label(soapConfig.getXroadInstance() + "/"
        + soapConfig.getMemberClass() + "/" + soapConfig.getMemberCode() + "/"
        + soapConfig.getDefaultSubsystem()));
    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(gridLayout);
    return layout;
  }

  private Layout getSendDocumentLayout() {
    final Label chosenFile = new Label();
    final TextField consignmentId = new TextField();
    consignmentId.setEnabled(false);
    consignmentId.setCaption(getMessage("activity.send-document.consignmentid"));
    CheckBox consignmentCheck =
        new CheckBox(getMessage("activity.send-document.generate-consignmentid"));
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
    final ComboBox capsules =
        getSelect(config.getCapsuleSelect(), getMessage("activity.send-document.choose-document"));
    capsules.setWidth(400, Unit.PIXELS);
    capsules.setRequired(true);
    List<Map<String, String>> adresseesSelect = null;;
    try {
      adresseesSelect = ((AddressClientServiceImpl) addressService).getAdresseesAsSelect();
    } catch (DhxException ex) {
      log.error("Error while sending document." + ex.getMessage(), ex);
      showDhxNotification(getMessage("activity.send-document.error")
          + getMessageForDhxException(ex), Notification.Type.WARNING_MESSAGE);
    }
    final ComboBox adressees =
        getSelect(/* config.getCapsuleAddressateSelect() */adresseesSelect,
            getMessage("activity.send-document.choose-adressee"));
    adressees.setWidth(400, Unit.PIXELS);
    adressees.setRequired(true);
    Button buttonSubmit = new Button(getMessage("activity.send-document.send-dcoument-button"));
    buttonSubmit.addClickListener(
        new Button.ClickListener() {
          private static final long serialVersionUID = -6857112166321059475L;

          public void buttonClick(ClickEvent event) {
            if (adressees.getValue() == null || capsules.getValue() == null) {
              showDhxNotification(getMessage("error.fill-required-fields"),
                  Notification.Type.WARNING_MESSAGE);
            } else {
              log.info("got request. addressees: {} capsule: {}", adressees.getValue(),
                  capsules.getValue());
              try {
                String consignmentIdStr = consignmentId.getValue();
                if (StringUtil.isNullOrEmpty(consignmentIdStr)) {
                  consignmentIdStr = UUID.randomUUID().toString();
                }
                List<DhxSendDocumentResult> responses =
                    documentClientService.sendDocument(capsules.getValue().toString(), adressees
                        .getValue().toString(), consignmentIdStr);
                Boolean success = isSendDocumentSuccess(responses);
                String statuses = getSendDocumentStatuses(responses, success);
                showDhxNotification("<span style=\"white-space:normal;\">"
                    + getMessage("activity.send-document.results") + ":<br/> "
                    + statuses + "</span>", (success
                    ? Notification.Type.TRAY_NOTIFICATION
                    : Notification.Type.WARNING_MESSAGE));
              } catch (DhxException ex) {
                log.error("Error while sending document." + ex.getMessage(), ex);
                showDhxNotification(getMessage("activity.send-document.error")
                    + getMessageForDhxException(ex), Notification.Type.WARNING_MESSAGE);
              }
            }
          }
        });
    Label help = new Label();
    help.setCaptionAsHtml(true);
    help.setCaption("<span style=\"white-space:normal;\">"
        + getMessage("activity.send-document.help")
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

  private Boolean isSendDocumentSuccess(List<DhxSendDocumentResult> responses) {
    Boolean success = true;
    for (DhxSendDocumentResult response : responses) {
      if (response.getResponse().getFault() != null) {
        success = false;
      } else {
        success = true;
      }
    }
    return success;
  }

  private String getSendDocumentStatuses(List<DhxSendDocumentResult> responses, Boolean success) {
    String statuses = "";
    for (DhxSendDocumentResult response : responses) {
      statuses +=
          "&nbsp;&nbsp;"
              + (success
                  ? getMessage("activity.send-document.document-sent")
                  : getMessage("activity.send-document.error"))
              + " "
              + getMessage("activity.send-document.receiptid")
              + ": "
              + response.getResponse().getReceiptId()
              + "<br/>"
              + (response.getResponse().getFault() == null
                  ? ""
                  : "&nbsp;&nbsp;&nbsp;&nbsp;faultCode: "
                      + response.getResponse().getFault().getFaultCode()
                      + "<br/>&nbsp;&nbsp;&nbsp;&nbsp;faultString: "
                      + response.getResponse().getFault().getFaultString() + "\n'");
    }
    return statuses;
  }

  private void showDhxNotification(String notificationText, Notification.Type type) {
    Notification notification =
        new Notification(notificationText, Notification.Type.TRAY_NOTIFICATION);
    if (type.equals(Notification.Type.WARNING_MESSAGE)) {
      notification.setStyleName("error v-Notification-error");
    } else {
      notification.setStyleName("warning v-Notification-warning");
    }
    notification.show(Page.getCurrent());
    notification.setHtmlContentAllowed(true);
  }

  private ComboBox getSelect(List<Map<String, String>> selectString, String caption) {
    ComboBox select = new ComboBox(caption);
    select.setFilteringMode(FilteringMode.CONTAINS);
    select.setPageLength(0);
    for (Map<String, String> row : selectString) {
      select.addItem(row.get("value"));
      select.setItemCaption(row.get("value"), row.get("name"));
    }
    return select;
  }

  private Layout getRepresentationListLayout() {
    final ComboBox adressees =
        getSelect(config.getCapsuleAddressateSelectRepresentation(),
            getMessage("activity.representation-list.choose-adressee"));
    adressees.setWidth(400, Unit.PIXELS);
    adressees.setRequired(true);
    Button button = new Button(getMessage("activity.representation-list.send-request"));
    button.addClickListener(
        /* button.addListener( */new Button.ClickListener() {
          private static final long serialVersionUID = -6857112166321059475L;

          public void buttonClick(ClickEvent event) {
            if (adressees.getValue() != null) {
              try {
                log.debug("getting representation List");
                InternalXroadMember member =
                    addressService.getClientForMemberCode(adressees.getValue().toString(), null);
                if (member.getRepresentee() == null) {
                  RepresentationListResponse response = dhxGateway.getRepresentationList(member);
                  String reprStr = "";
                  if (response.getRepresentees() != null
                      && response.getRepresentees().getRepresentee() != null
                      && response.getRepresentees().getRepresentee().size() > 0) {
                    for (Representee repr : response.getRepresentees().getRepresentee()) {
                      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                      String startDateStr = "";
                      String endDateStr = "";
                      DhxRepresentee representee = new DhxRepresentee(repr);
                      if (representee.getStartDate() != null) {
                        startDateStr = sdf.format(representee.getStartDate());
                      }
                      if (representee.getEndDate() != null) {
                        endDateStr = sdf.format(representee.getEndDate());
                      }
                      reprStr =
                          reprStr + "<br/>"
                              + (repr.getMemberCode() + " "
                                  + getMessage("activity.representation-list.start-date") + ": "
                                  + startDateStr
                                  + " " + getMessage("activity.representation-list.end-date")
                                  + ":" + endDateStr);
                    }
                  }
                  showDhxNotification("<span style=\"white-space:normal;\">"
                      + getMessage("activity.representation-list.representees")
                      + ": " + reprStr + "</span>", Notification.Type.HUMANIZED_MESSAGE);
                } else {
                  showDhxNotification(
                      getMessage("activity.representation-list.error.chosen-adressse-is-represented"),
                      Notification.Type.WARNING_MESSAGE);
                }
              } catch (DhxException ex) {
                log.error("Error while sending document." + ex.getMessage(), ex);
                showDhxNotification("<span style=\"white-space:normal;\">"
                    + getMessage("activity.representation-list.error")
                    + getMessageForDhxException(ex) + "</span>",
                    Notification.Type.WARNING_MESSAGE);
              }
            } else {
              showDhxNotification(getMessage("error.fill-required-fields"),
                  Notification.Type.WARNING_MESSAGE);
            }
          }
        });
    FormLayout formLayout = new FormLayout(adressees, button);
    formLayout.setMargin(false);
    Label help = new Label();
    help.setCaptionAsHtml(true);
    help.setCaption("<span style=\"white-space:normal;\">"
        + getMessage("activity.representation-list-help")
        + "</span>");
    VerticalLayout vertLayout = new VerticalLayout();
    vertLayout.addComponent(help);
    vertLayout.addComponent(formLayout);
    return vertLayout;
  }


  private Layout getAdresseeList() {
    VerticalLayout adresseeLayout = new VerticalLayout();
    try {
      List<InternalXroadMember> members = addressService.getAdresseeList();
      final Label adrLabel = new Label("");
      adrLabel.setCaptionAsHtml(true);
      adrLabel.setCaption(getAdresseeString(members));
      adresseeLayout.addComponent(adrLabel);
      Button button = new Button(getMessage("settings.local-addresse-list.refresh"));
      button.addClickListener(
          new Button.ClickListener() {
            private static final long serialVersionUID = -6857112166321059475L;

            public void buttonClick(ClickEvent event) {

              log.debug("renewing address list");
              try {
                addressService.renewAddressList();
                List<InternalXroadMember> members = addressService.getAdresseeList();
                adrLabel.setCaption("<span style=\"white-space:normal;\">"
                    + getAdresseeString(members)
                    + "</span>");
              } catch (DhxException ex) {
                log.error(ex.getMessage(), ex);
              }
              showDhxNotification(getMessage("settings.local-addresse-list.refreshed"),
                  Notification.Type.HUMANIZED_MESSAGE);
            }
          });
      adresseeLayout.addComponent(button);
      return adresseeLayout;
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }

  private String getAdresseeString(List<InternalXroadMember> members) {
    String labelAdr = "";
    if (members != null && members.size() > 0) {
      for (InternalXroadMember member : members) {
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
                + member
                    .toString()
                    .replace("addressee", getMessage("settings.local-addresse-list.adressee"))
                    .replace("X-road member", "X-tee liige")
                    .replace("representee",
                        getMessage("activity.representation-list.is-represented"))
                + (member.getRepresentee() == null ? "" : " "
                    + getMessage("activity.representation-list.start-date") + ": " + startDateStr
                    + " " + getMessage("activity.representation-list.end-date") + ":"
                    + endDateStr) + "\n";
      }
    }
    return labelAdr;
  }


}
