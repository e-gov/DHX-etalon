package ee.bpw.dhx.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhxException extends Exception {
	
	private DHXExceptionEnum exceptionCode;
	
	public DhxException () {
		super();
	}
	
	public DhxException (String message) {
		super(message);
	}
	
	public DhxException (DHXExceptionEnum exceptionCode, String message) {
		super(message);
		this.setExceptionCode(exceptionCode);
	}
	
/*	public DhxException (String message, Exception cause) {
		super(message, cause);
	}*/
	
	public DhxException (DHXExceptionEnum exceptionCode, String message, Exception cause) {
		super(message, cause);
		this.setExceptionCode(exceptionCode);
	}
	
	@Override
	public String getMessage(){
		String message = super.getMessage();
		return "DHXException code: " + exceptionCode.getCodeForService() + " " + message;
	}

}
