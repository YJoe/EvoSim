package main;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args){

        // define the properties of the simulation
        int populationSize = 100;
        int generationCount = 500;
        int childrenToCreate = 50;
        int childrenToRandom = 0;
        int robotsToFight = 5;
        int roundsPerFight = 3;
        int sequenceCountMax = 20;
        int eventSequenceMax = 10;
        int functionSwitchOutChance = 30;
        int parameterMutationChance = 30;
        int variableScalePercentage = 30;
        int seeTopResultsOf = 5;
        double ramDamageMultiplier = 0;
        double bulletDamageMultiplier = 0;
        double energyMultiplier = 0;
        double timesFirstMultiplier = 0;
        double timesSecondMultiplier = 0;
        double timesThirdMultiplier = 0;
        double functionValMin = 0.1;
        double functionValMax = 20.0;
        String dataLoggingFile = "P100_B50_RA0_G500_R3_RANDOM.txt";

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