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

        // create a controller and get all of the robots available
        RobocodeEngine robocodeEngine = new RobocodeEngine();
        RobotSpecification[] allRobots = robocodeEngine.getLocalRepository();

        BattleSpecification battleSpecification = new BattleSpecification(new BattlefieldSpecification(), 1,
                10000, 5, 50, false, allRobots);

        robocodeEngine.addBattleListener(new BattleEventDispatcher(){
            @Override
            public void onBattleCompleted(BattleCompletedEvent battleCompletedEvent) {
                BattleResults[] battleResults = battleCompletedEvent.getSortedResults();
                for(BattleResults b : battleResults){
                    System.out.println(b.getRank() + " " + b.getTeamLeaderName() + " " + b.getScore());
                }
            }
        });
        robocodeEngine.runBattle(battleSpecification);
        robocodeEngine.waitTillBattleOver();
        robocodeEngine.close();
    }
}
