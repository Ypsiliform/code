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

import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;

public class MediatorRequestDecoder
    implements Decoder.Text<MediatorRequest>
{

    @Override
    public void init(EndpointConfig config)
    {

    }

    @Override
    public void destroy()
    {

    }

    @Override
    public MediatorRequest decode(String s)
        throws DecodeException
    {
        MediatorRequest msg = new MediatorRequest();
        JsonReader reader = Json.createReader(new StringReader(s));
        JsonObject readObject = reader.readObject();
        Map<Integer, Solution> map =
            getMap(readObject.getJsonArray("solutions"));
        msg.setSolutions(map);
        return msg;
    }

    private Map<Integer, Solution> getMap(JsonArray jsonArray)
    {
        Map<Integer, Solution> map = new HashMap<>();
        for ( int i = 0; i < jsonArray.size(); i++ )
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            Solution sol = new Solution();
            sol.setDemands(getArray(jsonObject.getJsonArray("demands")));
            sol.setSolution(getBooleanArray(jsonObject
                .getJsonArray("solution")));
            map.put(jsonObject.getInt("no"), sol);
        }
        return map;
    }

    private Integer[] getArray(JsonArray jsonArray)
    {
        Integer[] array = new Integer[jsonArray.size()];
        for ( int i = 0; i < jsonArray.size(); i++ )
        {
            array[i] = jsonArray.getInt(i);
        }
        return array;
    }

    private boolean[] getBooleanArray(JsonArray jsonArray)
    {
        boolean[] array = new boolean[jsonArray.size()];
        for ( int i = 0; i < jsonArray.size(); i++ )
        {
            array[i] = jsonArray.getBoolean(i);
        }
        return array;
    }

    @Override
    public boolean willDecode(String s)
    {
        JsonReader reader = Json.createReader(new StringReader(s));
        String string = reader.readObject().getString("type", "");
        return "mediatorrequest".equals(string);
    }

}
