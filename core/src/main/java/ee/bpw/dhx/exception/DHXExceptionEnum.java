package ee.bpw.dhx.exception;

public enum DHXExceptionEnum {
	
	CAPSULE_VALIDATION_ERROR("Client.Validation"), DUPLICATE_PACKAGE("Client.Duplicate"),WRONG_RECIPIENT("Client.InvalidAddressee"), OVER_MAX_SIZE("Server.SizeLimitExceeded"),
	FILE_ERROR("FILE_ERROR"), WS_ERROR("WS_ERROR"), EXCTRACTION_ERROR("EXCTRACTION_ERROR"), TECHNICAL_ERROR("Client.TechnicalError");
	//error code which is returned is services
	private String codeForService;
	

	private DHXExceptionEnum (String codeForService) {
		this.codeForService = codeForService;
	}
	
	public String getCodeForService() {
		return codeForService;
	}

}
