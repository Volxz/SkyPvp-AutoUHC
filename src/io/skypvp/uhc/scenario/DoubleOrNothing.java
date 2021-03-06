package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class DoubleOrNothing extends DropUpdaterScenario {
	
	public DoubleOrNothing(SkyPVPUHC main) {
		super(main, ScenarioType.DOUBLE_OR_NOTHING);
	}

	@Override
	public ArrayList<ItemStack> handleDrops(ArrayList<ItemStack> drops) {
		int rndmNum = ThreadLocalRandom.current().nextInt(0, 100);
		Iterator<ItemStack> iterator = drops.iterator();
		while(iterator.hasNext()) {
			ItemStack item = iterator.next();
			iterator.remove();
			if(rndmNum > 50) {
				item.setAmount(item.getAmount() * 2);
				drops.add(item);
			}
		}
		
		return drops;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Player p = evt.getPlayer();
		UHCPlayer uhcPlayer = instance.getOnlinePlayers().get(p.getUniqueId());
		Block b = evt.getBlock();
		
		if(uhcPlayer != null && TripleOres.ORES.contains(b.getType()) && isActive()) {
			ScenarioDrops drops = UHCSystem.getScenarioDrops(b);
			if(drops == null && evt.isCancelled()) return;
			
			if(drops == null) {
				drops = new ScenarioDrops(instance, evt);
				UHCSystem.addScenarioDrop(b, drops);
			}
			
			drops.queue(this);
		}
	}

	@Override
	public void unregisterEvents() {
		BlockBreakEvent.getHandlerList().unregister(this);
	}

}
