package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.exceptions.IllegalSectionEnteringException;
import fr.prodrivers.bukkit.commons.exceptions.InvalidSectionException;
import fr.prodrivers.bukkit.commons.exceptions.NotPartyOwnerException;
import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.advancements.AdvancementManager;
import fr.prodrivers.bukkit.parkouraddon.events.PlayerCompleteCourseEvent;
import fr.prodrivers.bukkit.parkouraddon.events.PlayerRankUpEvent;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourPlayerCompletion;
import fr.prodrivers.bukkit.parkouraddon.sections.ParkourSection;
import io.github.a5h73y.parkour.type.player.ParkourSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class Players {
	static void insertCompletion(final Player player, final ParkourCourse course) {
		if(course != null) { // If the course exists
			// Get a byte array from the player's UUID
			byte[] playerUuid = Utils.getBytesFromUniqueId(player.getUniqueId());

			// Search for an already present entry
			ParkourPlayerCompletion present = ParkourPlayerCompletion.retrieve(ParkourAddonPlugin.database, playerUuid, course);

			if(present == null) { // If no completion was registered for this course and this player
				// Create a new completion in the database
				ParkourPlayerCompletion completion = new ParkourPlayerCompletion();

				completion.setCourse(course);
				completion.setPlayerUniqueId(playerUuid);

				// Insert it
				ParkourAddonPlugin.database.save(completion);

				System.out.println("[ParkourAddon] Player " + player.getName() + " completed course " + course.getName());

				// Clear UI if necessary
				if(course.getCategory() != null) {
					ParkourSelectionUI.reload(player, course.getCategory().getCategoryId());
				}

				// Reward the player if necessary and trigger event
				Bukkit.getScheduler().runTask(ParkourAddonPlugin.plugin, () -> {
					// Add the category parkoins reward to the player, if possible
					if(course.getCategory() != null) {
						int parkoinsReward = course.getCategory().getParkoinsReward();
						Parkoins.add(player, parkoinsReward);
					}

					// Do some stuff to inform him
					UI.courseCompleted(player, course);

					// Trigger event
					ParkourAddonPlugin.plugin.getServer().getPluginManager().callEvent(new PlayerCompleteCourseEvent(player, course));
				});
			}
		} else {
			Bukkit.getScheduler().runTask(ParkourAddonPlugin.plugin, () -> Log.severe("Player " + player.getName() + " completed a course not present in the database."));
		}
	}

	static void rankPlayer(final Player player, ParkourCourse course, int playerLevel) {
		boolean hasRankedUp = false;
		if(course != null && course.getCategory() != null) { // If the course exists
			if(course.getCategory().getNextCategories() != null) { // If the course has a next category
				// Get number of completed course in the course's category for this player
				int completed = course.getCategory().getNumberOfCompletedCourses(player.getUniqueId());
				// If the player has an inferior level to the next category's base level
				for(ParkourCategory nextCat : course.getCategory().getNextCategories()) {
					// Get the next category's base level
					final int nextLevel = nextCat.getBaseLevel();

					if(nextLevel > playerLevel) { // If the player has a lower level than the next category
						// Get the required number of courses in this category
						int required = nextCat.getRequiredCoursesNumberInPreviousCategoryForRankup();

						if(completed >= required) { // If the player has completed the required number of courses courses
							// Woohoo ! The player ranks up !

							hasRankedUp = true;

							System.out.println("[ParkourAddon] Player " + player.getName() + " ranked up to level " + nextLevel);

							// Locally set new level to be considered for other iterations with other next categories
							playerLevel = nextLevel;

							Bukkit.getScheduler().runTask(ParkourAddonPlugin.plugin, () -> {
								// Set the player's new level
								ParkourLevel.setLevel(player, nextLevel);

								// Do some stuff to inform him
								UI.rankUp(player, nextLevel);

								// Trigger event
								ParkourAddonPlugin.plugin.getServer().getPluginManager().callEvent(new PlayerRankUpEvent(player, nextLevel));
							});
						}
					}
				}
			}
		} else {
			Bukkit.getScheduler().runTask(ParkourAddonPlugin.plugin, () -> Log.severe("Player " + player.getName() + " completed a course not present in the database."));
		}

		// If player has ranked up
		if(hasRankedUp) {
			Bukkit.getScheduler().runTask(ParkourAddonPlugin.plugin, () -> {
				try {
					// Grant him the corresponding criteria
					AdvancementManager.grant(player, course.getCategory());
				} catch(Exception e) {
					Log.severe("Error on advancement criteria grant of category " + course.getCategory().getCategoryId() + " for " + player.getName());
				}
			});
		}
	}

	static void insertCompletionAndRankAsync(final Player player, final String courseName) {
		// Get the player's level synchronously
		final int playerLevel = ParkourLevel.getLevel(player);

		// Run the whole thing asynchronously
		Bukkit.getScheduler().runTaskAsynchronously(ParkourAddonPlugin.plugin, () -> {
			// Get the associated course
			ParkourCourse course = ParkourCourse.retrieveFromName(ParkourAddonPlugin.database, courseName);

			insertCompletion(player, course);
			rankPlayer(player, course, playerLevel);
		});
	}

	public static boolean joinParkour(Player player, String name) {
		try {
			ParkourSession session = ParkourAddonPlugin.plugin.getParkour().getPlayerManager().getParkourSession(player);
			if(session != null && name.equals(session.getCourseName())) {
				Log.finest("Player is in a parkour session and same course, ignoring.");
				// We return true to not fail the check
				return true;
			}

			ParkourAddonPlugin.plugin.getSectionManager().enter(player, ParkourSection.NAME_PREFIX + name);
			return true;
		} catch(InvalidSectionException e) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.invalidcourse);
		} catch(NotPartyOwnerException e) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.cannotjoinnotpartyowner);
		} catch(IllegalSectionEnteringException e) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.errorocurred);
		}
		return false;
	}

	public static boolean joinParkourAll(Player originator, String name) {
		name = name.trim();

		ParkourCourse course = ParkourCourse.retrieveFromName(ParkourAddonPlugin.database, name);
		if(course == null) {
			ParkourAddonPlugin.chat.error(originator, ParkourAddonPlugin.messages.invalidcourse);
			return false;
		}

		for(Player player : Bukkit.getOnlinePlayers()) {
			joinParkour(player, name);
		}
		return false;
	}

	public static boolean leaveParkour(Player player) {
		try {
			SectionManager sectionManager = ParkourAddonPlugin.plugin.getSectionManager();
			Section section = sectionManager.getCurrentSection(player);
			if(section != null && section.getFullName().startsWith(ParkourSection.NAME_PREFIX)) {
				sectionManager.enter(player);
			}
			return true;
		} catch(Exception e) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.errorocurred);
			Log.severe("Error when leaving parkour.", e);
		}
		return false;
	}
}
