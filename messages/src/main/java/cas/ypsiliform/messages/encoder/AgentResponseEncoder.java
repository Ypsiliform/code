package cas.ypsiliform.messages.encoder;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.AgentResponse;

public class AgentResponseEncoder
    implements Encoder.Text<AgentResponse>
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
    public String encode(AgentResponse object)
        throws EncodeException
    {
        try
        {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("type", "agentresponse");
            builder.add("selection", object.getSelection());
            builder.add("cost", object.getCost());
            builder.add("demands", buildMap(object.getDemands()));
            return builder.build().toString();
        }
        catch ( Exception e )
        {
            throw new EncodeException(object, "Failed encode object", e);
        }
    }

    private JsonArrayBuilder buildMap(Map<Integer, Integer[]> demands)
    {
        JsonArrayBuilder array = Json.createArrayBuilder();
        demands.entrySet().forEach((entry) -> {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("solution", entry.getKey());
            JsonArrayBuilder demandArray = Json.createArrayBuilder();
            for ( int i = 0; i < entry.getValue().length; i++ )
            {
                demandArray.add(entry.getValue()[i]);
            }
            builder.add("demand", demandArray);
            array.add(builder);
        });
        return array;
    }

}
