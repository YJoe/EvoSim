package main;
import net.sf.robocode.battle.events.BattleEventDispatcher;
import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.*;

public class Main {
    public static void main(String[] args){
        RobocodeEngine.setLogMessagesEnabled(true);
        RobocodeEngine.setLogErrorsEnabled(true);

        RobocodeEngine robocodeEngine = new RobocodeEngine();
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < 5; i++){
            str.append("joebot.Joebot*,");
        }

        RobotSpecification[] allRobots = robocodeEngine.getLocalRepository(str.toString());
        BattleSpecification battleSpecification = new BattleSpecification(new BattlefieldSpecification(), 3,
                1000, 5, 50, false, allRobots);

        //robocodeEngine.setVisible(true);
        robocodeEngine.addBattleListener(new BattleEventDispatcher() {
            @Override
            public void onBattleCompleted(BattleCompletedEvent battleCompletedEvent) {
                BattleResults[] battleResults = battleCompletedEvent.getSortedResults();
                for(BattleResults b : battleResults) {
                    System.out.println("SCORE [" + b.getScore() + "] NAME [" + b.getTeamLeaderName() + "]");
                }
            }
        });
        robocodeEngine.runBattle(battleSpecification);
        robocodeEngine.waitTillBattleOver();
        robocodeEngine.close();

        throw new Error("EVERYTHING IS FINE");
    }
}
