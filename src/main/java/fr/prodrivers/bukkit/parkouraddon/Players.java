package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.advancements.AdvancementManager;
import fr.prodrivers.bukkit.parkouraddon.events.PlayerCompleteCourseEvent;
import fr.prodrivers.bukkit.parkouraddon.events.PlayerRankUpEvent;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourPlayerCompletion;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourSelection;
import fr.prodrivers.bukkit.parkouraddon.ui.PlayerUI;
import io.ebean.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;

public class Players {
	private final JavaPlugin plugin;
	private final Database database;
	private final PlayerUI playerUI;
	private final ParkourSelection parkourSelection;
	private final ParkourLevel parkourLevel;
	private final Parkoins parkoins;
	private final AdvancementManager advancementManager;

	@Inject
	public Players(JavaPlugin plugin, Database database, PlayerUI playerUI, ParkourSelection parkourSelection, ParkourLevel parkourLevel, Parkoins parkoins, AdvancementManager advancementManager) {
		this.plugin = plugin;
		this.database = database;
		this.playerUI = playerUI;
		this.parkourSelection = parkourSelection;
		this.parkourLevel = parkourLevel;
		this.parkoins = parkoins;
		this.advancementManager = advancementManager;
	}

	public void insertCompletion(final Player player, final ParkourCourse course) {
		if(course != null) { // If the course exists
			// Get a byte array from the player's UUID
			byte[] playerUuid = Utils.getBytesFromUniqueId(player.getUniqueId());

			// Search for an already present entry
			ParkourPlayerCompletion present = ParkourPlayerCompletion.retrieve(this.database, playerUuid, course);

			if(present == null) { // If no completion was registered for this course and this player
				// Create a new completion in the database
				ParkourPlayerCompletion completion = new ParkourPlayerCompletion();

				completion.setCourse(course);
				completion.setPlayerUniqueId(playerUuid);

				// Insert it
				this.database.save(completion);

				System.out.println("[ParkourAddon] Player " + player.getName() + " completed course " + course.getName());

				// Clear UI if necessary
				if(course.getCategory() != null) {
					this.parkourSelection.reload(player, course.getCategory().getCategoryId());
				}

				// Reward the player if necessary and trigger event
				Bukkit.getScheduler().runTask(this.plugin, () -> {
					// Add the category parkoins reward to the player, if possible
					if(course.getCategory() != null) {
						int parkoinsReward = course.getCategory().getParkoinsReward();
						this.parkoins.add(player, parkoinsReward);
					}

					// Do some stuff to inform him
					this.playerUI.courseCompleted(player, course);

					// Trigger event
					this.plugin.getServer().getPluginManager().callEvent(new PlayerCompleteCourseEvent(player, course));
				});
			}
		} else {
			Bukkit.getScheduler().runTask(this.plugin, () -> Log.severe("Player " + player.getName() + " completed a course not present in the database."));
		}
	}

	public void rankPlayer(final Player player, ParkourCourse course, int playerLevel) {
		boolean hasRankedUp = false;
		if(course != null && course.getCategory() != null) { // If the course exists
			if(course.getCategory().getNextCategories() != null) { // If the course has a next category
				// Get number of completed course in the course's category for this player
				int completed = course.getCategory().getNumberOfCompletedCourses(this.database, player.getUniqueId());
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

							Bukkit.getScheduler().runTask(this.plugin, () -> {
								// Set the player's new level
								this.parkourLevel.setLevel(player, nextLevel);

								// Do some stuff to inform him
								this.playerUI.rankUp(player, nextLevel);

								// Trigger event
								this.plugin.getServer().getPluginManager().callEvent(new PlayerRankUpEvent(player, nextLevel));
							});
						}
					}
				}
			}
		} else {
			Bukkit.getScheduler().runTask(this.plugin, () -> Log.severe("Player " + player.getName() + " completed a course not present in the database."));
		}

		// If player has ranked up
		if(hasRankedUp) {
			Bukkit.getScheduler().runTask(this.plugin, () -> {
				try {
					// Grant him the corresponding criteria
					this.advancementManager.grant(player, course.getCategory());
				} catch(Exception e) {
					Log.severe("Error on advancement criteria grant of category " + course.getCategory().getCategoryId() + " for " + player.getName());
				}
			});
		}
	}

	public void insertCompletionAndRankAsync(final Player player, final String courseName) {
		// Get the player's level synchronously
		final int playerLevel = this.parkourLevel.getLevel(player);

		// Run the whole thing asynchronously
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			// Get the associated course
			ParkourCourse course = ParkourCourse.retrieveFromName(this.database, courseName);

			insertCompletion(player, course);
			rankPlayer(player, course, playerLevel);
		});
	}
}
