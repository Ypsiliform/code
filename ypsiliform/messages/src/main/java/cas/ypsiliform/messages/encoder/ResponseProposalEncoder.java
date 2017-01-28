package cas.ypsiliform.messages.encoder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.ResponseProposal;

public class ResponseProposalEncoder implements Encoder.Text<ResponseProposal> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public String encode(ResponseProposal object) throws EncodeException {
		try {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("type", "responseproposal");
			builder.add("accept", object.isAccept());
			return builder.build().toString();
		} catch (Exception e) {
			throw new EncodeException(object, "Failed encode object", e);
		}
	}

}
