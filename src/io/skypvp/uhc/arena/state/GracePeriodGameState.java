package io.skypvp.uhc.arena.state;

import io.skypvp.uhc.SkyPVPUHC;
import io.skypvp.uhc.UHCSystem;
import io.skypvp.uhc.player.UHCPlayer;
import io.skypvp.uhc.player.UHCPlayer.PlayerState;
import io.skypvp.uhc.timer.TimerUtils;

public class GracePeriodGameState extends TimedGameState {

	public GracePeriodGameState(SkyPVPUHC instance, GameStateManager stateMgr) {
		super(instance, "gracePeriod", stateMgr);
	}

	public void onEnter() {
		// Activate all the players.
		for(UHCPlayer player : main.getOnlinePlayers().values()) {
			player.setState(PlayerState.ACTIVE);
		}

		main.getGame().activateScenarios();
		stateMgr.setTimer(TimerUtils.createTimer(main, "Grace Period", 
		        main.getProfile().getGracePeriodLength()));
		timer = stateMgr.getTimer();
		UHCSystem.broadcastMessageAndSound(main.getMessages().getMessage("grace-period-begin"), main.getSettings().getStateUpdateSound());
		super.onEnter();
	}
}
