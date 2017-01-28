package cas.ypsiliform.messages.decoder;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.StartNegotiation;

public class StartNegotiationDecoder implements Decoder.Text<StartNegotiation> {

	public void init(EndpointConfig config) {

	}

	public void destroy() {

	}

	public StartNegotiation decode(String s) throws DecodeException {
		StartNegotiation msg = new StartNegotiation();
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject readObject = reader.readObject();
		int agent = readObject.getInt("agent");
		int period = readObject.getInt("period");
		msg.setAgent(agent);
		msg.setPeriod(period);
		return msg;
	}

	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", "");
		return "startnegotiation".equals(string);
	}

}
