package it.govhub.govio.planner.batch.exception;

public class BackendGovioRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public BackendGovioRuntimeException(Throwable e) {
		super(e);
	}

}
