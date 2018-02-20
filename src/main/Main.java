package main;
import javafx.util.Pair;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    private static void readQueueAndEventCommands(ArrayList<String> queueCommands, ArrayList<String> eventCommands, String fileName){
        try {
            // Open the file of the given parent
            BufferedReader reader = new BufferedReader(new FileReader("../EvoSim/robots/joebot/Joebot.data/" + fileName));

            // Read lines until we see a blank line
            String line;
            while (!(line = reader.readLine()).equals("")) {
                queueCommands.add(line);
            }

            for (int k = 0; k < 4; k++) {
                line = reader.readLine();
                eventCommands.add(line);
            }

            reader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static void writeQueueAndEventCommands(ArrayList<String> queueCommands, ArrayList<String> eventCommands, String fileName){

        try {

            // Create the file
            PrintWriter writer = new PrintWriter("../EvoSim/robots/joebot/Joebot.data/" + fileName, "UTF-8");

            // Write the command queue
            for (String queueCommand : queueCommands) {
                writer.println(queueCommand);
            }

            // A space so that the robot separate the queue commands from the event commands
            writer.println("");

            // Write the event commands
            for (String eventCommand : eventCommands) {
                writer.println(eventCommand);
            }

            writer.close();

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomCommand(){
        int functionOptions = 22;
        double functionValMin = 0.0;
        double functionValMax = 500.0;
        Random rand = new Random();

        double functionVal = functionValMin + (functionValMax - functionValMin) * rand.nextDouble();
        int functionIndex = rand.nextInt(functionOptions);
        return functionIndex + "," + functionVal;
    }

    private static ArrayList<String> createPopulationFiles(int populationSize){
        int sequenceCountMax = 20;
        Random rand = new Random();
        ArrayList<String> fileNames = new ArrayList<>();

        for(int i = 0; i < populationSize; i++){

            ArrayList<String> robotCommandQueue = new ArrayList<>();
            ArrayList<String> robotEventQueue = new ArrayList<>();

            fileNames.add((i + 1) + ".txt");

            int thisSequenceCount = rand.nextInt(sequenceCountMax - 1) + 1;
            for(int j = 0; j < thisSequenceCount; j++){
                robotCommandQueue.add(getRandomCommand());
            }

            for(int j = 0; j < 4; j++){
                robotEventQueue.add(getRandomCommand());
            }

            writeQueueAndEventCommands(robotCommandQueue, robotEventQueue, (i + 1) + ".txt");
        }

        return fileNames;
    }

    private static void setJoeBotFilePointer(String fileLocation){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("../EvoSim/robots/joebot/Joebot.data/filePointer.txt", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert writer != null;

        writer.println(fileLocation);
        writer.close();
    }

    private static void createJoeBotLog(){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("../EvoSim/robots/joebot/Joebot.data/scores.txt", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert writer != null;

        writer.println("JoeBot Scores");
        writer.close();
    }

    private static String getRobotLoadString(int totalBots, int joeBots){

        Random rand = new Random();

        ArrayList<String> botNames = new ArrayList<String>(){{
//            add("sample.Corners");
            add("sample.Crazy");
//            add("sample.Fire");
//            add("sample.RamFire");
//            add("sample.SittingDuck");
//            add("sample.SpinBot");
//            add("sample.Tracker");
//            add("sample.VelociRobot");
//            add("sample.Walls");
        }};

        int totalCreated = 0;

        StringBuilder str = new StringBuilder();
        for(int i = 0; i < joeBots; i++){

            if(totalCreated < totalBots) {
                str.append("joebot.Joebot*,");
            }
        }

        while (totalCreated < totalBots){
            int randIndex = rand.nextInt(botNames.size());
            str.append(botNames.get(randIndex)).append(",");
            totalCreated++;
        }

        return str.toString();
    }

    private static ArrayList<Pair<String, Integer>> getGenerationScores(){
        ArrayList<Pair<String, Integer>> scores = new ArrayList<>();

        try {
            // Open the scores file
            BufferedReader reader = new BufferedReader(new FileReader("../EvoSim/robots/joebot/Joebot.data/scores.txt"));

            // Read lines until we see a blank line
            String line;

            // the first line isn't a result so just ignore it
            reader.readLine();

            // while we haven't reached the end of the file
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(" ");
                scores.add(new Pair<>(lineSplit[0], Integer.parseInt(lineSplit[1])));
            }

            // we are done with the file so close it
            reader.close();
        }
        catch (IOException | NumberFormatException e){
            e.printStackTrace();
        }

        return scores;
    }

    private static ArrayList<Double> getPercentageShares(ArrayList<Pair<String, Integer>> scores){

        // Add up the total score so we can get a value to work out the percentage
        int totalScore = 0;
        for (Pair<String, Integer> score : scores) {
            totalScore += score.getValue();
        }

        // Create an arrayList of all of the percentages, this will line up by index with the scores arrayList
        ArrayList<Double> percentages = new ArrayList<>();
        for (Pair<String, Integer> score : scores) {

            // Add to the percentage list
            percentages.add((double) score.getValue() / (double) totalScore * 100.0);

            // Display the percentage share of each file score
            //System.out.println("[" + scores.get(i).getKey() + "] scored [" + scores.get(i).getValue() + "] -> [" +
            //        + (float)scores.get(i).getValue() / (float)totalScore * 100.0f + "%]");
        }

        return percentages;
    }

    private static ArrayList<ArrayList<String>> parentSelection(ArrayList<Pair<String, Integer>> scores, int childrenToCreate){
        ArrayList<ArrayList<String>> parentArray = new ArrayList<>();

        ArrayList<Double> percentages = getPercentageShares(scores);

        // Create a random object for picking the parents
        Random rand = new Random();

        for(int i = 0; i < childrenToCreate; i++){

            parentArray.add(new ArrayList<>());

            // For the two parents we want to get
            for(int j = 0; j < 2; j++){

                // Pick a value between 0 and 100
                int randomSelection = rand.nextInt(100);

                // Cycle through the percentages and checking to see if the random score is within that percentage
                float currentPercentage = 0;
                for(int k = 0; k < percentages.size(); k++){

                    // if the value is within the percentage segment
                    if(randomSelection >= currentPercentage && randomSelection <= currentPercentage + percentages.get(k)){

                        // set the parent to the key of the score that was picked
                        parentArray.get(parentArray.size() - 1).add(scores.get(k).getKey());
                    }

                    // increment the current percentage
                    currentPercentage += percentages.get(k);
                }
            }
        }

        // return the big list of parents to the call
        return parentArray;
    }

    private static ArrayList<String> crossover(ArrayList<ArrayList<String>> parents, int lastFileNumber){

        ArrayList<String> childrenFileNames = new ArrayList<>();

        // create a random object too keep crossover interesting
        Random rand = new Random();

        // for each set of parents
        for (ArrayList<String> parent : parents) {

            //System.out.println(parent.get(0) + " and " + parent.get(1));

            // create a place for the commands of each file to be stored
            ArrayList<ArrayList<String>> queueCommands = new ArrayList<>();
            ArrayList<ArrayList<String>> eventCommands = new ArrayList<>();

            for (int j = 0; j < 2; j++) {
                //System.out.println("FILE [" + j + "] is [" + "../EvoSim/robots/joebot/Joebot.data/" + parent.get(j) + "]");
                queueCommands.add(new ArrayList<>());
                eventCommands.add(new ArrayList<>());

                readQueueAndEventCommands(queueCommands.get(j), eventCommands.get(j), parent.get(j));
            }

            // We have the commands of each parent in an easy format so we can now crossover
            // CROSSOVER FOR QUEUE COMMANDS
            // Take the command queue size from one of the parents, this is already random from selection so take it from the first
            int commandQueueSize = queueCommands.get(0).size();

            // create a step size for the crossover of queue commands to swap on it must be at least 3 and could be maximum 6
            int randomStep = rand.nextInt(4) + 4;

            ArrayList<String> childQueueCommands = new ArrayList<>();

            // start by reading from the left file
            boolean readFromFileOne = true;

            // for all of the command attempts we need to make step by the alternating factor
            for (int j = 0; j < commandQueueSize; j += randomStep) {

                // for the random step that we have skipped by
                for (int k = 0; k < randomStep; k++) {

                    // check we aren't above the the command queue size that we wanted and that the given file has enough commands to take from
                    if (j + k < commandQueueSize && j + k < queueCommands.get(readFromFileOne ? 0 : 1).size()) {

                        // store the command index from the given file
                        childQueueCommands.add(queueCommands.get(readFromFileOne ? 0 : 1).get(j + k));
                    }
                }

                // swap which file to take commands from every <randomStep> commands
                readFromFileOne = !readFromFileOne;
            }

            // CROSSOVER FOR EVENT COMMANDS
            ArrayList<String> childEventCommands = new ArrayList<>();

            // we will take 3 of the event commands from the left file and 1 command from the right
            for (int j = 0; j < 3; j++) {
                childEventCommands.add(eventCommands.get(0).get(j));
            }
            childEventCommands.add(eventCommands.get(1).get(3));

            // CROSSOVER COMPLETE :D!
            // the child queue command size could be smaller than expected if the right file has less commands
            // than the left this is okay because ideally we would favour smaller command sets as there is an
            // upper limit anyway

            // Create a file for this child incrementing the last known file name
            childrenFileNames.add((++lastFileNumber) + ".txt");
            writeQueueAndEventCommands(childQueueCommands, childEventCommands,lastFileNumber + ".txt");
        }

        return childrenFileNames;
    }

    private static ArrayList<String> killParents(ArrayList<Pair<String, Integer>> scores, int parentsToKeep, boolean deleteKilled){
        ArrayList<String> survivorFileNames = new ArrayList<>();
        ArrayList<Double> percentages = getPercentageShares(scores);
        int currentTotalPercentageShare = 100;
        Random rand = new Random();

        for(int i = 0; i < parentsToKeep; i++){

            // Pick a value between 0 and the percentage share and just returns 0 if none of the robots did well enough to get a percentage share
            int randomSelection = currentTotalPercentageShare < 1 ? 0 : rand.nextInt(currentTotalPercentageShare);

            // Cycle through the percentages and checking to see if the random score is within that percentage
            float currentPercentage = 0;
            for(int k = 0; k < percentages.size(); k++){

                // if the value is within the percentage segment
                if(randomSelection >= currentPercentage && randomSelection <= currentPercentage + percentages.get(k)){

                    // set the parent to the key of the score that was picked
                    survivorFileNames.add(scores.get(k).getKey());

                    // change the random number maximum
                    currentTotalPercentageShare -= percentages.get(k);

                    // remove the robot we just saved from the list
                    scores.remove(k);

                    // remove the entry from the percentages list
                    percentages.remove(k);

                    // break out of this inner for loop and continue to pick another parent to save
                    break;
                }

                // increment the current percentage
                currentPercentage += percentages.get(k);
            }
        }

        // only delete things if you want to
        if(deleteKilled){
            for (Pair<String, Integer> score : scores) {

                // delete the files that are still in the scores list
                File file = new File("../EvoSim/robots/joebot/Joebot.data/" + score.getKey());
                if (!file.delete()) {
                    System.out.println("Deleting [" + score.getKey() + "] failed");
                }
            }
        }

        return survivorFileNames;
    }

    private static void mutateChildren(ArrayList<String> childrenFileNames, int functionSwitchOutChance, int parameterMutationChance, int variableScaleMaxPercentage){

        Random rand = new Random();

        for(int i = 0; i < childrenFileNames.size(); i++){

            ArrayList<ArrayList<String>> commands = new ArrayList<>();
            commands.add(new ArrayList<>());
            commands.add(new ArrayList<>());

            readQueueAndEventCommands(commands.get(0), commands.get(1), childrenFileNames.get(i));

            // Increase or decrease the parameter value of the commands randomly
            for(int j = 0; j < commands.size(); j++){
                for(int k = 0; k < commands.get(j).size(); k++){
                    if(rand.nextInt(100) < parameterMutationChance){
                        String[] splitCommand = commands.get(j).get(k).split(",");
                        double value = Double.parseDouble(splitCommand[1]);
                        double randomMutateFactor = (rand.nextInt(variableScaleMaxPercentage * 2) - variableScaleMaxPercentage) / 100.0;
                        double result = value + (value * randomMutateFactor);
                        commands.get(j).set(k, splitCommand[0] + "," + result);
                    }
                }
            }

            // remove a command from the queue command list make sure we have at least one command though
            if(commands.get(0).size() > 1 && rand.nextInt(100) < functionSwitchOutChance){

                // pick a random command to remove
                commands.get(0).remove(rand.nextInt(commands.get(0).size()));
            }

            // remove a command from the queue command list
            if(rand.nextInt(100) < functionSwitchOutChance){

                // pick a random command to remove
                commands.get(0).add(rand.nextInt(commands.get(0).size()), getRandomCommand());
            }

            writeQueueAndEventCommands(commands.get(0), commands.get(1), childrenFileNames.get(i));
        }
    }

    private static void outputScores(ArrayList<Pair<String, Integer>> scores){

        int lowest = scores.get(0).getValue();
        int highest = scores.get(0).getValue();
        double average = 0;

        for(int i = 0; i < scores.size(); i++){
            if(scores.get(i).getValue() < lowest){
                lowest = scores.get(i).getValue();
            }
            if(scores.get(i).getValue() > highest){
                highest = scores.get(i).getValue();
            }
            average += scores.get(i).getValue();
        }

        average /= (double)(scores.size());

        System.out.println("\tFitness Low     [" + lowest + "]");
        System.out.println("\tFitness High    [" + highest + "]");
        System.out.println("\tFitness Average [" + average + "]");

        writeDataPoint("../EvoSim/robots/joebot/Joebot.data/logger.txt", highest, lowest, average);
    }

    public static ArrayList<ArrayList<Double>> readData(String fileName){

        ArrayList<ArrayList<Double>> data = new ArrayList<>();

        try {
            // Open the file of the given parent
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            // Read lines until we see a blank line
            String line;
        while ((line = reader.readLine()) != null) {
                data.add(new ArrayList<>());
                String[] lineSplit = line.split(",");
                for (String aLineSplit : lineSplit) data.get(data.size() - 1).add(Double.parseDouble(aLineSplit));
            }
            reader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static void drawChart(){

        ArrayList<ArrayList<Double>> data = readData("../EvoSim/robots/joebot/Joebot.data/logger.txt");

        double[] highData = new double[data.size()];
        double[] lowData = new double[data.size()];
        double[] averageData = new double[data.size()];

        for(int i = 0; i < data.size(); i++){
            highData[i] = data.get(i).get(0);
            lowData[i] = data.get(i).get(1);
            averageData[i] = data.get(i).get(2);
        }

        XYChart chart = new XYChartBuilder().width(800).height(600).xAxisTitle("X").yAxisTitle("Y").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.addSeries("High", null, highData).setMarker(SeriesMarkers.NONE);
        chart.addSeries("Low", null, lowData).setMarker(SeriesMarkers.NONE);
        chart.addSeries("Average", null, averageData).setMarker(SeriesMarkers.NONE);

        new SwingWrapper<>(chart).displayChart();
    }

    public static void writeDataPoint(String fileName, double high, double low, double average){
        try {

            // Create the file
            FileWriter writer = new FileWriter(fileName, true);

            // Write the command queue
            writer.append(String.valueOf(high)).append(",").append(String.valueOf(low)).append(",").append(String.valueOf(average)).append("\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearFile(String fileName){
        try {

            // Create the file
            FileWriter writer = new FileWriter(fileName);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

        clearFile("../EvoSim/robots/joebot/Joebot.data/logger.txt");

        // Create an instance of RobocodeEngine to run the battles
        RobocodeEngine.setLogMessagesEnabled(true);
        RobocodeEngine.setLogErrorsEnabled(true);
        RobocodeEngine robocodeEngine = new RobocodeEngine();

        // Create a population of JoeBots
        int populationSize = 20;
        ArrayList<String> robotFiles = createPopulationFiles(populationSize);

        // Define how many generations to cycle through
        int generationCount = 100;

        // Some more explanatory variables
        int lastFileNumber = populationSize;
        int childrenToCreate = (int)(populationSize / 1.5f);
        int robotsToFight = 5;
        int roundsPerFight = 10;

        for(int i = 0; i < generationCount; i++) {

            if(i == generationCount - 1){
                robocodeEngine.setVisible(true);
            }

            // Get a string of random robots to challenge this round, make sure there is one JoeBot
            String robotoString = getRobotLoadString(robotsToFight, 1);

            // Using the string we made earlier, get a collection of robot specifications
            RobotSpecification[] allRobots = robocodeEngine.getLocalRepository(robotoString);

            // Create a Battle specification using these robots
            BattleSpecification battleSpecification = new BattleSpecification(new BattlefieldSpecification(),
                    roundsPerFight, 10, 5, 50, false, allRobots);

            // create (or overwrite) the score logging file for this generation
            createJoeBotLog();

            // For the entire JoeBot population size
            System.out.println("Generation [" + i + "] -> Testing population");
            for (int j = 0; j < populationSize; j++) {

                // Point the given JoeBot to its data file
                setJoeBotFilePointer(robotFiles.get(j));

                // Run the battle and wait for it to finish before we continue
                robocodeEngine.runBattle(battleSpecification);
                robocodeEngine.waitTillBattleOver();
            }

            // The generation finished their fighting so read the results
            System.out.println("Generation [" + i + "] -> Scores");
            ArrayList<Pair<String, Integer>> scores = getGenerationScores();
            outputScores(scores);

            // Begin the parent selection process using the scores
            System.out.println("Generation [" + i + "] -> Selecting parents");
            ArrayList<ArrayList<String>> parentPairs = parentSelection(scores, childrenToCreate);

            // Begin the crossover stage using the parent groups formed
            System.out.println("Generation [" + i + "] -> Creating [" + childrenToCreate + "] children");
            ArrayList<String> childrenFileNames = crossover(parentPairs, lastFileNumber);

            // Randomly mutate children
            System.out.println("Generation [" + i + "] -> Mutating children");
            mutateChildren(childrenFileNames, 30, 30, 30);

            // Kill parents (kill as many parents as children we created to keep the population constant)
            System.out.println("Generation [" + i + "] -> Killing [" + childrenToCreate + "] parents");
            ArrayList<String> parentFileNames = killParents(scores, populationSize - childrenToCreate, true);

            // Merge the children and parent file names ready for the next iteration
            robotFiles.clear();
            robotFiles.addAll(parentFileNames);
            robotFiles.addAll(childrenFileNames);

            // Increment the last created file number
            lastFileNumber += childrenToCreate;

            // Keeping the logging pretty
            System.out.println("");
        }

        // close the robocode instance
        robocodeEngine.close();

        drawChart();
    }
}