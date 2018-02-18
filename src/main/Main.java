package main;
import javafx.util.Pair;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

    public static String[] createPopulationFiles(int populationSize){
        int sequenceCountMax = 20;
        int functionOptions = 22;
        double functionValMin = 0.0;
        double functionValMax = 1000.0;
        Random rand = new Random();
        String[] fileNames = new String[populationSize];

        for(int i = 0; i < populationSize; i++){

            fileNames[i] = (i + 1) + ".txt";

            PrintWriter writer = null;
            try {
                writer = new PrintWriter("../EvoSim/robots/joebot/Joebot.data/" + (i + 1) + ".txt", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            assert writer != null;

            int thisSequenceCount = rand.nextInt(sequenceCountMax - 1) + 1;
            for(int j = 0; j < thisSequenceCount; j++){
                double functionVal = functionValMin + (functionValMax - functionValMin) * rand.nextDouble();
                int functionIndex = rand.nextInt(functionOptions);
                writer.println(functionIndex + "," + functionVal);
            }

            writer.println();

            for(int j = 0; j < 4; j++){
                double functionVal = functionValMin + (functionValMax - functionValMin) * rand.nextDouble();
                int functionIndex = rand.nextInt(functionOptions);
                writer.println(functionIndex + "," + functionVal);
            }

            writer.close();
        }

        return fileNames;
    }

    public static void setJoeBotFilePointer(String fileLocation){
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

    public static void createJoeBotLog(){
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

    public static String getRobotLoadString(int totalBots, int joeBots){

        Random rand = new Random();

        ArrayList<String> botNames = new ArrayList<String>(){{
            add("sample.Corners");
            add("sample.Crazy");
            add("sample.Fire");
            add("sample.RamFire");
            add("sample.SittingDuck");
            add("sample.SpinBot");
            add("sample.Tracker");
            add("sample.VelociRobot");
            add("sample.Walls");
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

    private static ArrayList<Pair<String, String>> parentSelection(ArrayList<Pair<String, Integer>> scores, int childrenToCreate){
        ArrayList<Pair<String, String>> parentArray = new ArrayList<>();

        // Add up the total score so we can get a value to work out the percentage
        int totalScore = 0;
        for(int i = 0; i < scores.size(); i++){
            totalScore += scores.get(i).getValue();
        }

        // Create an arrayList of all of the percentages, this will line up by index with the scores arrayList
        ArrayList<Float> percentages = new ArrayList<>();
        for(int i = 0; i < scores.size(); i++){

            // Add to the percentage list
            percentages.add((float)scores.get(i).getValue() / (float)totalScore * 100.0f);

            // Display the percentage share of each file score
            System.out.println("[" + scores.get(i).getKey() + "] scored [" + scores.get(i).getValue() + "] -> [" +
                    + (float)scores.get(i).getValue() / (float)totalScore * 100.0f + "%]");
        }

        // Create a random object for picking the parents
        Random rand = new Random();

        for(int i = 0; i < childrenToCreate; i++){

            // Define a space to store the two parents
            String[] parents = new String[2];

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
                        parents[j] = scores.get(k).getKey();
                    }

                    // increment the current percentage
                    currentPercentage += percentages.get(k);
                }
            }

            // store the parents that we found
            parentArray.add(new Pair<>(parents[0], parents[1]));
        }

        // return the big list of parents to the call
        return parentArray;
    }

    private static void crossover(ArrayList<Pair<String, String>> parents){
        for(int i = 0; i < parents.size(); i++){
            System.out.println(parents.get(i).getKey() + " and " + parents.get(i).getValue());
        }
    }

    public static void main(String[] args){

        // Create an instance of RobocodeEngine to run the battles
        RobocodeEngine.setLogMessagesEnabled(true);
        RobocodeEngine.setLogErrorsEnabled(true);
        RobocodeEngine robocodeEngine = new RobocodeEngine();

        // Create a population of JoeBots
        int population = 5;
        String[] robotFiles = createPopulationFiles(population);

        // Get a string of random robots to challenge this round, make sure there is one JoeBot
        String robotoString = getRobotLoadString(10, 1);

        // Using the string we made earlier, get a collection of robot specifications
        RobotSpecification[] allRobots = robocodeEngine.getLocalRepository(robotoString);

        // Create a Battle specification using these robots
        BattleSpecification battleSpecification = new BattleSpecification(new BattlefieldSpecification(), 5,
                10, 5, 50, false, allRobots);

        // Set if we want to see the UI or not
        //robocodeEngine.setVisible(true);

        // create (or overwrite) the score logging file for this generation
        createJoeBotLog();

        // For the entire JoeBot population size
        for(int i = 0; i < population; i++){

            // Point the given JoeBot to its data file
            setJoeBotFilePointer(robotFiles[i]);
            System.out.println("Testing [" + robotFiles[i] + "]");

            // Run the battle and wait for it to finish before we continue
            robocodeEngine.runBattle(battleSpecification);
            robocodeEngine.waitTillBattleOver();
        }

        // The generation finished their fighting so read the results
        ArrayList<Pair<String, Integer>> scores = getGenerationScores();

        // Begin the parent selection process using the scores
        ArrayList<Pair<String, String>> parents = parentSelection(scores, 20);

        // Begin the crossover stage using the parent groups formed
        crossover(parents);

        // close the robocode instance
        robocodeEngine.close();
    }
}
