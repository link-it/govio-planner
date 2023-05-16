package it.govhub.govio.planner.batch.exception;

public class ShouldRetryException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ShouldRetryException(Throwable e) {
		super(e);
	}

}
