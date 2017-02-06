package cas.ypsiliform.agent.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import cas.ypsiliform.messages.AgentRegistration;

public class Main {

	public static void main(String[] args) {
		try {
			WebsocketClient client = new WebsocketClient(new URI("ws://localhost/mediator/mediator"));
			AgentRegistration reg = new AgentRegistration();
			reg.setId(1234);
			reg.setConfig("blabliblub");
			client.sendMessage(reg);
			Thread.sleep(6000);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
