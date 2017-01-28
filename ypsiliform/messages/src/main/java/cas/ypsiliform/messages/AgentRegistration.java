package cas.ypsiliform.messages;

public class AgentRegistration extends AbstractMessage {

	private int id;
	private String config;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentRegistration [id=");
		builder.append(id);
		builder.append(", config=");
		builder.append(config);
		builder.append("]");
		return builder.toString();
	}

}
