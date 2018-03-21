package main;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import robocode.Robot;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class GeneticSimulation {

    private int populationSize;
    private int generationCount;
    private int childrenToCreate;
    private int childrenToRandom;
    private int functionOptions;
    private int sequenceCountMax;
    private int eventSequenceMax;
    private int ramDamageMultiplier;
    private int bulletDamageMultiplier;
    private int timesFirstMultiplier;
    private int timesSecondMultiplier;
    private int timesThirdMultiplier;
    private int functionSwitchOutChance;
    private int parameterMutationChance;
    private int variableScalePercentage;
    private int seeTopResultsOf;
    private double functionValMin;
    private double functionValMax;
    private RobocodeEngine robocodeEngine;
    private ArrayList<RobotData> robots;
    private RobotSpecification[] allRobots;
    private int lastFileNumber;
    private BattleSpecification battleSpecification;
    private boolean deleteKilled = true;
    private ArrayList<String> botNames;

    GeneticSimulation(int populationSize, int generationCount, int childrenToCreate, int childrenToRandom,
                      int enemies, int roundsPerFight, double functionValMin, double functionValMax,
                      int sequenceCountMax, int eventSequenceMax, int ramDamageMultiplier, int bulletDamageMultiplier,
                      int timesFirstMultiplier, int timesSecondMultiplier, int timesThirdMultiplier,
                      int functionSwitchOutChance, int parameterMutationChance, int variableScalePercentage,
                      ArrayList<String> botNames, int seeTopResultsOf){

        // Make sure the choices are valid
        assert childrenToCreate + childrenToRandom <= populationSize;
        assert seeTopResultsOf <= populationSize;

        // Assign variables
        this.populationSize = populationSize;
        this.generationCount = generationCount;
        this.childrenToCreate = childrenToCreate;
        this.childrenToRandom = childrenToRandom;
        this.functionValMin = functionValMin;
        this.functionValMax = functionValMax;
        this.sequenceCountMax = sequenceCountMax;
        this.eventSequenceMax = eventSequenceMax;
        this.ramDamageMultiplier = ramDamageMultiplier;
        this.bulletDamageMultiplier = bulletDamageMultiplier;
        this.timesFirstMultiplier = timesFirstMultiplier;
        this.timesSecondMultiplier = timesSecondMultiplier;
        this.timesThirdMultiplier = timesThirdMultiplier;
        this.functionSwitchOutChance = functionSwitchOutChance;
        this.parameterMutationChance = parameterMutationChance;
        this.variableScalePercentage = variableScalePercentage;
        this.botNames = botNames;
        this.seeTopResultsOf = seeTopResultsOf;

        // This isn't really adjustable unless more functions are added to the robot java file
        this.functionOptions = 22;

        // Create an instance of RobocodeEngine to run the battles
        RobocodeEngine.setLogMessagesEnabled(true);
        RobocodeEngine.setLogErrorsEnabled(true);
        robocodeEngine = new RobocodeEngine();

        // keep track of how many files we have created
        lastFileNumber = populationSize;

        // Get a string of random robots to challenge this round, make sure there is one JoeBot
        String robotString = getRobotLoadString(enemies, 1);

        // Using the string we made earlier, get a collection of robot specifications
        allRobots = robocodeEngine.getLocalRepository(robotString);

        // Create a Battle specification using these robots
        battleSpecification = new BattleSpecification(new BattlefieldSpecification(), roundsPerFight, 10,
                                                        5, 50, false, allRobots);

        // Create (or overwrite) the score logging file for this generation
        createJoeBotLog();

        // Create the initial population
        System.out.println("Generating initial population");
        robots = new ArrayList<>();
        generateInitialPopulation();
    }

    public void run(){

        // clear the logger so that old data isn't graphed
        System.out.println("Clearing logging file");
        clearFile("../EvoSim/robots/joebot/Joebot.data/logger.txt");

        for(int i = 0; i < generationCount; i++) {

            // Reset the scores file
            createJoeBotLog();

            // Start a battle for each robot
            for (RobotData robot : robots) {

                // Point the given JoeBot to its data file
                setJoeBotFilePointer(robot.getFileName());

                // Run the battle and wait for it to finish before we continue
                robocodeEngine.runBattle(battleSpecification);
                robocodeEngine.waitTillBattleOver();
            }

            // The generation finished their fighting so read the results
            System.out.println("Generation [" + i + "] -> Scores");
            getGenerationScores();
            logScores();

            // Begin the parent selection process using the scores
            System.out.println("Generation [" + i + "] -> Selecting parents");
            ArrayList<ArrayList<RobotData>> parentPairs = parentSelection();

            // Begin the crossover stage using the parent groups formed
            System.out.println("Generation [" + i + "] -> Creating [" + childrenToCreate + "] children");
            ArrayList<RobotData> children = crossover(parentPairs, lastFileNumber);

            // Increment the last created file number
            lastFileNumber += childrenToCreate;

            // Randomly mutate the children we just created from parents
            System.out.println("Generation [" + i + "] -> Mutating children");
            mutateChildren(children, functionSwitchOutChance, parameterMutationChance, variableScalePercentage);

            // Create some completely random children and add them to the list of children
            System.out.println("Generation [" + i + "] -> Creating [" + childrenToRandom + "] random children");
            children.addAll(createRandomChildren());

            // Kill parents (kill as many parents as children we created to keep the population constant)
            System.out.println("Generation [" + i + "] -> Killing [" + (childrenToCreate + childrenToRandom) + "] parents");
            ArrayList<RobotData> parents = killParents();

            // Merge the children and parent file names ready for the next iteration
            robots.clear();
            robots.addAll(parents);
            robots.addAll(children);

            // Keeping the logging pretty
            System.out.println("");

            if (i == generationCount - 1) {

                // Create a Battle specification with only one round
                battleSpecification = new BattleSpecification(new BattlefieldSpecification(), 1,
                        10, 5, 50, false, allRobots);

                // Set the UI to visible
                robocodeEngine.setVisible(true);

                // Get a list of top robots
                System.out.println("Evaluation complete, top robots are");
                ArrayList<RobotData> bestRobots = getTopRobots(seeTopResultsOf);

                // For the amount of robots we want to see results for
                for(int j = 0; j < seeTopResultsOf; j++){
                    System.out.println("[" + bestRobots.get(j).getFileName() + "] score of [" + bestRobots.get(j).getTotalFitness() + "]");

                    // Point the given JoeBot to its data file
                    setJoeBotFilePointer(bestRobots.get(j).getFileName());

                    // Run the battle and wait for it to finish before we continue
                    robocodeEngine.runBattle(battleSpecification);
                    robocodeEngine.waitTillBattleOver();
                }
            }
        }

        // close the robocode instance
        robocodeEngine.close();
    }

    private String getRandomCommand(){
        Random rand = new Random();
        double functionVal = functionValMin + (functionValMax - functionValMin) * rand.nextDouble();
        int functionIndex = rand.nextInt(functionOptions);
        return functionIndex + "," + functionVal;
    }

    private RobotData getRandomRobot(String fileName){
        Random rand = new Random();

        ArrayList<String> robotCommandQueue = new ArrayList<>();
        ArrayList<ArrayList<String>> robotEventQueues = new ArrayList<>();

        int thisSequenceCount = rand.nextInt(sequenceCountMax - 1) + 1;
        for(int j = 0; j < thisSequenceCount; j++){
            robotCommandQueue.add(getRandomCommand());
        }

        for(int j = 0; j < 4; j++){
            robotEventQueues.add(new ArrayList<>());

            thisSequenceCount = rand.nextInt(eventSequenceMax - 1) + 1;
            for(int k = 0; k < thisSequenceCount; k++){
                robotEventQueues.get(j).add(getRandomCommand());
            }
        }

        writeQueueAndEventCommands(robotCommandQueue, robotEventQueues, fileName);

        return new RobotData(fileName);
    }

    private void writeQueueAndEventCommands(ArrayList<String> queueCommands, ArrayList<ArrayList<String>> eventCommands, String fileName){

        try {

            // Create the file
            PrintWriter writer = new PrintWriter("../EvoSim/robots/joebot/Joebot.data/" + fileName, "UTF-8");

            // Write the command queue
            for (String queueCommand : queueCommands) {
                writer.println(queueCommand);
            }

            // A space so that the robot separate the queue commands from the event commands
            writer.println("");

            // Write the event command blocks
            for(int i = 0; i < 4; i++){
                for (String eventCommand : eventCommands.get(i)) {
                    writer.println(eventCommand);
                }
                writer.println("");
            }

            writer.close();

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void generateInitialPopulation(){
        for(int i = 0; i < populationSize; i++){
            System.out.println("Creating robot [" + i + ".txt]");
            robots.add(getRandomRobot(i + ".txt"));
        }

        lastFileNumber = populationSize;
    }

    private String getRobotLoadString(int totalBots, int joeBots){

        Random rand = new Random();
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

    private void setJoeBotFilePointer(String fileLocation){
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

    private void createJoeBotLog(){
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

    private void getGenerationScores(){

        int index = 0;
        try {
            // open the scores file
            BufferedReader reader = new BufferedReader(new FileReader("../EvoSim/robots/joebot/Joebot.data/scores.txt"));

            // read lines until we see a blank line
            String line;

            // the first line isn't a result so just ignore it
            reader.readLine();

            // while we haven't reached the end of the file
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(" ");
                robots.get(index).setRobocodeScore(Integer.parseInt(lineSplit[1]));
                robots.get(index).setRamDamageScore(Integer.parseInt(lineSplit[2]));
                robots.get(index).setBulletDamageScore(Integer.parseInt(lineSplit[3]));
                robots.get(index).setTimesFirst(Integer.parseInt(lineSplit[4]));
                robots.get(index).setTimesSecond(Integer.parseInt(lineSplit[5]));
                robots.get(index).setTimesThird(Integer.parseInt(lineSplit[6]));

                // use the multipliers to calculate the score of the robot
                robots.get(index).calculateTotalFitness(ramDamageMultiplier, bulletDamageMultiplier, timesFirstMultiplier,
                        timesSecondMultiplier, timesThirdMultiplier);

                index++;
            }

            // we are done with the file so close it
            reader.close();
        }
        catch (IOException | NumberFormatException e){
            e.printStackTrace();
        }
    }

    private void logScores(){

        int lowest = robots.get(0).getTotalFitness();
        int highest = robots.get(0).getTotalFitness();
        double average = 0;

        for(int i = 0; i < robots.size(); i++){
            if(robots.get(i).getTotalFitness() < lowest){
                lowest = robots.get(i).getTotalFitness();
            }
            if(robots.get(i).getTotalFitness() > highest){
                highest = robots.get(i).getTotalFitness();
            }
            average += robots.get(i).getTotalFitness();
        }

        average /= (double)(robots.size());

        System.out.println("\tFitness Low     [" + lowest + "]");
        System.out.println("\tFitness High    [" + highest + "]");
        System.out.println("\tFitness Average [" + average + "]");

        writeDataPoint("../EvoSim/robots/joebot/Joebot.data/logger.txt", highest, lowest, average);
    }

    public void writeDataPoint(String fileName, double high, double low, double average){
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

    private ArrayList<Double> getPercentageShares(){

        // Add up the total score so we can get a value to work out the percentage
        int totalScore = 0;

        for(RobotData robot : robots){
            totalScore += robot.getTotalFitness();
        }

        // Create an arrayList of all of the percentages, this will line up by index with the scores arrayList
        ArrayList<Double> percentages = new ArrayList<>();

        // Avoid division by zero because who needs it
        if(totalScore == 0){

            // For all robots
            for(RobotData robot : robots){

                // Add to the percentage list
                percentages.add((double) 1 / (double) robots.size() * 100.0);
            }
        }
        else {

            // For all robots
            for(RobotData robot : robots){

                // Add to the percentage list
                percentages.add((double) robot.getTotalFitness() / (double) totalScore * 100.0);

            }
        }

        return percentages;
    }

    private ArrayList<ArrayList<RobotData>> parentSelection(){
        ArrayList<ArrayList<RobotData>> parentArray = new ArrayList<>();

        ArrayList<Double> percentages = getPercentageShares();

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
                        parentArray.get(parentArray.size() - 1).add(robots.get(k));
                    }

                    // increment the current percentage
                    currentPercentage += percentages.get(k);
                }
            }
        }

        // return the big list of parents to the call
        return parentArray;
    }

    private ArrayList<RobotData> crossover(ArrayList<ArrayList<RobotData>> parents, int lastFileNumber){

        ArrayList<RobotData> childrenRobots = new ArrayList<>();

        // for each set of parents
        for (ArrayList<RobotData> parent : parents) {

            // create a place for the commands of each file to be stored
            ArrayList<ArrayList<String>> queueCommands = new ArrayList<>();
            ArrayList<ArrayList<ArrayList<String>>> eventCommands = new ArrayList<>();

            for (int j = 0; j < 2; j++) {
                queueCommands.add(new ArrayList<>());
                eventCommands.add(new ArrayList<>());

                readQueueAndEventCommands(queueCommands.get(j), eventCommands.get(j), parent.get(j).getFileName());
            }

            // Crossover the queue commands
            ArrayList<String> childQueueCommands = new ArrayList<>();
            crossoverCommandBlock(queueCommands.get(0), queueCommands.get(1), childQueueCommands, 4, 8);

            // Crossover the event commands
            ArrayList<ArrayList<String>> childEventCommands = new ArrayList<>();

            // for each four events
            for(int j = 0; j < 4; j++){

                // crossover the command blocks of the parents events
                childEventCommands.add(new ArrayList<>());
                crossoverCommandBlock(eventCommands.get(0).get(j), eventCommands.get(1).get(j), childEventCommands.get(j), 2, 4);
            }

            // Create a file for this child incrementing the last known file name
            childrenRobots.add(new RobotData((++lastFileNumber) + ".txt"));
            writeQueueAndEventCommands(childQueueCommands, childEventCommands,lastFileNumber + ".txt");
        }

        // Increment the last name so that we can create some random children with their own names later
        lastFileNumber += childrenToCreate + 1;
        return childrenRobots;
    }

    private void readQueueAndEventCommands(ArrayList<String> queueCommands, ArrayList<ArrayList<String>> eventCommands, String fileName){
        try {
            // Open the file of the given parent
            BufferedReader reader = new BufferedReader(new FileReader("../EvoSim/robots/joebot/Joebot.data/" + fileName));

            // Read lines until we see a blank line
            String line;
            while (!(line = reader.readLine()).equals("")) {
                queueCommands.add(line);
            }

            for (int i = 0; i < 4; i++) {
                eventCommands.add(new ArrayList<>());

                while (!(line = reader.readLine()).equals("")) {
                    eventCommands.get(i).add(line);
                }
            }

            reader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void crossoverCommandBlock(ArrayList<String> block1, ArrayList<String> block2, ArrayList<String> destination, int stepMin, int stepMax){

        // create a random object too keep crossover interesting
        Random rand = new Random();

        // Take the command block size from the first block
        int commandQueueSize = block1.size();

        // create a step size for the crossover of queue commands to swap on it must be at least 3 and could be maximum 6
        int randomStep = rand.nextInt(stepMax - stepMin) + stepMin;

        // start by reading from the left file
        boolean readFromFileOne = true;

        // for all of the command attempts we need to make step by the alternating factor
        for (int j = 0; j < commandQueueSize; j += randomStep) {

            // for the random step that we have skipped by
            for (int k = 0; k < randomStep; k++) {

                // check we aren't above the the command queue size that we wanted and that the given file has enough commands to take from
                if (j + k < commandQueueSize && j + k < (readFromFileOne ? block1.size() : block2.size())) {

                    // store the command index from the given file
                    destination.add(readFromFileOne ? block1.get(j + k) : block2.get(j + k));
                }
            }

            // swap which file to take commands from every <randomStep> commands
            readFromFileOne = !readFromFileOne;
        }
    }

    private void mutateCommandBlock(ArrayList<String> commandBlock, int functionSwitchOutChance, int parameterMutationChance, int variableScaleMaxPercentage){

        Random rand = new Random();

        // Increase or decrease the parameter value of the commands randomly
        for(int k = 0; k < commandBlock.size(); k++){
            if(rand.nextInt(100) < parameterMutationChance){
                String[] splitCommand;
                splitCommand = commandBlock.get(k).split(",");
                double value = Double.parseDouble(splitCommand[1]);
                double randomMutateFactor = (rand.nextInt(variableScaleMaxPercentage * 2) - variableScaleMaxPercentage) / 100.0;
                double result = value + (value * randomMutateFactor);
                commandBlock.set(k, splitCommand[0] + "," + result);
            }
        }

        // remove a command from the queue command list make sure we have at least one command though
        if(commandBlock.size() > 1 && rand.nextInt(100) < functionSwitchOutChance){

            // pick a random command to remove
            commandBlock.remove(rand.nextInt(commandBlock.size()));
        }

        // remove a command from the queue command list
        if(rand.nextInt(100) < functionSwitchOutChance){

            // pick a random command to remove
            commandBlock.add(rand.nextInt(commandBlock.size()), getRandomCommand());
        }
    }

    private void mutateChildren(ArrayList<RobotData> children, int functionSwitchOutChance, int parameterMutationChance, int variableScaleMaxPercentage){

        for (RobotData child : children) {

            // read the command block data from the file
            ArrayList<String> queueCommands = new ArrayList<>();
            ArrayList<ArrayList<String>> eventCommands = new ArrayList<>();
            readQueueAndEventCommands(queueCommands, eventCommands, child.getFileName());

            // mutate the queue command block
            mutateCommandBlock(queueCommands, functionSwitchOutChance, parameterMutationChance, variableScaleMaxPercentage);

            // mutate all event command blocks
            for (ArrayList<String> eventCommand : eventCommands) {
                mutateCommandBlock(eventCommand, functionSwitchOutChance, parameterMutationChance, variableScaleMaxPercentage);
            }

            // write the mutated command blocks back to the file
            writeQueueAndEventCommands(queueCommands, eventCommands, child.getFileName());
        }
    }

    private ArrayList<RobotData> createRandomChildren(){
        ArrayList<RobotData> randomChildren = new ArrayList<>();

        for(int i = 0; i < childrenToRandom; i++){
            randomChildren.add(getRandomRobot(lastFileNumber + i + ".txt"));
        }

        lastFileNumber += childrenToRandom;

        return randomChildren;
    }

    private ArrayList<RobotData> killParents(){
        ArrayList<RobotData> survivors = new ArrayList<>();
        ArrayList<Double> percentages = getPercentageShares();
        int currentTotalPercentageShare = 100;
        Random rand = new Random();

        for(int i = 0; i <  populationSize - childrenToCreate - childrenToRandom; i++){

            // Pick a value between 0 and the percentage share and just returns 0 if none of the robots did well enough to get a percentage share
            int randomSelection = currentTotalPercentageShare < 1 ? 0 : rand.nextInt(currentTotalPercentageShare);

            // Cycle through the percentages and checking to see if the random score is within that percentage
            float currentPercentage = 0;
            for(int k = 0; k < percentages.size(); k++){

                // if the value is within the percentage segment
                if(randomSelection >= currentPercentage && randomSelection <= currentPercentage + percentages.get(k)){

                    // set the parent to the key of the score that was picked
                    survivors.add(robots.get(k));

                    // change the random number maximum
                    currentTotalPercentageShare -= percentages.get(k);

                    // remove the robot we just saved from the list
                    robots.remove(k);

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
            for (RobotData robot : robots) {

                // delete the files that are still in the scores list
                File file = new File("../EvoSim/robots/joebot/Joebot.data/" + robot.getFileName());
                if (!file.delete()) {
                    System.out.println("Deleting [" + robot.getFileName() + "] failed");
                }
            }
        }

        return survivors;
    }

    private void clearFile(String fileName){
        try {

            // Create the file
            FileWriter writer = new FileWriter(fileName);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void drawChart(){

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

        SwingWrapper<XYChart> s = new SwingWrapper<>(chart);
        s.displayChart();
    }

    private ArrayList<ArrayList<Double>> readData(String fileName){

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

    private ArrayList<RobotData> getTopRobots(int numberToGet){
        robots.sort(Comparator.comparingInt(RobotData::getTotalFitness));
        ArrayList<RobotData> topRobots = new ArrayList<>();

        for(int i = 0; i < numberToGet; i++){
            topRobots.add(robots.get(robots.size() - i - 1));
        }

        return topRobots;
    }
}
