/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.starter;

import java.io.*;
import java.util.*;

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

        Map<Integer, Process> spawnedProcesses = new HashMap<>();
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
                    storageCost.put(Integer.valueOf(split[0].trim()),
                                    Double.valueOf(split[1].trim()));
                }
                if ( lineCount >= 5 && lineCount < 10 )
                {
                    setupCost.put(Integer.valueOf(split[0].trim()),
                                  Double.valueOf(split[1].trim()));
                }
                if ( lineCount >= 10 && lineCount < 22 )
                {
                    demands[lineCount - 10] = Integer.valueOf(split[1].trim());
                }
                if ( lineCount >= 22 && lineCount < 27 )
                {
                    List<Integer> requiresTmp = readList(split);
                    requires.put(Integer.valueOf(split[0].trim()), requiresTmp);
                }
                if ( lineCount >= 27 && lineCount < 32 )
                {
                    productionLimit.put(Integer.valueOf(split[0].trim()),
                                        Integer.valueOf(split[1].trim()));
                }
                lineCount++;
            }
        }

        for ( int i = 1; i < 6; i++ )
        {
            Integer[] demandsTmp = new Integer[0];
            if ( i == 1 )
            {
                demandsTmp = demands;
            }
            String[] processArgs = getArgs(i,
                                           config.getName(),
                                           url,
                                           storageCost.get(i),
                                           setupCost.get(i),
                                           demandsTmp,
                                           requires.get(i),
                                           productionLimit.get(i));
            ProcessBuilder pb = new ProcessBuilder(processArgs);
            spawnedProcesses.put(i, pb.start());
            System.out.println("Start agent " + i + " args " + Arrays.toString(processArgs));
        }

        Iterator it = spawnedProcesses.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Process p = (Process)pair.getValue();
            BufferedReader stdOut=new BufferedReader(new InputStreamReader(p.getInputStream()));

            String s;
            while( (s=stdOut.readLine())!= null){
                //just wait
                try
                {
                    Thread.sleep(100);
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }

            }
            System.out.println("Process for agent " + (Integer)pair.getKey() + "has terminated");
        }
    }

    private static String[] getArgs(int id,
                                    String config,
                                    String url,
                                    double storageCost,
                                    double setupCost,
                                    Integer[] demands,
                                    List<Integer> requires,
                                    Integer productionLimit)
    {
        String[] args = new String[0];
        if ( isWindows() )
        {
            args =
                new String[] { "cmd", "/c", "start", "java", "-jar",
                    "agent.jar", String.valueOf(id), config, url,
                    String.valueOf(storageCost), String.valueOf(setupCost),
                    Arrays.toString(demands).replaceAll("\\s+", ""),
                    Arrays
                        .toString(requires
                            .toArray(new Integer[requires.size()]))
                        .replaceAll("\\s+", ""),
                    String.valueOf(productionLimit) };
        }
        else if ( isUnix() )
        {
            args =
                new String[] { "/bin/bash", "-c", "java", "-jar",
                    "agent.jar", String.valueOf(id), config, url,
                    String.valueOf(storageCost), String.valueOf(setupCost),
                    Arrays.toString(demands).replaceAll("\\s+", ""),
                    Arrays
                        .toString(requires
                            .toArray(new Integer[requires.size()]))
                        .replaceAll("\\s+", ""),
                    String.valueOf(productionLimit) };
        }
        else if ( isMac() )
        {
            args = new String[] { "osascript", "-e",
                "tell application \"Terminal\" to do script \"java -jar agent.jar\"" };
        }
        return args;
    }

    private static List<Integer> readList(String[] split)
    {
        List<Integer> requires = new ArrayList<>();
        if ( split.length == 1 )
        {
            return requires;
        }
        for ( int i = 1; i < split.length; i++ )
        {
            requires.add(Integer.valueOf(split[i].trim()));
        }
        return requires;
    }

    public static boolean isWindows()
    {

        return OS.contains("win");

    }

    public static boolean isMac()
    {

        return OS.contains("mac");

    }

    public static boolean isUnix()
    {

        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");

    }

    public static boolean isSolaris()
    {

        return OS.contains("sunos");

    }

}
