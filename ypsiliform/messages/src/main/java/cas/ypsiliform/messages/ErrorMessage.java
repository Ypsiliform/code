package cas.ypsiliform.messages;

public class ErrorMessage extends AbstractMessage {

	private int errorCode;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ErrorMessage [errorCode=");
		builder.append(errorCode);
		builder.append("]");
		return builder.toString();
	}

}
