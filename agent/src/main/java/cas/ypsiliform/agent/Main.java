package cas.ypsiliform.agent;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Main
{

    public static void main(String[] args)
    {
        if ( args.length != 8 )
        {
            throw new IllegalArgumentException("8 arguments require!");
        }
        int id = Integer.valueOf(args[0]);
        System.out.println("I'm Agent No. " + id);
        String config = args[1];
        String url = args[2];
        double storageCost = Double.valueOf(args[3]);
        double setupCost = Double.valueOf(args[4]);
        String[] demandStrAry =
            args[5].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        Integer[] demands = new Integer[demandStrAry.length];
        if ( demandStrAry.length > 1 )
        {
            for ( int i = 0; i < demandStrAry.length; i++ )
            {
                demands[i] = Integer.valueOf(demandStrAry[i]);
            }
        }
        String[] requiresStrAry =
            args[6].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        ArrayList<Integer> requires = new ArrayList<>();
        if ( requiresStrAry.length >= 1 )
        {
            for ( String str : requiresStrAry )
            {
                if ( str.trim().isEmpty() )
                {
                    continue;
                }
                requires.add(Integer.valueOf(str));
            }
        }
        int productionLimit = Integer.valueOf(args[7]);

        try
        {
            Agent a = new Agent(id,
                                setupCost,
                                storageCost,
                                productionLimit,
                                requires,
                                new URI(url),
                                demands,
                                config);
            a.startConnection();

            while ( a.isRunning() )
            {
                try
                {
                    Thread.sleep(100);
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
        }
        catch ( URISyntaxException | ConnectException e )
        {
            e.printStackTrace();
        }
    }

}
