package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.commons.storage.SQLProvider;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import me.A5H73Y.parkour.player.PlayerInfo;
import me.A5H73Y.parkour.player.PlayerMethods;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class Parkoins {
	private static class ParkoinsSetThread extends Thread {
		private final Player player;
		private final int parkoins;

		ParkoinsSetThread( final Player player, final int parkoins ) {
			this.player = player;
			this.parkoins = parkoins;
		}

		public void run() {
			// Update the player's parkoins
			try {
				PreparedStatement query = SQLProvider.getConnection().prepareStatement( Utils.SET_PLAYER_PARKOINS_QUERY );
				query.setInt( 1, parkoins );
				query.setBytes( 2, Utils.getBytesFromUniqueId( player.getUniqueId() ) );
				query.executeUpdate();
			} catch( SQLException e ) {
				ParkourAddonPlugin.logger.log( Level.SEVERE, "Error while updating player parkoins : " + e.getLocalizedMessage(), e );
			}
		}
	}

	public static int get( Player player ) {
		return PlayerInfo.getParkoins( player );
	}

	public static void add( Player player, int parkoins ) {
		PlayerMethods.rewardParkoins( player, parkoins );

		final int balance = get( player );
		( new ParkoinsSetThread( player, balance ) ).run();
	}

	public static void remove( Player player, int parkoins ) {
		PlayerMethods.deductParkoins( player, parkoins );

		final int balance = get( player );
		( new ParkoinsSetThread( player, balance ) ).run();
	}
}
