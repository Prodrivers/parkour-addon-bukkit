package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
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

	private static class ParkoinsSetThread extends Thread {
		private final Database database;
		private final Player player;
		private final double parkoins;

		@Inject
		ParkoinsSetThread(final Database database, final Player player, final double parkoins) {
			this.database = database;
			this.player = player;
			this.parkoins = parkoins;
		}

		public void run() {
			// Update the player's parkoins
			SqlUpdate query = this.database.sqlUpdate(Utils.SET_PLAYER_PARKOINS_QUERY);
			query.setParameter(1, parkoins);
			query.setParameter(2, Utils.getBytesFromUniqueId(player.getUniqueId()));
			if(query.execute() == 0) {
				Log.severe("Cannot update player parkoins.");
			}
		}
	}

	public double get(Player player) {
		return PlayerInfo.getParkoins(player);
	}

	public void add(Player player, int parkoins) {
		Parkour.getInstance().getPlayerManager().rewardParkoins(player, parkoins);

		final double balance = get(player);
		(new ParkoinsSetThread(this.database, player, balance)).start();
	}

	public void remove(Player player, int parkoins) {
		Parkour.getInstance().getPlayerManager().deductParkoins(player, parkoins);

		final double balance = get(player);
		(new ParkoinsSetThread(this.database, player, balance)).start();
	}
}
