package joebot;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import robocode.*;

public class Joebot extends AdvancedRobot {

	ArrayList<Integer> sequenceFunctions = new ArrayList<Integer>();
	ArrayList<Double> sequenceParams = new ArrayList<Double>();
	int[] eventFunctions = new int[4];
	double[] eventParams = new double[4];
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

            // There must be 4 more commands to read
            for(int i = 0; i < 4; i++){
                line = reader.readLine();
                String[] lineSplit = line.split(",");
                eventFunctions[i] = Integer.parseInt(lineSplit[0]);
                eventParams[i] = Double.parseDouble(lineSplit[1]);
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
		evaluate(eventFunctions[0], eventParams[0]);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		evaluate(eventFunctions[1], eventParams[1]);
	}
	
	public void onHitWall(HitWallEvent e) {
		evaluate(eventFunctions[2], eventParams[2]);
	}
	
	public void onHitRobot(HitRobotEvent e){
		evaluate(eventFunctions[3], eventParams[3]);
	}

    public void onBattleEnded(BattleEndedEvent ev){
		try{
	    	RobocodeFileWriter fileWriter = new RobocodeFileWriter(getDataFile("scores.txt").getAbsolutePath(), true);
	        BattleResults battleResults = ev.getResults();
			fileWriter.write(filePointerPointsTo + " " + battleResults.getScore() + "\n");
			fileWriter.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
