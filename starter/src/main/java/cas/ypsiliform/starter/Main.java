/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.starter;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

public class Main
{

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static Path logFilePath;

    //args:
    // 1. url to mediator-server 
    // 2. config file
    public static void main(String[] args)
        throws FileNotFoundException,
            IOException
    {
        Map<Integer, Process> spawnedProcesses  = new HashMap<>();
        Map<Integer, Double> storageCost        = new HashMap<>();
        Map<Integer, Double> setupCost          = new HashMap<>();
        Map<Integer, List<Integer>> requires    = new HashMap<>();
        Map<Integer, Integer> productionLimit   = new HashMap<>();
        Integer[] demands                       = new Integer[12];
        int numberOfRepetitionsPerConfig = 1;
        List<File> allFilesInFolder = new ArrayList<>();

        if ( args.length != 3 )
            throw new IllegalArgumentException("[ERROR] Three arguments are required. 1. url, 2. config file, 3. Number of test repetitions");

        String url = args[0];

        try {
            numberOfRepetitionsPerConfig = Integer.valueOf(args[2]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("[ERROR] " + args[2] + " is not a valid number.");
        }

        File possibleFolder = new File(args[1]);
        if(possibleFolder.isDirectory()) {
            allFilesInFolder = Files.walk(get(args[1])).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
        } else {
            allFilesInFolder.add(possibleFolder);
        }

        creatLogFile();

        for(File config : allFilesInFolder) {
            String fileEnding = "";
            int i = config.getName().lastIndexOf('.');
            if(i>0)
                fileEnding = config.getName().substring(i+1);

            if(!fileEnding.equals("req")) {
                System.out.println("Skip wrong fileEnding: " + fileEnding);
                continue;
            }

            System.out.println("Start test execution for " + config.getName());

            for(i = 1; i <= numberOfRepetitionsPerConfig;i++) {
                readConfigFile(config, storageCost, setupCost, demands, requires, productionLimit);
                spawnProcesses(config, url, storageCost, setupCost, demands, requires, productionLimit, spawnedProcesses);
                waitForProcessesToTerminate(spawnedProcesses);
                collectResults(config.getName(), i);
            }
        }
    }

    private static void collectResults(String configName, int iteration) throws IOException {
        Double sum = 0.0;
        StringBuilder mediatorSolution = new StringBuilder();
        StringBuilder csv = new StringBuilder();
        csv.append(configName + ";" + iteration + ";");

        for(int i=1;i<=5;i++) {
            String[] tokens = new String(readAllBytes(get("agent_" + i + "_result"))).split(";");
            Double costs = round( Double.valueOf(tokens[0]), 2);
            sum += costs;
            csv.append(costs + ";");
            mediatorSolution.append(tokens[1]);
        }

        sum = round(sum, 2);
        csv.append(sum + ";" + mediatorSolution.toString() + System.getProperty("line.separator"));

        Files.write(logFilePath, csv.toString().getBytes(), StandardOpenOption.APPEND);
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static void creatLogFile() {
        logFilePath = Paths.get("./resultSummary.csv");
        try {
            Files.createDirectories(logFilePath.getParent());
            Files.createFile(logFilePath);
            Files.write(logFilePath, "Testkonfiguration;Iteration;Agent 1;Agent 2;Agent 3;Agent 4;Agent 5;Gesamtkosten;Vorgabe des Mediators\n".getBytes());
        } catch (FileAlreadyExistsException e) {
            //File exists, so don't do anything
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Spawns 5 agent processes
     * */
    private static void spawnProcesses(File config,
                                       String url,
                                       Map<Integer, Double> storageCost,
                                       Map<Integer, Double> setupCost,
                                       Integer[] demands,
                                       Map<Integer, List<Integer>> requires,
                                       Map<Integer, Integer> productionLimit,
                                       Map<Integer, Process> spawnedProcesses
    ) throws IOException {
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
            pb.redirectErrorStream(true);
            spawnedProcesses.put(i, pb.start());
            System.out.println("Start agent " + i + " args " + Arrays.toString(processArgs));
        }
    }

    /**
     * Loops until all the spawned processes are completed
     * */
    private static void waitForProcessesToTerminate(Map<Integer, Process> spawnedProcesses) throws IOException {
        String s;
        Iterator it = spawnedProcesses.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Process p = (Process)pair.getValue();

            //Process.waitFor() terminates once the jar file was loaded, so check the outputstreams instead
            BufferedReader stdOut=new BufferedReader(new InputStreamReader(p.getInputStream()));
            while( (s=stdOut.readLine())!= null){
                //just wait
            }
            p.destroy();
        }
    }

    /**
     * Prepares the arguments required to spawn an agent process depending on the running OS
     * */
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

    /**
     * Reads the provided configuration file and sets the values accordingly
     * */
    private static void readConfigFile(File config,
                                       Map<Integer, Double> storageCost,
                                       Map<Integer, Double> setupCost,
                                       Integer[] demands,
                                       Map<Integer, List<Integer>> requires,
                                       Map<Integer, Integer> productionLimit
                                       ) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(config));
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
                List<Integer> requiresTmp = readRequiredList(split);
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

    /**
     * If an agent depends on more than one subagent, this dependecy is provided in the form of space separated String
     * and is transformed into a list of dependencies.
     * */
    private static List<Integer> readRequiredList(String[] split)
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
