package cas.ypsiliform.messages.decoder;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.AgentRegistration;

public class AgentRegistrationDecoder implements Decoder.Text<AgentRegistration> {

	@Override
	public AgentRegistration decode(String s) throws DecodeException {
		AgentRegistration reg = new AgentRegistration();
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject readObject = reader.readObject();
		int id = readObject.getInt("id");
		String config = readObject.getString("config");
		reg.setId(id);
		reg.setConfig(config);
		return reg;
	}

	@Override
	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", "");
		return "agentregistration".equals(string);
	}

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

}
