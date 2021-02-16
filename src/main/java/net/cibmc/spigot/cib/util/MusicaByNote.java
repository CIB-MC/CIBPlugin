package net.cibmc.spigot.cib.util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;

public class MusicaByNote {
	private ArrayList<OneSound> music;
	private int currentSound;
	private int iTick;
	
	private MusicaByNote(){
		music = null;
		currentSound = -1;
		iTick = 0;
	}
	
	public MusicaByNote(String music){
		this.music = new ArrayList<OneSound>();
		currentSound = -1;
		iTick = 0;
		music = music.toUpperCase(Locale.ENGLISH);
		Pattern p = Pattern.compile("^(BASS_DRUM|BASS_GUITAR|PIANO|SNARE_DRUM|STICKS):(((N|H)[A-G](F|S)?[0-9]+)+)");
		Matcher m = p.matcher(music);
		if(!m.find()) return;
		Instrument tempInst = null;
		if(m.group(1).equalsIgnoreCase("BASS_DRUM")){
			tempInst = Instrument.BASS_DRUM;
		}else if(m.group(1).equalsIgnoreCase("BASS_GUITAR")){
			tempInst = Instrument.BASS_GUITAR;
		}else if(m.group(1).equalsIgnoreCase("PIANO")){
			tempInst = Instrument.PIANO;
		}else if(m.group(1).equalsIgnoreCase("SNARE_DRUM")){
			tempInst = Instrument.SNARE_DRUM;
		}else if(m.group(1).equalsIgnoreCase("STICKS")){
			tempInst = Instrument.STICKS;
		}else{
			return;
		}//End if
		
		String sounds = m.group(2);
		Pattern p2 = Pattern.compile("^((N|H)([A-G])((F|S)?)([0-9]+))");
		Matcher m2 = p2.matcher(sounds);
		while(m2.find()){
			OneSound os = new OneSound();
			os.insturument = tempInst;
			
			int tempOctave = 0;
			switch(m2.group(2)){
			case "N":
				tempOctave = 0;
				break;
			case "H":
				tempOctave = 1;
				break;
			default:
				return;
			}//End switch-case
			
			Note.Tone tempNoteTone = null;
			switch(m2.group(3)){
			case "A":
				tempNoteTone = Note.Tone.A;
				break;
			case "B":
				tempNoteTone = Note.Tone.B;
				break;
			case "C":
				tempNoteTone = Note.Tone.C;
				break;
			case "D":
				tempNoteTone = Note.Tone.D;
				break;
			case "E":
				tempNoteTone = Note.Tone.E;
				break;
			case "F":
				tempNoteTone = Note.Tone.F;
				break;
			case "G":
				tempNoteTone = Note.Tone.G;
				break;
			default:
				return;
			}//End switch-case;
			
			switch(m2.group(4)){
			case "":
				os.note = Note.natural(tempOctave, tempNoteTone);
				break;
			case "F":
				os.note = Note.flat(tempOctave, tempNoteTone);
				break;
			case "S":
				os.note = Note.sharp(tempOctave, tempNoteTone);
				break;
			}//End switch-case
			
			os.ticks = Integer.parseInt(m2.group(6));
			this.music.add(os);
			sounds = m2.replaceFirst("");
			m2 = p2.matcher(sounds);
		}//End while
		
		return;
	}//End public static MusicaByNote compile(String music)
	
	public MusicaByNote shallowCopyFactory(){
		MusicaByNote mbn = new MusicaByNote();
		mbn.music = this.music;
		return mbn;
	}//End CopyFactory
	
	public void playSoundIfAtTime(Player pl, int ticks){
		if(this.music.isEmpty()) return;
		if(this.music.size() <= currentSound) return;
		
		boolean playAtThisTime = false;
		
		if(this.currentSound == -1){
			playAtThisTime = true;
			this.currentSound++;
		}else{
			this.iTick += ticks;
			if(iTick >= this.music.get(this.currentSound).ticks){
				currentSound++;
				if(this.music.size() > currentSound){
					playAtThisTime = true;
				}//End if
				this.iTick = 0;
			}//End if
		}//End if
		
		if(playAtThisTime){
			pl.playNote(pl.getLocation(), this.music.get(currentSound).insturument, this.music.get(currentSound).note);
			
		}//End if
	}//End public void playSoundIfAtTime(Player pl, int ticks)
	
	public boolean isEnd(){
		return this.music.size() <= currentSound;
	}//End public boolean isEnd()
	
	class OneSound{
		Instrument insturument;
		Note note;
		int ticks;
	}//End class OneSound
}
