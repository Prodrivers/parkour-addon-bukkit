package fr.prodrivers.bukkit.parkouraddon.listeners;

import fr.prodrivers.bukkit.parkouraddon.ui.ParkourSelection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerLoginLogoutListener implements Listener {
	private final ParkourSelection parkourSelectionUi;

	@Inject
	public PlayerLoginLogoutListener(ParkourSelection parkourSelectionUi) {
		this.parkourSelectionUi = parkourSelectionUi;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.parkourSelectionUi.reload(event.getPlayer());
	}

	public void unregister() {
		PlayerQuitEvent.getHandlerList().unregister(this);
	}
}