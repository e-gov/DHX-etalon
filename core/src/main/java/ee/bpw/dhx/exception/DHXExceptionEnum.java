package ee.bpw.dhx.exception;

public enum DHXExceptionEnum {
	
	EXCTRACTION_ERROR("EXCTRACTION_ERROR"), CAPSULE_VALIDATION_ERROR("CAPSULE_VALIDATION_ERROR"),
	FILE_ERROR("FILE_ERROR"), WRONG_RECIPIENT("WRONG_RECIPIENT"), WS_ERROR("WS_ERROR");
	//error code which is returned is services
	private String codeForService;
	

	private DHXExceptionEnum (String codeForService) {
		this.codeForService = codeForService;
	}
	
	public String getCodeForService() {
		return codeForService;
	}

}
