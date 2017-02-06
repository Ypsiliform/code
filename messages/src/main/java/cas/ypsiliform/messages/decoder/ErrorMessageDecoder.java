package cas.ypsiliform.messages.decoder;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.ErrorMessage;

public class ErrorMessageDecoder implements Decoder.Text<ErrorMessage> {

	public void init(EndpointConfig config) {

	}

	public void destroy() {

	}

	public ErrorMessage decode(String s) throws DecodeException {
		ErrorMessage msg = new ErrorMessage();
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject readObject = reader.readObject();
		int errorCode = readObject.getInt("error");
		msg.setErrorCode(errorCode);
		return msg;
	}

	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", "");
		return "error".equals(string);
	}

}
