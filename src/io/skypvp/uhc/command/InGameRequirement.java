package io.skypvp.uhc.command;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.player.UHCPlayer;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InGameRequirement extends PlayerRequirement {

    @Override
    public boolean isReached(CommandSender sender) {
        if(super.isReached(sender)) {
            Player p = (Player) sender;
            UHCPlayer uhcPlayer = SkyPVPUHC.onlinePlayers.get(p.getUniqueId());
            
            return (uhcPlayer != null && uhcPlayer.isInGame());
        }
        
        return false;
    }

    @Override
    public void onFailed(CommandSender sender) {
        if(sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "You must be in a game to execute that command.");
        }else {
            super.onFailed(sender);
        }
    }

}
