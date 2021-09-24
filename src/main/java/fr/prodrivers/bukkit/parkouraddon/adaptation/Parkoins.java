package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import io.ebean.SqlUpdate;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.player.PlayerInfo;
import org.bukkit.entity.Player;

public class Parkoins {
	private static class ParkoinsSetThread extends Thread {
		private final Player player;
		private final double parkoins;

		ParkoinsSetThread(final Player player, final double parkoins) {
			this.player = player;
			this.parkoins = parkoins;
		}

		public void run() {
			// Update the player's parkoins
			SqlUpdate query = ParkourAddonPlugin.plugin.getDatabase().sqlUpdate(Utils.SET_PLAYER_PARKOINS_QUERY);
			query.setParameter(1, parkoins);
			query.setParameter(2, Utils.getBytesFromUniqueId(player.getUniqueId()));
			if(query.execute() == 0) {
				Log.severe("Cannot update player parkoins.");
			}
		}
	}

	public static double get(Player player) {
		return PlayerInfo.getParkoins(player);
	}

	public static void add(Player player, int parkoins) {
		Parkour.getInstance().getPlayerManager().rewardParkoins(player, parkoins);

		final double balance = get(player);
		(new ParkoinsSetThread(player, balance)).run();
	}

	public static void remove(Player player, int parkoins) {
		Parkour.getInstance().getPlayerManager().deductParkoins(player, parkoins);

		final double balance = get(player);
		(new ParkoinsSetThread(player, balance)).run();
	}
}
