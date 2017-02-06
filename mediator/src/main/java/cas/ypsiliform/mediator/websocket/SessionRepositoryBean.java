package cas.ypsiliform.mediator.websocket;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.websocket.Session;

@Singleton
@Local(SessionRepository.class)
public class SessionRepositoryBean implements SessionRepository {

	private Map<String, Session> sessions = new HashMap<>();

	@Override
	public void addSession(Session session) {
		sessions.put(session.getId(), session);
	}

	@Override
	public void removeSession(String id) {
		sessions.remove(id);
	}

}
