package cas.ypsiliform.messages.encoder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.ErrorMessage;

public class ErrorMessageEncoder implements Encoder.Text<ErrorMessage> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public String encode(ErrorMessage object) throws EncodeException {
		try {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("type", "error");
			builder.add("error", object.getErrorCode());
			return builder.build().toString();
		} catch (Exception e) {
			throw new EncodeException(object, "Failed encode object", e);
		}
	}

}
