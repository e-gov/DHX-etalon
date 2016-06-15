package ee.bpw.dhx.util;

public enum StatusEnum {
	
	
	ACCEPTED("ACCEPTED"), REJECTED("REJECTED");
	
	private String name;
	
	private StatusEnum (String name) {
		this.name = name;
	}

	public String getName () {
		return name;
	}
	
	@Override
	public String toString () {
		return name;
	}
}
