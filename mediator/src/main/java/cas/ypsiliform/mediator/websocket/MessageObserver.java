package cas.ypsiliform.mediator.websocket;

import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;

import cas.ypsiliform.messages.AgentRegistration;

@Stateless
@LocalBean
public class MessageObserver {
	private static Logger log = Logger.getLogger(MessageObserver.class.getName());

	public void onMessage(@Observes AgentRegistration msg) {
		log.finer("on message test = " + msg);
	}
}
