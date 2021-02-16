package net.cibmc.spigot.cib;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CIBCommon {
	public static final String CIB_SIGN_STRING = "[CIB_MC]";
	public static final String CIB_METADATA_TYPE = "CIB";
	public static final String CIB_METADATA_STARTED = "CIBSTARTED";
	public static final Vector VEC_STOP = new Vector(0,0,0);
	public static final Vector VEC_NORTH = new Vector(0,0,-10);
	public static final Vector VEC_SOUTH = new Vector(0,0,10);
	public static final Vector VEC_EAST = new Vector(10,0,0);
	public static final Vector VEC_WEST = new Vector(-10,0,0);
	
	public static final int TP_SEARCH_RADIUS_X = 1;
	public static final int TP_SEARCH_RADIUS_Y = 0;
	public static final int TP_SEARCH_RADIUS_Z = 1;
	
	public static final String STR_NORTH = "NORTH";
	public static final String STR_SOUTH = "SOUTH";
	public static final String STR_EAST = "EAST";
	public static final String STR_WEST = "WEST";
	public static final String STR_TEREMINAL = "TERMINAL";
	
	private CIBCommon(){
		throw new AssertionError();
	}//End default constructor killer
	
	public static Minecart getMinecartEntity(World world, int entityID){
		if(world == null) return null;
		Collection<Entity> entities = world.getEntities();
		Iterator<Entity> it = entities.iterator();
		while(it.hasNext()){
			Entity ent;
			if((ent = it.next()).getEntityId() == entityID){
				if(ent instanceof Minecart)return (Minecart)ent;
			}//End if
		}//End if
		return null;
	}//End public static Minecart getMinecartEntity(World world, int entityID)
	
	public static boolean isThereCurveRailNearDatnum(Location datnum, int radius){
		for(int i = radius * -1; i <= radius; i++){
			for(int j = radius * -1; j <= radius; j++){
				for(int k = radius * -1; k <= radius; k++){
					Block b = datnum.getWorld().getBlockAt(datnum.getBlockX() + i, datnum.getBlockY() + j,datnum.getBlockZ() + k);
					BlockData bd = b.getState().getBlock().getBlockData();
					if(bd instanceof Rail){
						Rail r = (Rail)bd;
						switch(r.getShape()) {
							case NORTH_EAST:
							case NORTH_WEST:
							case SOUTH_EAST:
							case SOUTH_WEST:
								return true;
							default:
								break;
						}
					}//End if
				}//Next k
			}//Next j
		}//Next i
		return false;
	}//End public static Location isThereSpecifiedBlockNearHere
	
	public static boolean isThereSlopeRailNearDatnum(Location datnum, int radius){
		for(int i = radius * -1; i <= radius; i++){
			for(int j = 0; j <= radius; j++){
				for(int k = radius * -1; k <= radius; k++){
					Block b = datnum.getWorld().getBlockAt(datnum.getBlockX() + i, datnum.getBlockY() + j,datnum.getBlockZ() + k);
					BlockData bd = b.getState().getBlock().getBlockData();
					if(bd instanceof Rail){
						Rail r = (Rail)b;
						switch(r.getShape()) {
							case ASCENDING_NORTH:
							case ASCENDING_SOUTH:
							case ASCENDING_EAST:
							case ASCENDING_WEST:
								return true;
							default:
								break;
						}
					}//End if
				}//Next k
			}//Next j
		}//Next i
		return false;
	}//End public static Location isThereSpecifiedBlockNearHere
	
	public static Location getSafetyTpLoc(Location datnum, CIBPlugin plugin){
		for(int i = TP_SEARCH_RADIUS_Y; i >= TP_SEARCH_RADIUS_Y * -1; i--){
			for(int j = TP_SEARCH_RADIUS_X; j >= TP_SEARCH_RADIUS_X * -1; j--){
				for(int k = TP_SEARCH_RADIUS_Z; k >= TP_SEARCH_RADIUS_Z * -1; k--){
					//Block tmpBlock = datnum.getWorld().getBlockAt(datnum.getBlockX() + j , datnum.getBlockY() + i, datnum.getBlockZ() + k);
					Block tmpBlock = datnum.getBlock().getLocation().add(j, i, k).getBlock();
					if(isRideableBlock(tmpBlock)){
						if(tmpBlock.getLocation().add(0,1,0).getBlock().isEmpty() && tmpBlock.getLocation().add(0,2,0).getBlock().isEmpty() && tmpBlock.getLocation().add(0,3,0).getBlock().isEmpty()){
							Location resultLoc = tmpBlock.getLocation().add(0,1,0);
							return resultLoc;
						}//End if
					}//End if
				}//Next k
			}//Next j
		}//Next i
		//System.out.println("===== Search done. =====");
		return null;
	}//End public static Location getSafetyTpLoc(Location datnum)
	
	public static boolean isCIBSign(Block b){
		Material mat = b.getState().getBlock().getType();
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
				return false;
		}
		Sign s = (Sign)b.getState();
		if(s.getLine(0).equals(CIB_SIGN_STRING)) return true;
		return false;
	}//End public static boolean isCIBSign(Block b)
	
	public static boolean isPlayerRideOnMinecart(Player pl){
		if(pl.isInsideVehicle()){
			Entity veh = pl.getVehicle();
			if(veh.getType() == EntityType.MINECART){
				return true;
			}//End if level 2
		}//End if level 1
		return false;
	}//End public static boolean isPlayerRideOnHorse
	
	public static boolean isRideableBlock(Block b){
		if(b.isEmpty()) return false;
		if(b.isLiquid()) return false;
		
		Material mat = b.getState().getType();
		if(mat.equals(Material.RAIL)) return false;
		if(mat.equals(Material.DETECTOR_RAIL)) return false;
		if(mat.equals(Material.POWERED_RAIL)) return false;
		if(mat.equals(Material.ACTIVATOR_RAIL)) return false;
		if(mat.equals(Material.SNOW)) return false;
		if(mat.equals(Material.ACACIA_FENCE)) return false;
		if(mat.equals(Material.BIRCH_FENCE)) return false;
		if(mat.equals(Material.SPRUCE_FENCE)) return false;
		if(mat.equals(Material.JUNGLE_FENCE)) return false;
		if(mat.equals(Material.DARK_OAK_FENCE)) return false;
		
		return true;
	}//End public static boolean isNonRideableBlock(Block b)
}//End public class CIBCommon
