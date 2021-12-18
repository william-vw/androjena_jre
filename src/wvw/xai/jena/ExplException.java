package wvw.xai.jena;

public class ExplException extends Exception {

	private static final long serialVersionUID = 1L;

	public ExplException() {
		super();
	}

	public ExplException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExplException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExplException(String message) {
		super(message);
	}

	public ExplException(Throwable cause) {
		super(cause);
	}

}
