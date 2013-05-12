package home.poolplayer.robot;

/**
 *Test of RemotPilotControl.  Be sure RCPilot is running on the NXT before
 * starting this program
 * @author roger
 */

public class RemotPilotTester
{  
  
	public static void main(String[] args){
	    RemotePilotControl pilot = new RemotePilotControl();
	    pilot.connect(null, null);

	    pilot.setShootSpeed(100);
	    
	    
	    pilot.shoot(34);
	    
	    pilot.shoot(- 34);
	    
	    
	    pilot.exit();
	}
	public static void main2(String[] args)
    {
    RemotePilotControl pilot = new RemotePilotControl();
    pilot.connect(null, null);
    pilot.setTurnSpeed(90);
    pilot.setMoveSpeed(8);
    for(int i = 0 ; i < 4 ; i++)
    {
      pilot.travel(10);
      System.out.println("TRAV 10 "+pilot.getTravelDistance());
      pilot.rotate(-90);
      System.out.println("ROT -90 " + pilot.getAngle());
    }
      pilot.reset();
      pilot.arc(10, 90);
      System.out.println("arc(10,90) distance: " + pilot.getTravelDistance()
              +" angle "+pilot.getAngle());
      pilot.reset();
      pilot.steer(50,-45);
      pilot.steer(-50,45);
      System.out.println("steer " + pilot.getTravelDistance()
              +" angle "+pilot.getAngle());
      pilot.stop();

      pilot.exit();
   System.exit(0);
    }

}
