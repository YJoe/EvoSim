package main;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args){

        // define the properties of the simulation
        int populationSize = 20;
        int generationCount = 100;
        int childrenToCreate = 9;
        int childrenToRandom = 1;
        int robotsToFight = 1;
        int roundsPerFight = 5;
        int sequenceCountMax = 20;
        int eventSequenceMax = 10;
        int functionSwitchOutChance = 30;
        int parameterMutationChance = 30;
        int variableScalePercentage = 30;
        int seeTopResultsOf = 5;
        double ramDamageMultiplier = 0;
        double bulletDamageMultiplier = 0;
        double energyMultiplier = 100;
        double timesFirstMultiplier = 0;
        double timesSecondMultiplier = 0;
        double timesThirdMultiplier = 0;
        double functionValMin = 0.1;
        double functionValMax = 20.0;
        String dataLoggingFile = "P100_C50_G20_SD1.txt";

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

        GeneticSimulation geneticSimulation = new GeneticSimulation(populationSize, generationCount, childrenToCreate,
                                                                    childrenToRandom, robotsToFight, roundsPerFight,
                                                                    functionValMin, functionValMax, sequenceCountMax,
                                                                    eventSequenceMax, ramDamageMultiplier,
                                                                    bulletDamageMultiplier, energyMultiplier,
                                                                    timesFirstMultiplier, timesSecondMultiplier,
                                                                    timesThirdMultiplier, functionSwitchOutChance,
                                                                    parameterMutationChance,variableScalePercentage,
                                                                    botNames, seeTopResultsOf, dataLoggingFile);

        geneticSimulation.run();

        geneticSimulation.drawChart(dataLoggingFile);
    }
}