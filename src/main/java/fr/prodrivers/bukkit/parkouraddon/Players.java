package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.exceptions.NotPartyOwnerException;
import fr.prodrivers.bukkit.commons.parties.Party;
import fr.prodrivers.bukkit.commons.parties.PartyManager;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.events.PlayerCompleteCourseEvent;
import fr.prodrivers.bukkit.parkouraddon.events.PlayerRankUpEvent;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourPlayerCompletion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

class Players {
	static void insertCompletion( final Player player, final ParkourCourse course ) {
		if( course != null ) { // If the course exists
			// Get a byte array from the player's UUID
			byte[] playerUuid = Utils.getBytesFromUniqueId( player.getUniqueId() );

			// Search for an already present entry
			ParkourPlayerCompletion present = ParkourPlayerCompletion.retrieve( ParkourAddonPlugin.database, playerUuid, course );

			if( present == null ) { // If no completion was registered for this course and this player
				// Create a new completion in the database
				ParkourPlayerCompletion completion = new ParkourPlayerCompletion();

				completion.setCourse( course );
				completion.setPlayerUniqueId( playerUuid );

				// Insert it
				ParkourAddonPlugin.database.save( completion );

				System.out.println( "[ParkourAddon] Player " + player.getName() + " completed course " + course.getName() );

				// Clear UI if necessary
				if( course.getCategory() != null ) {
					ParkourSelectionUI.reload( player, course.getCategory().getCategoryId() );
				}

				// Reward the player if necessary and trigger event
				Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> {
					// Add the category parkoins reward to the player, if possible
					if( course.getCategory() != null ) {
						int parkoinsReward = course.getCategory().getParkoinsReward();
						Parkoins.add( player, parkoinsReward );
					}

					// Trigger event
					ParkourAddonPlugin.plugin.getServer().getPluginManager().callEvent(new PlayerCompleteCourseEvent( player, course ) );
				});
			}
		} else {
			Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> ParkourAddonPlugin.logger.severe( "Player " + player.getName() + " completed a course not present in the database." ) );
		}
	}

	static void rankPlayer( final Player player, ParkourCourse course, final int playerLevel ) {
		if( course != null && course.getCategory() != null ) { // If the course exists
			if( course.getCategory().getNextCategory() != null ) { // If the course has a next category
				// If the player has an inferior level to the next category's base level
				ParkourCategory nextCat = course.getCategory().forceGetNextCategory( ParkourAddonPlugin.database );
				final int nextLevel = nextCat.getBaseLevel();
				if( nextLevel > playerLevel ) {
					// Get number of completed course in the course's category for this player
					int completed = Categories.getNumberOfCompletedCoursesInCategory( player.getUniqueId(), course.getCategory() );
					// Get the required number of courses in this category
					int required = course.getCategory().getRequiredCoursesNumberRankup();

					System.out.println( "[ParkourAddon] Player " + player.getName() + " completed " + completed + " courses in category " + course.getCategory().getCategoryId() );

					if( completed >= required ) { // If the player has completed the required number of courses courses
						// Woohoo ! The player ranks up !

						System.out.println( "[ParkourAddon] Player " + player.getName() + " ranked up to level " + nextLevel );

						Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> {
							// Set the player's new level
							ParkourLevel.setLevel( player, nextLevel );

							// Do some stuff to inform him
							UI.rankUp( player, nextLevel );

							// Trigger event
							ParkourAddonPlugin.plugin.getServer().getPluginManager().callEvent( new PlayerRankUpEvent( player, nextLevel ) );
						});
					}
				}
			}
		} else {
			Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> ParkourAddonPlugin.logger.severe( "Player " + player.getName() + " completed a course not present in the database." ) );
		}
	}

	static void insertCompletionAndRankAsync( final Player player, final String courseName ) {
		// Get the player's level synchronously
		final int playerLevel = ParkourLevel.getLevel( player );

		// Run the whole thing asynchronously
		//( new CompletionRankThread( player, playerLevel, courseName, playerRewarded ) ).start();
		Bukkit.getScheduler().runTaskAsynchronously( ParkourAddonPlugin.plugin, new Runnable() {
			@Override
			public void run() {
				// Get the associated course
				ParkourCourse course = ParkourCourse.retrieveFromName( ParkourAddonPlugin.database, courseName );

				insertCompletion( player, course );
				rankPlayer( player, course, playerLevel );
			}
		});
	}

	public static void joinParkour( Player player, String name ) {
		Party party = PartyManager.getParty( player.getUniqueId() );
		if( party != null ) {
			ParkourCourse course = ParkourCourse.retrieveFromName( ParkourAddonPlugin.database, name );
			if( course == null ) {
				ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.invalidcourse );
				return;
			}
			ParkourCategory category = course.getCategory();
			for( UUID partyPlayerUUID : party.getPlayers() ) {
				Player partyPlayer = Bukkit.getPlayer( partyPlayerUUID );
				System.out.println(partyPlayer);
				if( partyPlayer != null ) {
					int level = ParkourLevel.getLevel( player );
					System.out.println(level);
					System.out.println(category.getBaseLevel());
					if( level < category.getBaseLevel() ) {
						party.broadcast( ParkourAddonPlugin.chat, ParkourAddonPlugin.messages.party_notenoughlevel );
						return;
					}
				}
			}
		}
		try {
			SectionManager.enter( player, "parkour", name );
		} catch( NotPartyOwnerException e ) {
			ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.cannotjoinnotpartyowner );
		}
	}

	public static void leaveParkour( Player player ) {
		SectionManager.enter( player, "main" );
	}
}
