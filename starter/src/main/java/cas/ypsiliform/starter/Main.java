/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main
{

    private static String OS = System.getProperty("os.name").toLowerCase();

    //args:
    // 1. url to mediator-server 
    // 2. config file
    public static void main(String[] args)
        throws FileNotFoundException,
            IOException
    {
        if ( args.length != 2 )
        {
            throw new IllegalArgumentException("two arguments are required. 1. url, 2. config file");
        }

        String url = args[0];
        File config = new File(args[1]);

        Map<Integer, Double> storageCost = new HashMap<>();
        Map<Integer, Double> setupCost = new HashMap<>();
        Integer[] demands = new Integer[12];
        Map<Integer, List<Integer>> requires = new HashMap<>();
        Map<Integer, Integer> productionLimit = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(config)))
        {
            String line;
            boolean first = true;
            int lineCount = 0;

            while ( (line = br.readLine()) != null )
            {
                if ( first )
                {
                    first = false;
                    continue;
                }
                if ( line.trim().length() <= 0 )
                {
                    continue;
                }
                //split any whitespace
                String[] split = line.split("\\s+");
                if ( lineCount < 5 )
                {
                    storageCost.put(Integer.valueOf(split[0].trim()), value);
                }
                lineCount++;
            }
        }

    }

    public static boolean isWindows()
    {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac()
    {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix()
    {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0
            || OS.indexOf("aix") > 0);

    }

    public static boolean isSolaris()
    {

        return (OS.indexOf("sunos") >= 0);

    }

}
