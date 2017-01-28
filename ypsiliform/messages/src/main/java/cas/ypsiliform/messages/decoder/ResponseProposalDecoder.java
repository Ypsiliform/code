package cas.ypsiliform.messages.decoder;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.ResponseProposal;

public class ResponseProposalDecoder implements Decoder.Text<ResponseProposal> {

	public void init(EndpointConfig config) {

	}

	public void destroy() {

	}

	public ResponseProposal decode(String s) throws DecodeException {
		ResponseProposal msg = new ResponseProposal();
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject readObject = reader.readObject();
		boolean accept = readObject.getBoolean("accept");
		msg.setAccept(accept);
		return msg;
	}

	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", "");
		return "responseproposal".equals(string);
	}

}
