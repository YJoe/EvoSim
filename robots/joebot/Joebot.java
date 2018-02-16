package joebot;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import robocode.*;

public class Joebot extends AdvancedRobot {
	
	public void run() {
		int number;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(getDataFile("data.txt")));
			number = Integer.parseInt(reader.readLine());
			if(reader != null){
				reader.close();
			}		
		} catch (IOException e) {
			number = 0;
		} catch (NumberFormatException e) {
			number = 0;
		}
		
		while(true){
			back(number);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		//fire(1);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		//back(10);
	}
	
	public void onHitWall(HitWallEvent e) {
		//back(20);
	}	
}
