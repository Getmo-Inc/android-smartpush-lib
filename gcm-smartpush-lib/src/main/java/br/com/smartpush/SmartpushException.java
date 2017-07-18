package br.com.smartpush;

final class SmartpushException extends RuntimeException {

	private static final long serialVersionUID = -8039400262284675828L;

	public SmartpushException() {
		super();
	}

	public SmartpushException(String detailMessage, Throwable throwable ) {
		super( detailMessage, throwable );
	}

	public SmartpushException(String detailMessage ) {
		super( detailMessage );
	}

	public SmartpushException(Throwable throwable ) {
		super( throwable );
	}
}