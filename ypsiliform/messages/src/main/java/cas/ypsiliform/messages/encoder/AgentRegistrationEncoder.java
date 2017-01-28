package cas.ypsiliform.messages.encoder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.AgentRegistration;

public class AgentRegistrationEncoder implements Encoder.Text<AgentRegistration> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public String encode(AgentRegistration object) throws EncodeException {
		try {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("type", "agentregistration");
			builder.add("id", object.getId());
			builder.add("config", object.getConfig());
			return builder.build().toString();
		} catch (Exception e) {
			throw new EncodeException(object, "Failed encode object", e);
		}
	}

}
