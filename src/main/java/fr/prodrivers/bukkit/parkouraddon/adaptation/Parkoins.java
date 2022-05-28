package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.models.EStoredPlayer;
import io.ebean.Database;
import io.ebean.SqlUpdate;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.player.PlayerInfo;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Parkoins {
	private final Database database;

	@Inject
	public Parkoins(Database database) {
		this.database = database;
	}

	public double get(Player player) {
		return PlayerInfo.getParkoins(player);
	}

	public void add(Player player, int parkoins) {
		Parkour.getInstance().getPlayerManager().rewardParkoins(player, parkoins);

		// Adjust parkoins in database
		if(parkoins != 0) {
			new Thread(() -> {
				EStoredPlayer storedPlayer = EStoredPlayer.get(this.database, player);
				if(storedPlayer != null) {
					storedPlayer.addParkoins(parkoins);
					this.database.update(storedPlayer);
				}
			}).start();
		}
	}

	public void remove(Player player, int parkoins) {
		Parkour.getInstance().getPlayerManager().deductParkoins(player, parkoins);

		// Adjust parkoins in database
		if(parkoins != 0) {
			new Thread(() -> {
				EStoredPlayer storedPlayer = EStoredPlayer.get(this.database, player);
				if(storedPlayer != null) {
					storedPlayer.removeParkoins(parkoins);
					this.database.update(storedPlayer);
				}
			}).start();
		}
	}
}
