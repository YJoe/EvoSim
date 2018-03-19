package main;

public class Main {

    public static void main(String[] args){

        // define the properties of the simulation
        int populationSize = 5;
        int generationCount = 10;
        int childrenToCreate = 3;
        int childrenToRandom = 0;
        int robotsToFight = 1;
        int roundsPerFight = 5;
        int sequenceCountMax = 20;
        int eventSequenceMax = 10;
        int ramDamageMultiplier = 0;
        int bulletDamageMultiplier = 0;
        int timesFirstMultiplier = 0;
        int timesSecondMultiplier = 0;
        int timesThirdMultiplier = 0;
        int functionSwitchOutChance = 30;
        int parameterMutationChance = 30;
        int variableScalePercentage = 30;
        double functionValMin = 0.1;
        double functionValMax = 20.0;

        GeneticSimulation geneticSimulation = new GeneticSimulation(populationSize, generationCount, childrenToCreate,
                                                                    childrenToRandom, robotsToFight, roundsPerFight,
                                                                    functionValMin, functionValMax, sequenceCountMax,
                                                                    eventSequenceMax, ramDamageMultiplier, bulletDamageMultiplier,
                                                                    timesFirstMultiplier, timesSecondMultiplier, timesThirdMultiplier,
                                                                    functionSwitchOutChance, parameterMutationChance, variableScalePercentage);

        geneticSimulation.run();

        geneticSimulation.drawChart();
    }
}