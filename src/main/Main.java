package main;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args){

        // define the properties of the simulation
        int populationSize = 20;
        int generationCount = 10;
        int childrenToCreate = 10;
        int childrenToRandom = 0;
        int robotsToFight = 5;
        int roundsPerFight = 1;
        int sequenceCountMax = 20;
        int eventSequenceMax = 10;
        int ramDamageMultiplier = 0;
        int bulletDamageMultiplier = 1;
        int timesFirstMultiplier = 0;
        int timesSecondMultiplier = 0;
        int timesThirdMultiplier = 0;
        int functionSwitchOutChance = 30;
        int parameterMutationChance = 30;
        int variableScalePercentage = 30;
        int seeTopResultsOf = 5;
        double functionValMin = 0.1;
        double functionValMax = 20.0;

        ArrayList<String> botNames = new ArrayList<String>(){{
//            add("sample.Corners");
//            add("sample.Crazy");
//            add("sample.Fire");
//            add("sample.RamFire");
//            add("sample.SittingDuck");
            add("sample.SpinBot");
//            add("sample.Tracker");
//            add("sample.VelociRobot");
//            add("sample.Walls");
        }};

        GeneticSimulation geneticSimulation = new GeneticSimulation(populationSize, generationCount, childrenToCreate,
                                                                    childrenToRandom, robotsToFight, roundsPerFight,
                                                                    functionValMin, functionValMax, sequenceCountMax,
                                                                    eventSequenceMax, ramDamageMultiplier,
                                                                    bulletDamageMultiplier, timesFirstMultiplier,
                                                                    timesSecondMultiplier, timesThirdMultiplier,
                                                                    functionSwitchOutChance, parameterMutationChance,
                                                                    variableScalePercentage, botNames, seeTopResultsOf);

        geneticSimulation.run();

        geneticSimulation.drawChart();
    }
}