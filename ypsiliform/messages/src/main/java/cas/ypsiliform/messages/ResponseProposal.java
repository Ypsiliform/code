package cas.ypsiliform.messages;

public class ResponseProposal extends AbstractMessage {

	private boolean accept;

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResponseProposal [accept=");
		builder.append(accept);
		builder.append("]");
		return builder.toString();
	}

}
