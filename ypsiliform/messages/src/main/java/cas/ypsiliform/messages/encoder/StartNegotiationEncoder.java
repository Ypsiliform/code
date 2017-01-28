package cas.ypsiliform.messages.encoder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.StartNegotiation;

public class StartNegotiationEncoder implements Encoder.Text<StartNegotiation> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public String encode(StartNegotiation object) throws EncodeException {
		try {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("type", "startnegotiation");
			builder.add("agent", object.getAgent());
			builder.add("period", object.getPeriod());
			return builder.build().toString();
		} catch (Exception e) {
			throw new EncodeException(object, "Failed encode object", e);
		}
	}

}
