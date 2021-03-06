package io.skypvp.uhc.scenario;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class TripleOres extends DropUpdaterScenario {
	
	public static ArrayList<Material> ORES;
	
	static {
		ORES = new ArrayList<Material>();
		ORES.add(Material.COAL_ORE);
		ORES.add(Material.REDSTONE_ORE);
		ORES.add(Material.EMERALD_ORE);
		ORES.add(Material.GOLD_ORE);
		ORES.add(Material.IRON_ORE);
		ORES.add(Material.LAPIS_ORE);
		ORES.add(Material.DIAMOND_ORE);
	}
	
	public TripleOres(SkyPVPUHC main) {
		super(main, ScenarioType.TRIPLE_ORES);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt) {
		Block b = evt.getBlock();
		
		if(isActive() && ORES.contains(b.getType())) {
			ScenarioDrops drops = UHCSystem.getScenarioDrops(b);
			if(drops == null && evt.isCancelled()) return;
			
			if(drops == null && !evt.isCancelled()) {
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
	
	public static ArrayList<Material> getOres() {
		return TripleOres.ORES;
	}

	public ArrayList<ItemStack> handleDrops(ArrayList<ItemStack> drops) {
		Iterator<ItemStack> iterator = drops.iterator();
		ArrayList<ItemStack> newDrops = new ArrayList<ItemStack>();
		newDrops.addAll(drops);
		while(iterator.hasNext()) {
			ItemStack drop = iterator.next();
			ItemStack update = drop.clone();
			update.setAmount((update.getAmount() * 3) - drop.getAmount());
			newDrops.add(update);
		}
		
		return newDrops;
	}

}
