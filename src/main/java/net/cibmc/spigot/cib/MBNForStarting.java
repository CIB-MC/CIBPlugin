package net.cibmc.spigot.cib;

import net.cibmc.spigot.cib.util.MusicaByNote;

public enum MBNForStarting {
	SPEED_NA("PIANO:na4"),
	SPEED_NB("PIANO:nb4"),
	SPEED_NC("PIANO:nc4"),
	SPEED_ND("PIANO:nd4"),
	SPEED_NE("PIANO:ne4"),
	SPEED_NF("PIANO:nf4"),
	SPEED_HG("PIANO:hg4"),
	SPEED_HA("PIANO:ha4"),
	SPEED_HB("PIANO:hb4"),
	SPEED_HC("PIANO:hc4"),
	SPEED_HD("PIANO:hd4"),
	SPEED_HE("PIANO:he4"),
	SPEED_HF("PIANO:hf4");
	
	private MusicaByNote mbn;
	
	private MBNForStarting(String musicCode){
		this.mbn = new MusicaByNote(musicCode);
	}
	
	public MusicaByNote getMBN(){
		return this.mbn.shallowCopyFactory();
	}
}
