package it.govhub.govio.planner.batch.exception;

public class ShouldSkipException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public ShouldSkipException(Throwable e) {
		super(e);
	}

}
