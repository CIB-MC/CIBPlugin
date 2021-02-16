package net.cibmc.spigot.cib;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class MinecartInfo {
	public static final String BOSS_BAR_TITLE_WAIT = ChatColor.GREEN + "[CIB]" + ChatColor.WHITE + " Speed: 00.00 km/h";
	public static final String BOSS_BAR_TITLE_RUNNING_FORMAT = ChatColor.GREEN + "[CIB]" + ChatColor.WHITE + " Speed: %.2f km/h";
	public static final String ANNOUNCE_TEXT_1 = ChatColor.GREEN + "[CIB]" + ChatColor.WHITE + " Thank you for using our minecart system.";
	public static final String ANNOUNCE_TEXT_2 = ChatColor.GREEN + "[CIB]" + ChatColor.WHITE + " Have a nice minecart trip!";
	private static final double MAX_AVAILABLE_SPEED = 108.0;
	
	public Minecart minecartEnt;
	public Location startLoc;
	public Location formerLoc;
	private boolean announced;
	private BossBar bossBar;
	private MinecartSpeed mcSpeed = MinecartSpeed.SPEED_RIDING;
	
	public MinecartInfo(Minecart minecartEnt, Location cibSign){
		this.minecartEnt = minecartEnt;
		this.startLoc = cibSign;
		this.formerLoc = cibSign;
		this.announced = false;
		this.bossBar = Bukkit.getServer().createBossBar(BOSS_BAR_TITLE_WAIT , BarColor.GREEN, BarStyle.SOLID);
	}//End constructor
	
	public MinecartSpeed getSpeed(){
		return this.mcSpeed;
	}//End getter
	
	public void setSpeed(MinecartSpeed mcs){
		this.mcSpeed = mcs;
		minecartEnt.setMaxSpeed(mcs.getSpeed());
	}//End Setter
	
	public void announceIfNotDone(){
		if(this.announced) return;
		List<Entity> entList = this.minecartEnt.getPassengers();
		for (int i = 0; i < entList.size(); i++){
			Entity ent = entList.get(i);
			if(ent != null && ent instanceof Player){
				Player pl = (Player)ent;
				pl.sendMessage(ANNOUNCE_TEXT_1);
				pl.sendMessage(ANNOUNCE_TEXT_2);
				this.announced = true;
			}//End if
		}
	}//End public void announceIfNotDone()
	
	public void setVisibleBossBarForPassenger(boolean visibility){
		if(this.bossBar == null) return;
		List<Entity> entList = this.minecartEnt.getPassengers();
		for (int i = 0; i < entList.size(); i++){
			Entity ent = entList.get(i);
			if(visibility && ent != null && ent instanceof Player){
				this.bossBar.addPlayer((Player)ent);
				this.bossBar.setVisible(true);
			}else{
				this.bossBar.setVisible(false);
				this.bossBar.removeAll();
			}//End if
		}
	}//End public void setVisibleBossBarForPassenger(Boolean visibility)
	
	public void setBossBarKBSpeed(double kb_speed){
		this.bossBar.setTitle(String.format(BOSS_BAR_TITLE_RUNNING_FORMAT, kb_speed));
		if(kb_speed < 0){
			kb_speed = 0;
		}//End if
		double progress = kb_speed / MAX_AVAILABLE_SPEED;
		if(progress > 1.0){
			progress = 1.0;
		}//End if
		if(progress > 0.6){
			this.bossBar.setColor(BarColor.GREEN);
		}else if(progress > 0.2){
			this.bossBar.setColor(BarColor.YELLOW);
		}else{
			this.bossBar.setColor(BarColor.RED);
		}//End if
		this.bossBar.setProgress(progress);
	}//End public void setBossBarProgress(double progress)
}//End public class MinecartInfo
