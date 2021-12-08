package net.cibmc.spigot.cib;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class SyncAMC extends Thread {//Automatic Minecart Control
	private final static double MULTIPLIER = 10D;
	
	private CIBPlugin plugin;
	
	public SyncAMC(CIBPlugin plugin){
		this.plugin = plugin;
	}//End constructor
	
	public void run(){
		synchronized(plugin.minecartBox){
			Iterator<Entry<String, MinecartInfo>> it = plugin.minecartBox.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, MinecartInfo> entry = it.next();
				MinecartInfo mci = entry.getValue();
				Minecart mc = mci.minecartEnt;

				Entity ent = null;
				List<Entity> entList = mc.getPassengers();
				listSearch: for (int i = 0; i < entList.size(); i++) {
					if ((ent = entList.get(i)) instanceof Player) {
						break listSearch;
					}
				}
				Location loc = mc.getLocation();
				
				RailsInfo ri = RailsInfo.getRailsInfoAroundLocation(loc);
				
				if(ent == null){
					mci.setVisibleBossBarForPassenger(false);
					mc.remove();
					plugin.minecartBox.remove(entry.getKey());
				}else if((!(mci.getSpeed().equals(MinecartSpeed.SPEED_WAIT))) && (!(mci.getSpeed().equals(MinecartSpeed.SPEED_RIDING))) && (!(mci.getSpeed().equals(MinecartSpeed.SPEED_ARRIVING))) && (!(mci.getSpeed().equals(MinecartSpeed.SPEED_ARRIVED))) && (!(mci.getSpeed().equals(MinecartSpeed.SPEED_EXITMC))) && (!(ri.isAtAccelerateableLoc()))){
					mci.setSpeed(MinecartSpeed.SPEED_NEAR_SLOPE);
				}else{
					switch(mci.getSpeed()){
					case SPEED_EXITMC:
						mc.setSilent(true);
						mc.eject();
						if(ent instanceof Player){
							Player pl = (Player)ent;
							Location tmpLoc = null;
							if((tmpLoc = CIBCommon.getSafetyTpLoc(pl.getLocation(), this.plugin)) != null){
								tmpLoc.setPitch(pl.getLocation().getPitch());
								tmpLoc.setYaw(pl.getLocation().getYaw());
								pl.teleport(tmpLoc);
							}//End if
						}//End if
						mc.remove();
						synchronized(plugin.musicBox){
							plugin.musicBox.remove(mci);
						}//End sync
						synchronized(plugin.minecartBox){
							plugin.minecartBox.remove(entry.getKey());
						}//End sync
						break;
					case SPEED_ARRIVING:
						mc.setSilent(true);
						synchronized(plugin.musicBox){
							plugin.musicBox.put(mci, plugin.mbnArrived.shallowCopyFactory());
							
							if(ent instanceof Player){
								Player pl = (Player)ent;
								pl.sendMessage(ChatColor.GREEN + "[CIB] " + ChatColor.WHITE + "目的地に到着しました。");
							}//End if
							mci.setSpeed(MinecartSpeed.SPEED_ARRIVED);
						}//End Sync
						break;
					case SPEED_ARRIVED:
						mc.setSilent(true);
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								mci.setSpeed(MinecartSpeed.SPEED_RIDING);
							}//End if
						}//End Sync
						break;
					case SPEED_RIDING:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, plugin.mbn.shallowCopyFactory());
								
								if(ent instanceof Player){
									Player pl = (Player)ent;
									Sign s = (Sign) mci.startLoc.getBlock().getState();
									if(s.getLine(1).equalsIgnoreCase(CIBCommon.STR_TEREMINAL)){
										pl.sendMessage(ChatColor.GREEN + "[CIB] " + ChatColor.WHITE + "こちらは降車専用です。ご乗車になれません。");
										mc.eject();
										break;
									}else{
										pl.sendMessage(ChatColor.GREEN + "[CIB] " + ChatColor.WHITE + "まもなく発車いたします。");
									}//End if
								}//End if
								mci.setSpeed(MinecartSpeed.SPEED_WAIT);
							}//End if
						}//End Sync
						break;
					case SPEED_WAIT:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								Sign s = (Sign) mci.startLoc.getBlock().getState();
								if(s.getLine(1).equalsIgnoreCase(CIBCommon.STR_NORTH)){
									mc.setVelocity(CIBCommon.VEC_NORTH);
								}else if(s.getLine(1).equalsIgnoreCase(CIBCommon.STR_SOUTH)){
									mc.setVelocity(CIBCommon.VEC_SOUTH);
								}else if(s.getLine(1).equalsIgnoreCase(CIBCommon.STR_EAST)){
									mc.setVelocity(CIBCommon.VEC_EAST);
								}else if(s.getLine(1).equalsIgnoreCase(CIBCommon.STR_WEST)){
									mc.setVelocity(CIBCommon.VEC_WEST);
								}else{
									if(ent instanceof Player){
										Player pl = (Player)ent;
										pl.sendMessage(ChatColor.GREEN + "[CIB] " + ChatColor.WHITE + "こちらはターミナルです。このトロッコは回送となります。");
									}//End if
									mc.eject();
									break;
								}//End if
								mci.setSpeed(MinecartSpeed.SPEED_START);
								mci.setVisibleBossBarForPassenger(true);
							}//End if
						}//End Sync
						break;
					case SPEED_NEAR_CURVE:
					case SPEED_NEAR_SLOPE:
						mci.setSpeed(MinecartSpeed.SPEED_NF);
						break;
					case SPEED_START:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_NA.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_NA);
							}//End if
						}//End Sync
						break;
					case SPEED_NA:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_NB.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_NB);
							}//End if
						}//End Sync
						break;
					case SPEED_NB:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_NC.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_NC);
							}//End if
						}//End Sync
						break;
					case SPEED_NC:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_ND.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_ND);
							}//End if
						}//End Sync
						break;
					case SPEED_ND:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_NE.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_NE);
							}//End if
						}//End Sync
						break;
					case SPEED_NE:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_NF.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_NF);
							}//End if
						}//End Sync
						break;
					case SPEED_NF:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_HG.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_HG);
							}//End if
						}//End Sync
						break;
					case SPEED_HG:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_HA.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_HA);
							}//End if
						}//End Sync
						break;
					case SPEED_HA:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_HB.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_HB);
							}//End if
						}//End Sync
						break;
					case SPEED_HB:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_HC.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_HC);
							}//End if
						}//End Sync
						break;
					case SPEED_HC:
						mci.announceIfNotDone();
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_HD.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_HD);
							}//End if
						}//End Sync
						break;
					case SPEED_HD:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								plugin.musicBox.put(mci, MBNForStarting.SPEED_HF.getMBN());
								mci.setSpeed(MinecartSpeed.SPEED_HF);
							}//End if
						}//End Sync
						break;
					case SPEED_HF:
						synchronized(plugin.musicBox){
							if(!plugin.musicBox.containsKey(mci)){
								mci.setSpeed(MinecartSpeed.SPEED_MAX);
							}//End if
						}//End Sync
						break;
					default:
						//Nothing to do.
						break;
					}//End switch-case
				}//End if//End if
				
				if(!(mci.getSpeed().equals(MinecartSpeed.SPEED_WAIT))){
					List<Entity> li = mc.getNearbyEntities(5, 5, 5);
					Iterator<Entity> itE = li.iterator();
					while(itE.hasNext()){
						Entity entNear = itE.next();
						if(!(entNear instanceof LivingEntity)){
							continue;
						}
						if(entNear instanceof Player){
							continue;
						}
						if(entNear instanceof AbstractHorse){
							continue;
						}
						if(entNear instanceof Villager){
							continue;
						}
						LivingEntity le = (LivingEntity)entNear;
						le.remove();
					}//Next itE
				}//End if
				
				if(mc != null){
					mc.setVelocity(mc.getVelocity().multiply(MULTIPLIER));
					double distance = loc.distance(mci.formerLoc);
					double speed_blocks_1sec = distance / ((double)CIBPlugin.AMC_TICK_INC) * 20.0;
					double speed_kblocks_1hour = speed_blocks_1sec * 3600 / 1000;
					mci.setBossBarKBSpeed(speed_kblocks_1hour);
					mci.formerLoc = loc;
				}//End if
				//System.out.println(mci.getSpeed());
			}//Next it
		}//End sync
	}//End public void run()
}//End public class SyncAMC extends Thread
