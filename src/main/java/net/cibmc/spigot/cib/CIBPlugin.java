package net.cibmc.spigot.cib;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import net.cibmc.spigot.cib.util.MusicaByNote;

public class CIBPlugin extends JavaPlugin {
	public static final int AMC_TICK_INC = 3;
	public static final int MBN_TICK_INC = 2;
	
	ConcurrentHashMap<String,Block> blockBox = new ConcurrentHashMap<String,Block>();
	ConcurrentHashMap<String,MinecartInfo> minecartBox = new ConcurrentHashMap<String,MinecartInfo>();
	ConcurrentHashMap<MinecartInfo,MusicaByNote> musicBox = new ConcurrentHashMap<MinecartInfo,MusicaByNote>();
	MusicaByNote mbn = new MusicaByNote("PIANO:nc4hg4nd4hg4ne4hg4nd4hg4nc4hg4nd4hg4ne4hg4nd4hg4nc4");
	MusicaByNote mbnArrived = new MusicaByNote("PIANO:hg8nc8hg8nc8hg8nc8hg8nc8hg60");
	@Override
	public void onEnable(){
		this.getLogger().info("====== Bonjour, Admin! ======");
		this.getLogger().info("            [CIB]");
		this.getLogger().info("          Compagnie");
		this.getLogger().info("        internationale");
		this.getLogger().info("         des berlines");
		this.getLogger().info("====== Start initialize ======");
		getServer().getPluginManager().registerEvents(new EL_CIBSign(this), this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SyncAMC(this), AMC_TICK_INC, AMC_TICK_INC);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SyncMBNInc(this, MBN_TICK_INC), 5, MBN_TICK_INC);
		this.getLogger().info("I won't struggle in the Database, Wow Wow.");
		this.getLogger().info("====== End initialize ======");
	}//End public void onEnable()
	
	@Override
	public void onDisable(){
	}//End public void onDisable()
}//End public class CIBPlugin extends JavaPlugin
