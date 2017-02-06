package cas.ypsiliform.messages.decoder;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.EndNegotiation;

public class EndNegotiationDecoder implements Decoder.Text<EndNegotiation> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public EndNegotiation decode(String s) throws DecodeException {
		EndNegotiation msg = new EndNegotiation();
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject readObject = reader.readObject();
		JsonArray array = readObject.getJsonArray("solution");
		int[] solution = new int[array.size()];
		for (int i = 0; i < array.size(); i++) {
			solution[i] = array.getInt(i);
		}
		msg.setSolution(solution);
		return msg;
	}

	@Override
	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", "");
		return "endnegotiation".equals(string);
	}

}
