package cas.ypsiliform.messages.encoder;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cas.ypsiliform.messages.EndNegotiation;

public class EndNegotiationEncoder
    implements Encoder.Text<EndNegotiation>
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
    public String encode(EndNegotiation object)
        throws EncodeException
    {
        try
        {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("type", "endnegotiation");
            builder.add("solution",
                        getJsonArray(object.getSolution().getSolution()));
            builder.add("demands",
                        getJsonArray(object.getSolution().getDemands()));
            return builder.build().toString();
        }
        catch ( Exception e )
        {
            throw new EncodeException(object, "Failed encode object", e);
        }
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
