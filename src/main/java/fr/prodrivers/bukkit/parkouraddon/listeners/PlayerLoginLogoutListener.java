package fr.prodrivers.bukkit.parkouraddon.listeners;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.EStoredPlayer;
import fr.prodrivers.bukkit.parkouraddon.plugin.EConfiguration;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourSelection;
import io.ebean.Database;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerLoginLogoutListener implements Listener {
	private final Database database;
	private final EConfiguration configuration;
	private final Economy economy;
	private final Parkoins parkoinsManager;
	private final ParkourLevel parkourLevelManager;
	private final ParkourSelection parkourSelectionUi;

	@Inject
	public PlayerLoginLogoutListener(Database database, EConfiguration configuration, Economy economy, Parkoins parkoinsManager, ParkourLevel parkourLevelManager, ParkourSelection parkourSelectionUi) {
		this.database = database;
		this.configuration = configuration;
		this.economy = economy;
		this.parkoinsManager = parkoinsManager;
		this.parkourLevelManager = parkourLevelManager;
		this.parkourSelectionUi = parkourSelectionUi;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		new Thread(() -> {
			EStoredPlayer storedPlayer = EStoredPlayer.get(this.database, player);
			if(storedPlayer != null) {
				storedPlayer.onLogin(player, this.configuration, this.parkoinsManager, this.parkourLevelManager, this.economy);
			}
		}).start();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.parkourSelectionUi.reload(event.getPlayer());
	}

	public void unregister() {
		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}
}