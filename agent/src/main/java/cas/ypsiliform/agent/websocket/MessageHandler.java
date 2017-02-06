package cas.ypsiliform.agent.websocket;

import cas.ypsiliform.messages.AbstractMessage;

public interface MessageHandler {

	void onNewMessage(AbstractMessage message);

}
