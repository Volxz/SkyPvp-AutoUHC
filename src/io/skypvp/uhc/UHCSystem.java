package io.skypvp.uhc;

import io.skypvp.uhc.arena.Team;
import io.skypvp.uhc.arena.UHCGame;
import io.skypvp.uhc.arena.UHCGame.GameState;
import io.skypvp.uhc.menu.Menu;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.scenario.ScenarioDrops;
import io.skypvp.uhc.timer.MatchTimer;
import io.skypvp.uhc.timer.TimerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class UHCSystem {
	
	private static SkyPVPUHC main;
	private static HashSet<Team> teams = new HashSet<Team>();
	private static HashSet<ItemStack> restrictedItems = new HashSet<ItemStack>();
	private static MatchTimer lobbyTimer;
	private static HashMap<Block, ScenarioDrops> scenarioDrops = new HashMap<Block, ScenarioDrops>();
	private static ArrayList<UUID> forcestartVotes;
	public static ScoreboardTeam GHOST_TEAM;
	
	static {
	    teams = new HashSet<Team>();
	    restrictedItems = new HashSet<ItemStack>();
	    scenarioDrops = new HashMap<Block, ScenarioDrops>();
	    forcestartVotes = new ArrayList<UUID>();
		GHOST_TEAM = new ScoreboardTeam(new Scoreboard(), "spectators");
		GHOST_TEAM.setCanSeeFriendlyInvisibles(true);
	}
	
	public static void setGhost(Player player, boolean enable) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		if(enable && player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
			packet = new PacketPlayOutScoreboardTeam(GHOST_TEAM, Arrays.asList(player.getName()), 4);
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		} else if(!enable) {
			packet = new PacketPlayOutScoreboardTeam(GHOST_TEAM, Arrays.asList(player.getName()), 3);
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
		}
		
		for(Player players : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) players).getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	public static boolean hasVotedForForceStart(UUID uuid) {
	    return forcestartVotes.contains(uuid);
	}
	
	/*
	 * Returns if the vote was successful or not.
	 * @return boolean
	 */

	public static boolean voteForForceStart(UUID uuid) {
	    boolean voted = hasVotedForForceStart(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if(p == null) return false;
        
	    if(!voted) {
            int onlinePlayers = main.getOnlinePlayers().keySet().size();
            if(main.getGame().isTeamMatch() && onlinePlayers >= main.getSettings().getMinimumTeamGamePlayers() 
                    || !main.getGame().isTeamMatch() && onlinePlayers >= main.getSettings().getMinimumSoloGamePlayers()) {
                // There's enough players online.
                if(main.getGame().getState() == GameState.WAITING) {
                    forcestartVotes.add(uuid);
                    p.sendMessage(main.getMessages().color(main.getMessages().getMessage("voteSuccess")));
                    
                    double perct = ((double) forcestartVotes.size()) / onlinePlayers;
                    if(perct >= 0.75) {
                        // Great, we have enough voters!
                        if(main.getGame().getState() == GameState.WAITING) {
                            main.getGame().setState(GameState.STARTING);
                            String forceStartMsg = main.getMessages().color(main.getMessages().getMessage("forceStartSuccess"));
                            Iterator<UUID> iterator = main.getOnlinePlayers().keySet().iterator();
                            while(iterator.hasNext()) {
                                UUID uid = iterator.next();
                                Player bP = Bukkit.getPlayer(uid);
                                if(bP != null) {
                                    bP.sendMessage(forceStartMsg);
                                }
                            }
                        }
                    }
                }
            }else {
                p.sendMessage(main.getMessages().color(main.getMessages().getMessage("notEnoughPlayers")));
                return false;
            }
	        
	        return true;
	    }
	    
	    return false;
	}
	
	public static void setLobbyTimer(SkyPVPUHC instance) {
		UHCSystem.main = instance;
		lobbyTimer = TimerUtils.createTimer(instance, "Starting", main.getSettings().getStartTime());
	}
	
	public static MatchTimer getLobbyTimer() {
		return UHCSystem.lobbyTimer;
	}
	
	public static Location getRandomSpawnPoint(double minX, double maxX, double minZ, double maxZ) {
		World world = main.getWorldHandler().getGameWorld().getCBWorld();
		double x, z;
		Block highestBlock;
		
		do {
			x = ThreadLocalRandom.current().nextDouble(minX, maxX);
			z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);
			highestBlock = world.getHighestBlockAt((int) x, (int) z);
		} while(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.WATER, Material.STATIONARY_WATER, Material.CACTUS, Material.WEB).contains(highestBlock.getType()));
		
		return new Location(world, x, highestBlock.getLocation().getY() + 1.0, z);
	}
	
	public static void addTeam(Team team) {
		teams.add(team);
	}
	
	public static HashSet<Team> getTeams() {
		return teams;
	}
	
	public static void addRestrictedItem(ItemStack item) {
		restrictedItems.add(item);
	}
	
	public static boolean isRestrictedItem(ItemStack item) {
		for(ItemStack restricted : restrictedItems) {
			if(item.isSimilar(restricted)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static HashSet<ItemStack> getRestrictedItems() {
		return restrictedItems;
	}
	
	public static void addScenarioDrop(Block block, ScenarioDrops drop) {
		scenarioDrops.put(block, drop);
	}
	
	public static ScenarioDrops getScenarioDrops(Block block) {
		return scenarioDrops.get(block);
	}
	
	public static HashMap<Block, ScenarioDrops> getScenarioDrops() {
		return scenarioDrops;
	}
	
	/**
	 * Returns a String like 1st, 2nd, 3rd, 5th, etc.
	 * @param int place
	 * @return String
	 */
	
	public static String getOrdinal(int place) {
		String suffix = "th";
		switch (place) {
			case 1:
				suffix = "st";
				break;
			case 2:
				suffix = "nd";
				break;
			case 3:
				suffix = "rd";
				break;
			default: break;
		}
		
		return String.format("%d%s", place, suffix);
	}
	
	public static void openMenu(UHCPlayer player, Menu menu) {
		player.setActiveMenu(menu);
		menu.show();
		player.getBukkitPlayer().openInventory(menu.getUI());
	}
	
	public static ItemStack nameItem(ItemStack item, String name) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack nameAndLoreItem(ItemStack item, String name, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static String getTeamNameWithPrefix(Team team) {
		String teamColor = team.getName().substring(0, 2);
		return teamColor.concat("Team ").concat(team.getName().substring(2, team.getName().length()));
	}
	
	public static void handleLobbyArrival(SkyPVPUHC main, UHCPlayer player) {
		Player p = player.getBukkitPlayer();
		UHCScoreboard scoreboard = new UHCScoreboard(main, "lobbyScoreboard", DisplaySlot.SIDEBAR);
		scoreboard.generate(main.getOnlinePlayers().get(p.getUniqueId()));
		player.setScoreboard(scoreboard);
		
		// If the game is available to join, let's give the player items.
		if(main.getGame().getState() == GameState.WAITING || main.getGame().getState() == GameState.STARTING) {
			ItemStack kitSelector = main.getSettings().getKitSelectorItem();
			ItemStack teamSelector = main.getSettings().getTeamSelectorItem();
			p.getInventory().clear();
			if(main.getGame().isTeamMatch()) p.getInventory().setItem(0, teamSelector);
			p.getInventory().setItem(8, kitSelector);
		}
	}
	
	public static void broadcastSound(Sound sound) {
        Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
        while(iterator.hasNext()) {
            UHCPlayer p = iterator.next();
            Player bP = Bukkit.getPlayer(p.getUUID());
            if(bP != null) {
                bP.playSound(bP.getLocation(), sound, 1F, 1F);
            }
        }
	}
	
	public static void broadcastMessage(String msg) {
        Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
        while(iterator.hasNext()) {
            UHCPlayer p = iterator.next();
            Player bP = Bukkit.getPlayer(p.getUUID());
            if(bP != null) {
                bP.sendMessage(msg);
            }
        }
	}
	
	public static void broadcastMessageAndSound(String msg, Sound sound) {
	    UHCSystem.broadcastMessageAndSound(msg, sound, 1F);
	}
	
	public static void broadcastMessageAndSound(String msg, Sound sound, float volume) {
	    Iterator<UHCPlayer> iterator = main.getOnlinePlayers().values().iterator();
	    while(iterator.hasNext()) {
	        UHCPlayer p = iterator.next();
	        Player bP = Bukkit.getPlayer(p.getUUID());
	        if(bP != null) {
	            bP.sendMessage(msg);
	            bP.playSound(bP.getLocation(), sound, volume, volume);
	        }
	    }
	}
	
	public static boolean canStartGame() {
		UHCGame game = main.getGame();
		int onlinePlayers = main.getOnlinePlayers().keySet().size();
		if((game.isTeamMatch() && onlinePlayers >= main.getSettings().getMinimumTeamGamePlayers()) || (!game.isTeamMatch() && 
				onlinePlayers >= main.getSettings().getMinimumSoloGamePlayers())) {
			return true;
		}
		
		return false;
	}
}
