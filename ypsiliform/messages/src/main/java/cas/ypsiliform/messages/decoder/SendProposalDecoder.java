package cas.ypsiliform.messages.decoder;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.SendProposal;

public class SendProposalDecoder implements Decoder.Text<SendProposal> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public SendProposal decode(String s) throws DecodeException {
		SendProposal msg = new SendProposal();
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject readObject = reader.readObject();
		int[] newProposal = getArray(readObject.getJsonArray("new_proposal"));
		int[] referenceProposal = getArray(readObject.getJsonArray("reference_proposal"));
		int totalRounds = readObject.getInt("total_rounds");
		int remainingRounds = readObject.getInt("remaining_rounds");
		msg.setNewProposal(newProposal);
		msg.setReferenceProposal(referenceProposal);
		msg.setTotalRounds(totalRounds);
		msg.setRemainingRounds(remainingRounds);
		return msg;
	}

	private int[] getArray(JsonArray jsonArray) {
		int[] array = new int[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			array[i] = jsonArray.getInt(i);
		}
		return array;
	}

	@Override
	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", "");
		return "sendproposal".equals(string);
	}

}
