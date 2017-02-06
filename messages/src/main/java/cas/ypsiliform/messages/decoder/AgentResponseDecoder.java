package cas.ypsiliform.messages.decoder;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.AgentResponse;

public class AgentResponseDecoder
    implements Decoder.Text<AgentResponse>
{

    public void init(EndpointConfig config)
    {

    }

    public void destroy()
    {

    }

    public AgentResponse decode(String s)
        throws DecodeException
    {
        AgentResponse msg = new AgentResponse();
        JsonReader reader = Json.createReader(new StringReader(s));
        JsonObject readObject = reader.readObject();
        int selection = readObject.getInt("selection");
        int cost = readObject.getInt("cost");
        Map<Integer, Integer[]> demands =
            getMap(readObject.getJsonArray("demands"));
        msg.setCost(cost);
        msg.setDemands(demands);
        msg.setSelection(selection);
        return msg;
    }

    private Map<Integer, Integer[]> getMap(JsonArray jsonArray)
    {
        Map<Integer, Integer[]> map = new HashMap<>();
        for ( int i = 0; i < jsonArray.size(); i++ )
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            JsonArray demandArray = jsonObject.getJsonArray("demand");
            Integer[] demands = new Integer[demandArray.size()];
            for ( int j = 0; j < demandArray.size(); j++ )
            {
                demands[j] = demandArray.getInt(j);
            }
            map.put(jsonObject.getInt("solution"), demands);
        }
        return map;
    }

    public boolean willDecode(String s)
    {
        JsonReader reader = Json.createReader(new StringReader(s));
        String string = reader.readObject().getString("type", "");
        return "agentresponse".equals(string);
    }

}
