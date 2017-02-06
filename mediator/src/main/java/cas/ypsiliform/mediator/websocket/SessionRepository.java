package cas.ypsiliform.mediator.websocket;

import javax.websocket.Session;

public interface SessionRepository {

	String LOOKUP = "java:global/mediator/SessionRepositoryBean!cas.ypsiliform.mediator.websocket.SessionRepository";

	void addSession(Session session);

	void removeSession(String id);

}
