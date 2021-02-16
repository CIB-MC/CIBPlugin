package net.cibmc.spigot.cib;

public enum MinecartSpeed {
	SPEED_ARRIVING(0),
	SPEED_ARRIVED(0),
	SPEED_RIDING(0),
	SPEED_WAIT(0),
	SPEED_NEAR_CURVE(0.4),
	SPEED_NEAR_SLOPE(0.22),
	SPEED_START(0.1),
	SPEED_NA(0.12),
	SPEED_NB(0.15),
	SPEED_NC(0.20),
	SPEED_ND(0.25),
	SPEED_NE(0.30),
	SPEED_NF(0.35),
	SPEED_HG(0.4),
	SPEED_HA(0.75),
	SPEED_HB(1),
	SPEED_HC(1.2),
	SPEED_HD(1.4),
	SPEED_HE(1.6),
	SPEED_HF(1.8),
	SPEED_MAX(1.9),
	SPEED_EXITMC(0),;
	
	private double speed;
	
	private MinecartSpeed(double speed){
		this.speed = speed;
	}//End private constructor
	
	public double getSpeed(){
		return this.speed;
	}//End getter
}//End public enum MinecartSpeed