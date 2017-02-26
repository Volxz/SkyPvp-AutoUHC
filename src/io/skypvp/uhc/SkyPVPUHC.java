package io.skypvp.uhc;

import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.event.TrafficEventsListener;
import io.skypvp.uhc.player.UHCPlayer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.wimbli.WorldBorder.WorldBorder;

public class SkyPVPUHC extends JavaPlugin {
	
	private Settings settings;
	private Messages msgs;
	private MultiverseCore multiverse;
	private WorldHandler worldHandler;
	private WorldBorder worldBorder;
	private UHCGame game;
	private HashMap<UUID, UHCPlayer> onlinePlayers;
	
	public void onEnable() {
		// We're going to require the lobby world to be called a certain name.
		if(Bukkit.getWorld(Globals.LOBBY_WORLD_NAME) == null) {
			sendConsoleMessage(ChatColor.DARK_RED + 
				String.format("Your main UHC lobby world must be called '%s' in order for this plugin to work correctly.", 
			Globals.LOBBY_WORLD_NAME));
			disable();
			return;
		}
		
		
		onlinePlayers = new HashMap<UUID, UHCPlayer>();
		worldHandler = null;
		multiverse = (MultiverseCore) getRequiredDependency("Multiverse-Core", MultiverseCore.class);
		worldBorder = (WorldBorder) getRequiredDependency("WorldBorder", WorldBorder.class);
		settings = new Settings(this);
		msgs = new Messages(this);
		settings.load();
	}
	
	public JavaPlugin getRequiredDependency(final String name, final Class<? extends JavaPlugin> returnType) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(name);
		
		if(plugin != null && returnType.isInstance(plugin)) {
			sendConsoleMessage(ChatColor.DARK_GREEN + String.format("Successfully hooked into %s [%s]!", 
					plugin.getDescription().getName(), 
			plugin.getDescription().getVersion()));
			
			return returnType.cast(plugin);
		}
		
		sendConsoleMessage(ChatColor.DARK_RED + String.format("%s could not be found. You must install it to use this plugin.", name));
		setEnabled(false);
		return null;
	}
	
	public void databaseConnected() {
		if(isEnabled()) {
			msgs = new Messages(this);
			game = new UHCGame(this);
			worldHandler = new WorldHandler(this);
			UHCSystem.setLobbyTimer(this);
			
			// We're listening for join and leave events for database and arena purposes.
			getServer().getPluginManager().registerEvents(new TrafficEventsListener(this), this);
		}
	}
	
	public void onDisable() {
		// In the case of a plugin unload, let's save player stats.
		if(onlinePlayers.size() > 0) {
			for(UUID id : onlinePlayers.keySet()) {
				settings.getDatabase().handlePlayerExit(id);
			}
		}
		
		if(settings != null && settings.getDatabase() != null) {
			try {
				settings.getDatabase().getConnection().close();
				sendConsoleMessage(ChatColor.DARK_GREEN + "Successfully closed connection to MySQL database.");
			} catch (SQLException | NullPointerException e) {
				sendConsoleMessage(ChatColor.DARK_RED + "Encountered an error while closing connection to MySQL database.");
				e.printStackTrace();
			}
		}
	}
	
	public void disable() {
		sendConsoleMessage(ChatColor.DARK_RED + "Disabling...");
		setEnabled(false);
	}
	
	public Settings getSettings() {
		return this.settings;
	}
	
	public Messages getMessages() {
		return this.msgs;
	}
	
	public MultiverseCore getMultiverse() {
		return this.multiverse;
	}
	
	public WorldHandler getWorldHandler() {
		return this.worldHandler;
	}
	
	public WorldBorder getWorldBorder() {
		return this.worldBorder;
	}
	
	public UHCGame getGame() {
		return this.game;
	}
	
	public HashMap<UUID, UHCPlayer> getOnlinePlayers() {
		return this.onlinePlayers;
	}
	
	public void sendConsoleMessage(String msg) {
		String pluginName = getDescription().getName();
		String prefix = ChatColor.DARK_GRAY +  "[" + ChatColor.GOLD + pluginName + ChatColor.DARK_GRAY + "]";
		getServer().getConsoleSender().sendMessage(prefix + " " + msg);
	}
	
}