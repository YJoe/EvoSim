package joebot;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import robocode.*;

public class Joebot extends AdvancedRobot {

	ArrayList<Integer> sequenceFunctions = new ArrayList<Integer>();
	ArrayList<Double> sequenceParams = new ArrayList<Double>();
	
	ArrayList<ArrayList<Integer>> eventSequenceFunctions = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<Double>> eventSequenceParams = new ArrayList<ArrayList<Double>>();	

	String filePointerPointsTo;

	private void readDataFile(){

        BufferedReader reader = null;
        try {

            // Find out what file this robot should load by looking at filePointer.txt
            reader = new BufferedReader(new FileReader(getDataFile("filePointer.txt")));
            filePointerPointsTo = reader.readLine();

            // If a reader was here in the first place, get rid of it
            if(reader != null){
                reader.close();
            }

            // Open the file that the filePointer file told us to
            reader = new BufferedReader(new FileReader(getDataFile(filePointerPointsTo)));

            // Read lines until we see a blank line
            String line;
            while (!(line = reader.readLine()).equals("")){

                // Split the line we read into an array of <functioncall><value>
                String[] lineSplit = line.split(",");
                for(int i = 0; i < lineSplit.length; i += 2){

                    // Store the line data for later use
                    sequenceFunctions.add(Integer.parseInt(lineSplit[i]));
                    sequenceParams.add(Double.parseDouble(lineSplit[i + 1]));
                }
            }

            // There must be 4 more command blocks to read
            for(int i = 0; i < 4; i++){
				eventSequenceFunctions.add(new ArrayList<Integer>());
				eventSequenceParams.add(new ArrayList<Double>());			

				while (!(line = reader.readLine()).equals("")){

             	   	// Split the line we read into an array of <functioncall><value>
	                String[] lineSplit = line.split(",");
	                for(int j = 0; j < lineSplit.length; j += 2){
	
	                    // Store the line data for later use
	                    eventSequenceFunctions.get(i).add(Integer.parseInt(lineSplit[j]));
	                    eventSequenceParams.get(i).add(Double.parseDouble(lineSplit[j + 1]));
	                }
	            }
            }

            // If a reader was here in the first place, get rid of it
            if(reader != null){
                reader.close();
            }
        }
        catch (IOException e){}
        catch (NumberFormatException e){}
	}

	public void run() {

	    // Get the instructions that this robot should use
	    readDataFile();
		
		// While the robot is alive
		while(true){
		
			// Cycle through the set of tasks we need to do
			for(int i = 0; i < sequenceFunctions.size(); i++){
				
				// Call a given command
				evaluate(sequenceFunctions.get(i), sequenceParams.get(i));
			}

	    	// if any actions were set they need to execute before this function ends the robot may get stuck if not,
            // when this execute was not in place several robots failed to write their data to the file
			execute();
		}
	}
	
	public void evaluate(int functionNumber, double functionValue){
		switch(functionNumber){
			case 0:	ahead(functionValue); break;
			case 1:	back(functionValue); break;
			case 2:	fire(functionValue); break;
			case 3:	turnGunLeft(functionValue); break;
			case 4:	turnGunRight(functionValue); break;
			case 5:	turnRadarLeft(functionValue); break;
			case 6:	turnRadarRight(functionValue); break;
			case 7:	turnLeft(functionValue); break;
			case 8:	turnRight(functionValue); break;
			case 9: setAhead(functionValue); break;
			case 10: setBack(functionValue); break;
			case 11: setFire(functionValue); break;
			case 12: setTurnGunLeft(functionValue); break;
			case 13: setTurnGunRight(functionValue); break;
			case 14: setTurnLeft(functionValue); break;
			case 15: setTurnRight(functionValue); break;
			case 16: setTurnRadarLeft(functionValue); break;
			case 17: setTurnRadarRight(functionValue); break;
			case 18: waitFor(new TurnCompleteCondition(this)); break;
			case 19: waitFor(new GunTurnCompleteCondition(this)); break;
			case 20: waitFor(new MoveCompleteCondition(this)); break;
			case 21: waitFor(new RadarTurnCompleteCondition(this)); break;
            case 22: execute(); break;
			case 23: doNothing(); break;
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		for(int i = 0; i < eventSequenceFunctions.get(0).size(); i++){
			evaluate(eventSequenceFunctions.get(0).get(i), eventSequenceParams.get(0).get(i));
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		for(int i = 0; i < eventSequenceFunctions.get(1).size(); i++){
			evaluate(eventSequenceFunctions.get(1).get(i), eventSequenceParams.get(1).get(i));
		}
	}
	
	public void onHitWall(HitWallEvent e) {
		for(int i = 0; i < eventSequenceFunctions.get(2).size(); i++){
			evaluate(eventSequenceFunctions.get(2).get(i), eventSequenceParams.get(2).get(i));
		}
	}
	
	public void onHitRobot(HitRobotEvent e){
		for(int i = 0; i < eventSequenceFunctions.get(3).size(); i++){
			evaluate(eventSequenceFunctions.get(3).get(i), eventSequenceParams.get(3).get(i));
		}
	}

    public void onBattleEnded(BattleEndedEvent ev){
		try{
	    	RobocodeFileWriter fileWriter = new RobocodeFileWriter(getDataFile("scores.txt").getAbsolutePath(), true);
	        BattleResults battleResults = ev.getResults();
			fileWriter.write(filePointerPointsTo + " " + battleResults.getScore() + " " + 
							battleResults.getRamDamage() + " " + battleResults.getBulletDamage() + " " +
							battleResults.getFirsts() + " " + battleResults.getSeconds() + " " +
							battleResults.getThirds() + " " + getEnergy() + "\n");
			fileWriter.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
