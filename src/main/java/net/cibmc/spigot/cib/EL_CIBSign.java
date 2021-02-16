package net.cibmc.spigot.cib;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class EL_CIBSign implements Listener{
	CIBPlugin plugin;
	
	public EL_CIBSign(CIBPlugin plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event){
		if(!event.getLine(0).equalsIgnoreCase(CIBCommon.CIB_SIGN_STRING)) return;
		if(!event.getPlayer().hasPermission("cib.stationbuilder")){
			event.getBlock().breakNaturally();
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] Wait a minute! But you don't have permission to build a station!");
			return;
		}//End if
		event.setLine(0, event.getLine(0).toUpperCase());
		
		Material itUnderSign = event.getPlayer().getWorld().getBlockAt(event.getBlock().getLocation().add(0, -1, 0)).getState().getBlock().getType();
		if(!itUnderSign.equals(Material.DETECTOR_RAIL)){
			event.getBlock().breakNaturally();
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] Hey Builder! There is no detector rail under the sign!");
			return;
		}
		
		String line1 = event.getLine(1);
		if(!(line1.equalsIgnoreCase(CIBCommon.STR_NORTH) || line1.equalsIgnoreCase(CIBCommon.STR_SOUTH) || line1.equalsIgnoreCase(CIBCommon.STR_EAST) || line1.equalsIgnoreCase(CIBCommon.STR_WEST) || line1.equalsIgnoreCase(CIBCommon.STR_TEREMINAL))){
			event.getBlock().breakNaturally();
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] Hey Builder! You specified wrong direction!");
			return;
		}//End if
		event.setLine(1, line1.toUpperCase());
		event.setLine(2, "------------");
		if(line1.equalsIgnoreCase(CIBCommon.STR_TEREMINAL)){
			event.setLine(3, ChatColor.DARK_RED + "降車専用");
		}else{
			event.setLine(3, ChatColor.GREEN + "乗車可能");
		}
		event.getPlayer().sendMessage(ChatColor.GREEN + "[CIB] You placed CIB Minecart Station Sign!");
	}//End public void onSignChange(SignChangeEvent event)
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		Block formerBlock = event.getBlock();
		if(!CIBCommon.isCIBSign(formerBlock)) return;
		if(!event.getPlayer().hasPermission("cib.stationbuilder")){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] " + ChatColor.WHITE + "You don't have permission to break a station!");
		}//End if
	}//End public void onBlockBreak(BlockBreakEvent event)
	
	@EventHandler
	public void onBlockPhysicsEvent(BlockPhysicsEvent event){
		Block objBlock = event.getBlock();
		if(CIBCommon.isCIBSign(objBlock)){
			event.setCancelled(true);
		}//End if
	}//End public void onBlockPhysicsEvent(BlockPhysicsEvent event)

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event){
		if(!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;
		
		Material mat = event.getClickedBlock().getState().getBlock().getType();
		switch(mat) {
			case ACACIA_WALL_SIGN:
			case BIRCH_WALL_SIGN:
			case DARK_OAK_WALL_SIGN:
			case JUNGLE_WALL_SIGN:
			case OAK_WALL_SIGN:
			case SPRUCE_WALL_SIGN:
			case ACACIA_SIGN:
			case BIRCH_SIGN:
			case DARK_OAK_SIGN:
			case JUNGLE_SIGN:
			case OAK_SIGN:
			case SPRUCE_SIGN:
				break;
			default:
				return;
		}
		
		Sign s = (Sign)event.getClickedBlock().getState();
		if(!s.getLine(0).equals(CIBCommon.CIB_SIGN_STRING)) return;
		if(event.getPlayer().getVehicle() != null) return;
		
		Block mayBeRailBlock = event.getClickedBlock().getLocation().getBlock().getLocation().add(0, -2, 0).getBlock();
		if(mayBeRailBlock.isBlockPowered()){
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] " + ChatColor.WHITE + "ほかのトロッコが停車中です。しばらくお待ちください。");
			return;
		}//End if
		
		if(s.getLine(1).equals(CIBCommon.STR_TEREMINAL)){
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] " + ChatColor.WHITE + "こちらは降車専用です。ご乗車にはなれません。");
			return;
		}//End if
		
		Location locRail = event.getClickedBlock().getLocation().add(0.5,-1,0.5);
		Minecart entMC = (Minecart)event.getPlayer().getWorld().spawnEntity(locRail, EntityType.MINECART);
		entMC.setMetadata(CIBCommon.CIB_METADATA_TYPE, new FixedMetadataValue(this.plugin, true));
		entMC.setSilent(true);
		Player pl = event.getPlayer();
		entMC.addPassenger(pl);
		MinecartInfo mci = new MinecartInfo(entMC, event.getClickedBlock().getLocation());
		mci.setSpeed(MinecartSpeed.SPEED_RIDING);
		synchronized(plugin.minecartBox){
			plugin.minecartBox.put(pl.getUniqueId().toString(), mci);
		}//End sync
	}//End public void onPlayerInteract(PlayerInteractEvent event)
	
	@EventHandler
	public void onBlockRedstoneEvent(BlockRedstoneEvent event){
		//plugin.getLogger().info("RS Event!");
		if(event.getNewCurrent() == 0) return;
		Block objBlock = event.getBlock();
		Block mayBeCIBSign = objBlock.getLocation().add(0, 1, 0).getBlock();
		//plugin.getLogger().info("MaybeCIBS_Block is " + mayBeCIBSign.getType().toString());
		if(!CIBCommon.isCIBSign(mayBeCIBSign)) return;
		//plugin.getLogger().info("It's CIB Sign!");
		Collection<Entity> ents = objBlock.getWorld().getNearbyEntities(objBlock.getLocation(), 5, 5, 5);
		if(ents.isEmpty()) return;
		//plugin.getLogger().info("There is Entities!");
		Iterator<Entity> entsIt = ents.iterator();
		while(entsIt.hasNext()){
			Entity ent = entsIt.next();
			if(ent.getType() == EntityType.MINECART){
				//plugin.getLogger().info("It's Minecart!");
				Minecart mc = (Minecart)ent;
				
				synchronized(plugin.minecartBox){
					Iterator<Entry<String, MinecartInfo>> it = plugin.minecartBox.entrySet().iterator();
					while(it.hasNext()){
						Entry<String, MinecartInfo> entry = it.next();
						MinecartInfo mci = entry.getValue();
						if(mci.minecartEnt.getEntityId() == mc.getEntityId()){
							if(!mci.startLoc.getBlock().getLocation().equals(mayBeCIBSign.getLocation())){
								mci.startLoc = mayBeCIBSign.getLocation();
								mci.setSpeed(MinecartSpeed.SPEED_ARRIVING);
							}//End if
						}//End if
					}//Next
				}//End synchronized(plugin.minecartBox)
			}//End if
		}//Next entsIt
	}//End public void onBlockRedstoneEvent(BlockRedstoneEvent event)
	
	@EventHandler
	public void onVehicleExit(VehicleExitEvent event){
		if(!(event.getVehicle() instanceof Minecart)) return;
		List<Entity> entList = event.getVehicle().getPassengers();
		for (int i = 0; i < entList.size(); i ++){
			Entity ent = entList.get(i);
			if(!(ent instanceof Player)) return;
			Player pl = (Player)ent;
			synchronized(plugin.minecartBox){
				if(plugin.minecartBox.containsKey(pl.getUniqueId().toString())){
					MinecartInfo mci = plugin.minecartBox.get(pl.getUniqueId().toString());
					mci.setVisibleBossBarForPassenger(false);
					if(mci.getSpeed().equals(MinecartSpeed.SPEED_EXITMC)){
						//Nothing to do.
					}else{
						mci.setSpeed(MinecartSpeed.SPEED_EXITMC);
						event.setCancelled(true);
					}//End if
				}//End if
			}//End sync
		}
	}//End public void onVehicleExit(VehicleExitEvent event)
}//End public class EL_CIBSign implements Listener