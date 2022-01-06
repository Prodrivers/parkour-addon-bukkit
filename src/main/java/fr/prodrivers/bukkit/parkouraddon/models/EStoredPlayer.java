package fr.prodrivers.bukkit.parkouraddon.models;

import fr.prodrivers.bukkit.commons.storage.player.StoredPlayer;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.plugin.EConfiguration;
import io.ebean.Database;
import io.ebean.annotation.Cache;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "players")
@Cache
public class EStoredPlayer extends StoredPlayer {
	@Column(columnDefinition = "integer default 0 not null")
	@NotNull
	@Getter
	@Setter
	int parkoins;

	@Column(columnDefinition = "integer default 0 not null")
	@NotNull
	@Getter
	@Setter
	int parkourLevel;

	public enum ParkoinsSyncWay {
		DB_TO_PLUGIN,
		PLUGIN_TO_DB
	}

	public EStoredPlayer(Player player) {
		super(player.getUniqueId());
	}

	public static EStoredPlayer get(Database database, Player player) {
		return database
				.find(EStoredPlayer.class)
				.select("parkourLevel")
				.select("parkoins")
				.where().eq("uniqueId", player.getUniqueId())
				.findOne();
	}

	public EStoredPlayer onLogin(Player player, EConfiguration configuration, Parkoins parkoinsManager, ParkourLevel parkourLevelManager, Economy economy) {
		setParkourLevel(parkourLevelManager.getLevel(player));
		double parkoinsAmount = parkoinsManager.get(player);
		if(configuration.parkoins_syncToVault && economy != null) {
			double balance = economy.getBalance(player);
			if(balance > parkoinsAmount) {
				economy.depositPlayer(player, balance - parkoinsAmount);
			} else {
				economy.withdrawPlayer(player, parkoinsAmount - balance);
			}
		} else {
			switch(configuration.parkoins_syncWay) {
				case DB_TO_PLUGIN -> {
					if(this.parkoins > parkoinsAmount) {
						parkoinsManager.add(player, (int) (this.parkoins - parkoinsAmount));
					} else {
							parkoinsManager.remove(player, (int) (parkoinsAmount - this.parkoins));
					}
				}
				case PLUGIN_TO_DB -> this.parkoins = (int) parkoinsAmount;
			}
		}
		return this;
	}

	public void addParkoins(int amount) {
		this.parkoins += amount;
	}
}

