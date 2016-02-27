package br.com.smartpush.e;

public final class SmartpushConfigurationsException extends RuntimeException {

	private static final long serialVersionUID = -8039400262284675828L;

	public SmartpushConfigurationsException() {
		super();
	}

	public SmartpushConfigurationsException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public SmartpushConfigurationsException(String detailMessage) {
		super(detailMessage);
	}

	public SmartpushConfigurationsException(Throwable throwable) {
		super(throwable);
	}
	
}