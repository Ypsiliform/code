package cas.ypsiliform.messages.encoder;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;

public class MediatorRequestEncoder
    implements Encoder.Text<MediatorRequest>
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
    public String encode(MediatorRequest object)
        throws EncodeException
    {
        try
        {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("type", "mediatorrequest");
            builder.add("solutions", getSolutionArray(object.getSolutions()));
            return builder.build().toString();
        }
        catch ( Exception e )
        {
            throw new EncodeException(object, "Failed encode object", e);
        }
    }

    private JsonArrayBuilder getSolutionArray(Map<Integer, Solution> solutions)
    {
        JsonArrayBuilder array = Json.createArrayBuilder();
        solutions.entrySet().forEach(entry -> {
            JsonObjectBuilder obj = Json.createObjectBuilder();
            obj.add("no", entry.getKey());
            obj.add("demands", getJsonArray(entry.getValue().getDemands()));
            obj.add("solution", getJsonArray(entry.getValue().getSolution()));
            array.add(obj);
        });
        return array;
    }

    private JsonArrayBuilder getJsonArray(int[] intArray)
    {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for ( int value : intArray )
        {
            array.add(value);
        }
        return array;
    }

    private JsonArrayBuilder getJsonArray(boolean[] boolArray)
    {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for ( boolean value : boolArray )
        {
            array.add(value);
        }
        return array;
    }

}
