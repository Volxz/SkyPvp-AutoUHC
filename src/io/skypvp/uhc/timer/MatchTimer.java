package io.skypvp.uhc.timer;

import java.util.Iterator;

import org.bukkit.scheduler.BukkitRunnable;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.timer.event.UHCMatchTimerExpiredEvent;

public class MatchTimer extends BukkitRunnable {

	final SkyPVPUHC main;
	private int initialMinutes;
	private int initialSeconds;
	private String name;
	private int minutes;
	private int seconds;
	private boolean cancelRequested;

	public MatchTimer(SkyPVPUHC instance, String name, int minutes, int seconds) {
		this.main = instance;
		this.name = name;
		this.minutes = initialMinutes = minutes;
		this.seconds = initialSeconds = seconds;
		this.cancelRequested = false;
	}

	public void run() {
		if(isCancelRequested()) {
			this.cancel();
			cancelRequested = false;
			return;
		}

		if((seconds - 1) < 0) {
			minutes--;
			seconds = 59;
		}else {
			if((seconds - 1) > 0 || (seconds - 1) == 0 && minutes > 0) {
				seconds--;
			}else if((seconds - 1) == 0 && minutes == 0) {
				seconds--;

				this.cancel();
				main.getServer().getPluginManager().callEvent(new UHCMatchTimerExpiredEvent(this));
			}
		}
		
		Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
		
		while(iterator.hasNext()) {
		    UHCPlayer p = iterator.next();
		    
		    if(p.getScoreboard() != null) {
		        p.getScoreboard().generate(p);
		        p.setScoreboard(p.getScoreboard());
		    }
		}
	}

	public void requestCancel() {
		this.cancelRequested = true;
	}

	public boolean isCancelRequested() {
		return this.cancelRequested;
	}

	public void reset() {
		this.minutes = initialMinutes;
		this.seconds = initialSeconds;
	}

	public int getInitialMinutes() {
		return this.minutes;
	}

	public int getInitialSeconds() {
		return this.seconds;
	}

	public void set(String name, int minutes, int seconds) {
		setName(name);
		setMinutes(minutes);
		setSeconds(seconds);
		initialMinutes = minutes;
		initialSeconds = seconds;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return this.name;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getMinutes() {
		return this.minutes;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getSeconds() {
		return this.seconds;
	}

	/**
	 * Returns the time in a watch format.
	 * Example: 12:05
	 * @return String
	 */

	public String toString() {
		if(initialMinutes != 0) {
			String mins = (minutes >= 10) ? String.valueOf(minutes) : String.format("0%d", minutes);
			String secs = (seconds >= 10) ? String.valueOf(seconds) : String.format("0%d", seconds);
			return mins.concat(":").concat(secs);
		}else {
			return String.format("%ds", seconds);
		}
	}
}
