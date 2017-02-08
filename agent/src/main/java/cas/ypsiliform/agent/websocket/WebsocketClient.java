package cas.ypsiliform.agent.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import cas.ypsiliform.messages.AbstractMessage;
import cas.ypsiliform.messages.decoder.IncomingMessageDecoder;
import cas.ypsiliform.messages.encoder.AgentRegistrationEncoder;
import cas.ypsiliform.messages.encoder.ErrorMessageEncoder;
import cas.ypsiliform.messages.encoder.AgentResponseEncoder;
import cas.ypsiliform.messages.encoder.StartNegotiationEncoder;

@ClientEndpoint(encoders = { ErrorMessageEncoder.class, AgentRegistrationEncoder.class, AgentResponseEncoder.class,
		StartNegotiationEncoder.class }, decoders = { IncomingMessageDecoder.class })
public class WebsocketClient {

	private Session userSession;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private List<MessageHandler> handlers = new ArrayList<>();

	public WebsocketClient(URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@OnOpen
	public void onOpen(Session userSession) {
		this.userSession = userSession;
	}

	@OnMessage
	public void onMessage(Session userSession, AbstractMessage message) {
		System.out.println("new message = " + message);
		lock.readLock().lock();
		try {
			handlers.forEach(handler -> {
				handler.onNewMessage(message);
			});
		} finally {
			lock.readLock().unlock();
		}
	}

	public void addMessageHandler(MessageHandler handler) {
		lock.writeLock().lock();
		try {
			this.handlers.add(handler);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void sendMessage(AbstractMessage message)  {
		try {
			this.userSession.getBasicRemote().sendObject(message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
