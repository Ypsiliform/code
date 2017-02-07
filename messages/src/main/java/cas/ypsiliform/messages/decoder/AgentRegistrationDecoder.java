package cas.ypsiliform.messages.decoder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.AgentRegistration;

public class AgentRegistrationDecoder
    implements Decoder.Text<AgentRegistration>
{

    @Override
    public AgentRegistration decode(String s)
        throws DecodeException
    {
        AgentRegistration reg = new AgentRegistration();
        JsonReader reader = Json.createReader(new StringReader(s));
        JsonObject readObject = reader.readObject();
        int id = readObject.getInt("id");
        String config = readObject.getString("config");
        int[] requires = getArray(readObject.getJsonArray("requires"));
        int[] demand = getArray(readObject.getJsonArray("demand"));
        List<Integer> reqList = new ArrayList<>();
        for ( int i = 0; i < requires.length; i++ )
        {
            reqList.add(requires[i]);
        }
        reg.setId(id);
        reg.setConfig(config);
        reg.setRequires(reqList);
        reg.setDemand(demand);
        return reg;
    }

    private int[] getArray(JsonArray jsonArray)
    {
        int[] array = new int[jsonArray.size()];
        for ( int i = 0; i < jsonArray.size(); i++ )
        {
            array[i] = jsonArray.getInt(i);
        }
        return array;
    }

    @Override
    public boolean willDecode(String s)
    {
        JsonReader reader = Json.createReader(new StringReader(s));
        String string = reader.readObject().getString("type", "");
        return "agentregistration".equals(string);
    }

    @Override
    public void init(EndpointConfig config)
    {

    }

    @Override
    public void destroy()
    {

    }

}
