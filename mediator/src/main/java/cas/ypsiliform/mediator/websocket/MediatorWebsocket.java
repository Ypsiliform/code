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
import cas.ypsiliform.messages.decoder.IncomingMessageDecoder;
import cas.ypsiliform.messages.encoder.EndNegotiationEncoder;
import cas.ypsiliform.messages.encoder.ErrorMessageEncoder;
import cas.ypsiliform.messages.encoder.MediatorRequestEncoder;

@ServerEndpoint(value = "/mediator", decoders = { IncomingMessageDecoder.class }, encoders = {
        ErrorMessageEncoder.class, MediatorRequestEncoder.class, EndNegotiationEncoder.class })
public class MediatorWebsocket {

    private static final Logger LOGGER = Logger.getLogger(MediatorWebsocket.class.getName());

    private SessionRepository repo;

    @Inject
    private Event<NewMessageEvent> event;

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
        LOGGER.info("new session " + session);
        //        repo.addSession(session); 
    }

    @OnMessage
    public void onMessage(Session session, AbstractMessage receivedMsg) {
        LOGGER.finer("new message received : " + receivedMsg + " session = " + session);
        event.fire(new NewMessageEvent(session, receivedMsg));
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("session closed = " + session.getId());
        repo.removeSession(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable thrownable) {
        LOGGER.warning("session = " + session.getId() + " on error = " + thrownable.getMessage());
        repo.removeSession(session.getId());
    }

}
