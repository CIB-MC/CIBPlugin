package net.cibmc.spigot.cib;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import net.cibmc.spigot.cib.util.MusicaByNote;

public class SyncMBNInc extends Thread {
	private CIBPlugin plugin;
	private int incrementNum;
	
	public SyncMBNInc(CIBPlugin plugin,int incrementNum){
		this.plugin = plugin;
		this.incrementNum = incrementNum;
	}//End Constructor
	
	public void run(){
		synchronized(plugin.minecartBox){
			Iterator<Entry<MinecartInfo, MusicaByNote>> it = plugin.musicBox.entrySet().iterator();
			while(it.hasNext()){
				Entry<MinecartInfo, MusicaByNote> entry = it.next();
				MusicaByNote mbn = entry.getValue();
				Minecart mc = entry.getKey().minecartEnt;
				if(mc == null || mbn.isEnd()){
					plugin.musicBox.remove(entry.getKey());
				}else{
					List<Entity> entList = mc.getPassengers();
					for (int i = 0; i < entList.size(); i++){
						Entity ent = entList.get(i);
						if(ent instanceof Player){
							mbn.playSoundIfAtTime((Player)ent, incrementNum);
						}//End if
					}
				}//End if
			}//Next it
		}//End sync
	}//End public void run();
}//End public class SyncMBNInc extends Thread
