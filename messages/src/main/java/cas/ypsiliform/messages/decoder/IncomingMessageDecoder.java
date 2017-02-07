package cas.ypsiliform.messages.decoder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.AbstractMessage;

public class IncomingMessageDecoder implements Decoder.Text<AbstractMessage> {

	private List<Decoder.Text<?>> decoders = new ArrayList<>();

	@Override
	public void init(EndpointConfig config) {
		decoders.add(new AgentRegistrationDecoder());
		decoders.add(new ErrorMessageDecoder());
		decoders.add(new AgentResponseDecoder());
		decoders.add(new StartNegotiationDecoder());
		decoders.add(new EndNegotiationDecoder());
		decoders.add(new MediatorRequestDecoder());
	}

	@Override
	public void destroy() {

	}

	@Override
	public AbstractMessage decode(String s) throws DecodeException {
		try {
			for (Decoder.Text<?> decoder : decoders) {
				if (decoder.willDecode(s)) {
					return (AbstractMessage) decoder.decode(s);
				}
			}
			throw new DecodeException(s, "Failed decoding message! unknown message!");
		} catch (Exception e) {
			throw new DecodeException(s, "Failed decoding message!", e);
		}
	}

	@Override
	public boolean willDecode(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		String string = reader.readObject().getString("type", null);
		return string != null;
	}

}
