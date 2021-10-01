package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import io.ebean.Database;
import io.ebean.SqlUpdate;
import io.github.a5h73y.parkour.type.player.PlayerInfo;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParkourLevel {
	private final Database database;

	@Inject
	public ParkourLevel(Database database) {
		this.database = database;
	}

	private static class ParkourLevelSetThread extends Thread {
		private final Player player;
		private final int level;
		private final Database database;

		ParkourLevelSetThread(final Player player, final int level, final Database database) {
			this.player = player;
			this.level = level;
			this.database = database;
		}

		public void run() {
			// Update the player's parkour level
			SqlUpdate query = this.database.sqlUpdate(Utils.SET_PLAYER_PARKOUR_LEVEL_QUERY);
			query.setParameter(1, level);
			query.setParameter(2, Utils.getBytesFromUniqueId(player.getUniqueId()));
			if(query.execute() == 0) {
				Log.severe("Cannot update player parkour level.");
			}
		}
	}

	public int getLevel(Player player) {
		return PlayerInfo.getParkourLevel(player);
	}

	public void setLevel(Player player, int level) {
		Log.info("Setting player " + player.getName() + " level to " + level + ".");
		PlayerInfo.setParkourLevel(player, level);
		(new ParkourLevelSetThread(player, level, this.database)).start();
	}
}
