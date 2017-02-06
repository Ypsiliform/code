package cas.ypsiliform.messages.encoder;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.SendProposal;

public class SendProposalEncoder implements Encoder.Text<SendProposal> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public String encode(SendProposal object) throws EncodeException {
		try {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("type", "sendproposal");
			builder.add("new_proposal", getJsonArray(object.getNewProposal()));
			builder.add("reference_proposal", getJsonArray(object.getReferenceProposal()));
			builder.add("total_rounds", object.getTotalRounds());
			builder.add("remaining_rounds", object.getRemainingRounds());
			return builder.build().toString();
		} catch (Exception e) {
			throw new EncodeException(object, "Failed encode object", e);
		}
	}

	private JsonArrayBuilder getJsonArray(int[] intArray) {
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (int value : intArray) {
			array.add(value);
		}
		return array;
	}

}
