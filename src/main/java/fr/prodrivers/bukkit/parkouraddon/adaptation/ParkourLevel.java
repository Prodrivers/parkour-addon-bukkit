package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.commons.storage.SQLProvider;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import io.github.a5h73y.parkour.type.player.PlayerInfo;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ParkourLevel {
	private static class ParkourLevelSetThread extends Thread {
		private final Player player;
		private final int level;

		ParkourLevelSetThread(final Player player, final int level) {
			this.player = player;
			this.level = level;
		}

		public void run() {
			// Update the player's parkour level
			try {
				PreparedStatement query = ParkourAddonPlugin.plugin.getConnection().prepareStatement(Utils.SET_PLAYER_PARKOUR_LEVEL_QUERY);
				query.setInt(1, level);
				query.setBytes(2, Utils.getBytesFromUniqueId(player.getUniqueId()));
				query.executeUpdate();
			} catch(SQLException e) {
				Log.severe("Cannot update player parkour level.", e);
			}
		}
	}

	public static int getLevel(Player player) {
		return PlayerInfo.getParkourLevel(player);
	}

	public static void setLevel(Player player, int level) {
		Log.info("Setting player " + player.getName() + " level to " + level + ".");
		PlayerInfo.setParkourLevel(player, level);
		(new ParkourLevelSetThread(player, level)).run();
	}
}
