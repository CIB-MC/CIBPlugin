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
import org.bukkit.block.BlockState;

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
		}
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
		}
		event.setLine(1, line1.toUpperCase());
		event.setLine(2, "------------");
		if(line1.equalsIgnoreCase(CIBCommon.STR_TEREMINAL)){
			event.setLine(3, ChatColor.DARK_RED + "Terminal");
		}else{
			event.setLine(3, ChatColor.GREEN + "Station");
		}
		event.getPlayer().sendMessage(ChatColor.GREEN + "[CIB] You placed CIB Minecart Station Sign!");
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		Block formerBlock = event.getBlock();
		if(!CIBCommon.isCIBSign(formerBlock)) return;
		if(!event.getPlayer().hasPermission("cib.stationbuilder")){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] " + ChatColor.WHITE + "You don't have permission to break a station!");
		}
	}
	
	@EventHandler
	public void onBlockPhysicsEvent(BlockPhysicsEvent event){
		Block objBlock = event.getBlock();
		if(CIBCommon.isCIBSign(objBlock)){
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event){
		if(!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;

		BlockState bs = event.getClickedBlock().getState();
		if (!(bs instanceof Sign)) return;

		Sign s = (Sign)bs;
		if(!s.getLine(0).equals(CIBCommon.CIB_SIGN_STRING)) return;
        event.setCancelled(true);
		if(event.getPlayer().getVehicle() != null) return;
		
		Block mayBeRailBlock = event.getClickedBlock().getLocation().getBlock().getLocation().add(0, -2, 0).getBlock();
		if(mayBeRailBlock.isBlockPowered()){
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] " + ChatColor.WHITE + "Please wait for a moment.");
			return;
		}
		
		if(s.getLine(1).equals(CIBCommon.STR_TEREMINAL)){
			event.getPlayer().sendMessage(ChatColor.RED + "[CIB] " + ChatColor.WHITE + "Sorry, arrival only. You can't get on from here");
			return;
		}
		
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
		}
	}
	
	@EventHandler
	public void onBlockRedstoneEvent(BlockRedstoneEvent event){
		if(event.getNewCurrent() == 0) return;
		Block objBlock = event.getBlock();
		Block mayBeCIBSign = objBlock.getLocation().add(0, 1, 0).getBlock();
		if(!CIBCommon.isCIBSign(mayBeCIBSign)) return;
		Collection<Entity> ents = objBlock.getWorld().getNearbyEntities(objBlock.getLocation(), 5, 5, 5);
		if(ents.isEmpty()) return;
		Iterator<Entity> entsIt = ents.iterator();
		while(entsIt.hasNext()){
			Entity ent = entsIt.next();
			if(ent.getType() == EntityType.MINECART){
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
							}
						}
					}
				}
			}
		}
	}
	
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
					}
				}
			}
		}
	}
}