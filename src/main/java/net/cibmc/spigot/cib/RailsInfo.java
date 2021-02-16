package net.cibmc.spigot.cib;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;

public class RailsInfo {
	private static final int SEARCH_RADIUS_X = 6;
	private static final int SEARCH_RADIUS_Y = 5;
	private static final int SEARCH_RADIUS_Z = 6;
	
	private boolean hasOnSlope = false;
	private boolean hasCurve = false;
	private boolean hasFlat = false;
	
	private RailsInfo(){/* Nothing to do. */}
	
	public static RailsInfo getRailsInfoAroundLocation(Location loc){
		RailsInfo ri = new RailsInfo();
		for(int i = SEARCH_RADIUS_X * -1; i <= SEARCH_RADIUS_X; i++){
			for(int j = SEARCH_RADIUS_Y * -1; j <= SEARCH_RADIUS_Y; j++){
				for(int k = SEARCH_RADIUS_Z * -1; k <= SEARCH_RADIUS_Z; k++){
					Block b = loc.getWorld().getBlockAt(loc.getBlockX() + i, loc.getBlockY() + j,loc.getBlockZ() + k);
					BlockData bd = b.getState().getBlock().getBlockData();
					if(bd instanceof Rail){
						Rail r = (Rail)bd;
						switch(r.getShape()) {
							case NORTH_EAST:
							case NORTH_WEST:
							case SOUTH_EAST:
							case SOUTH_WEST:
								ri.hasCurve = true;
							case ASCENDING_NORTH:
							case ASCENDING_SOUTH:
							case ASCENDING_EAST:
							case ASCENDING_WEST:
								ri.hasOnSlope = true;
							default:
								ri.hasFlat = true;
						}
					}//End if
				}//Next k
			}//Next j
		}//Next i
		return ri;
	}//End public static RailsInfo getRailsInfoAroundLocation(Location loc)
	
	public boolean isAtAccelerateableLoc(){
		return !this.hasOnSlope && this.hasFlat ^ this.hasCurve;
	}//End public boolean hasSingleTypeRail()

	public boolean hasOnSlope(){
		return this.hasOnSlope;
	}//End getter
	
	public boolean hasCurve(){
		return this.hasCurve;
	}//End getter
	
	public boolean hasFlat(){
		return this.hasFlat;
	}//End getter
}//End public class RailsInfo
