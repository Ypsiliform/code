package cas.ypsiliform.messages;

public class StartNegotiation extends AbstractMessage {

	private int agent;
	private int period;

	public int getAgent() {
		return agent;
	}

	public void setAgent(int agent) {
		this.agent = agent;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StartNegotiation [agent=");
		builder.append(agent);
		builder.append(", period=");
		builder.append(period);
		builder.append("]");
		return builder.toString();
	}

}
