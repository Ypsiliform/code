package cas.ypsiliform.mediator.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import cas.ypsiliform.messages.AbstractMessage;
import cas.ypsiliform.messages.EndNegotiation;
import cas.ypsiliform.messages.decoder.IncomingMessageDecoder;
import cas.ypsiliform.messages.encoder.EndNegotiationEncoder;
import cas.ypsiliform.messages.encoder.ErrorMessageEncoder;
import cas.ypsiliform.messages.encoder.SendProposalEncoder;

@ServerEndpoint(value = "/mediator", decoders = { IncomingMessageDecoder.class }, encoders = {
		ErrorMessageEncoder.class, SendProposalEncoder.class, EndNegotiationEncoder.class })
public class MediatorWebsocket {

	private static final Logger LOGGER = Logger.getLogger(MediatorWebsocket.class.getName());

	private SessionRepository repo;

	@Inject
	private Event<AbstractMessage> event;

	@PostConstruct
	public void lookup() {
		try {
			InitialContext context = new InitialContext();
			repo = (SessionRepository) context.lookup(SessionRepository.LOOKUP);
		} catch (NamingException e) {
			LOGGER.log(Level.SEVERE, "could not lookup SessionRepositoryBean", e);
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		LOGGER.severe("new session " + session);
		repo.addSession(session);
	}

	@OnMessage
	public void onMessage(Session session, AbstractMessage receivedMsg) {
		LOGGER.severe("new message received : " + receivedMsg + " session = " + session);
		EndNegotiation data = new EndNegotiation();
		data.setSolution(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
		session.getAsyncRemote().sendObject(data);
		event.fire(receivedMsg);
	}

	@OnClose
	public void onClose(Session session) {
		repo.removeSession(session.getId());
	}

	@OnError
	public void onError(Session session, Throwable thrownable) {
		repo.removeSession(session.getId());
	}

}
