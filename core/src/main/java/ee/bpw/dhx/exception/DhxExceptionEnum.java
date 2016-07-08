package ee.bpw.dhx.exception;

/**
 * Enumeration which contains codes of the errors that might occur in DHX application.
 * @author Aleksei Kokarev
 *
 */
public enum DhxExceptionEnum {

  CAPSULE_VALIDATION_ERROR("DHX.Validation", true), 
  DUPLICATE_PACKAGE("DHX.Duplicate", true), 
  WRONG_RECIPIENT("DHX.InvalidAddressee", true), 
  OVER_MAX_SIZE("DHX.SizeLimitExceeded", true), 
  FILE_ERROR("FILE_ERROR", false), 
  WS_ERROR("WS_ERROR", false), 
  EXTRACTION_ERROR("EXCTRACTION_ERROR", false), 
  TECHNICAL_ERROR("TECHNICAL_ERROR", false),
  DATA_ERROR("DATA_ERROR", false);

  // error code which is returned is services
  private String codeForService;
  private Boolean businessException;


  private DhxExceptionEnum(String codeForService, Boolean businessException) {
    this.codeForService = codeForService;
    this.businessException = businessException;
  }

  public String getCodeForService() {
    return codeForService;
  }

  public Boolean isBusinessException() {
    return businessException;
  }
}
