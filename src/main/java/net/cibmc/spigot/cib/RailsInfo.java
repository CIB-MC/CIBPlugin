package net.cibmc.spigot.cib;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;

public class RailsInfo {
	private static final int SEARCH_RADIUS_X = 6;
	private static final int SEARCH_RADIUS_Y = 5;
	private static final int SEARCH_RADIUS_Z = 6;
	private static final int FLAG_HAS_FLAT = 0x1;
	private static final int FLAG_HAS_ONSLOPE = 0x2;
	private static final int FLAG_HAS_CURVE = 0x4;
	
	private int flags = 0;
	
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
								ri.flags = ri.flags | FLAG_HAS_CURVE;
								break;
							case ASCENDING_NORTH:
							case ASCENDING_SOUTH:
							case ASCENDING_EAST:
							case ASCENDING_WEST:
								ri.flags = ri.flags | FLAG_HAS_ONSLOPE;
								break;
							default:
								ri.flags = ri.flags | FLAG_HAS_FLAT;
								break;
						}
					}//End if
				}//Next k
			}//Next j
		}//Next i
		return ri;
	}//End public static RailsInfo getRailsInfoAroundLocation(Location loc)
	
	public boolean isAtAccelerateableLoc(){
		return (Integer.bitCount(this.flags) == 1) & !this.hasOnSlope();
	}//End public boolean hasSingleTypeRail()

	public boolean hasOnSlope(){
		return (this.flags & FLAG_HAS_ONSLOPE) > 0;
	}//End getter
	
	public boolean hasCurve(){
		return (this.flags & FLAG_HAS_CURVE) > 0;
	}//End getter
	
	public boolean hasFlat(){
		return (this.flags & FLAG_HAS_FLAT) > 0;
	}//End getter
}//End public class RailsInfo
