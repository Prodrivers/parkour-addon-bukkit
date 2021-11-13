package fr.prodrivers.bukkit.parkouraddon.adaptation;

import fr.prodrivers.bukkit.commons.exceptions.IllegalSectionEnteringException;
import fr.prodrivers.bukkit.commons.exceptions.InvalidSectionException;
import fr.prodrivers.bukkit.commons.exceptions.NotPartyOwnerException;
import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import fr.prodrivers.bukkit.parkouraddon.sections.ParkourSection;
import io.ebean.Database;
import io.github.a5h73y.parkour.type.course.CourseInfo;
import io.github.a5h73y.parkour.type.course.CourseManager;
import io.github.a5h73y.parkour.type.player.ParkourSession;
import io.github.a5h73y.parkour.type.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Course {
	private final Plugin plugin;
	private final Database database;
	private final PlayerManager playerManager;
	private final CourseManager courseManager;
	private final EMessages messages;
	private final EChat chat;
	private final SectionManager sectionManager;

	private int finishWaitIterations = 0;

	@Inject
	public Course(Plugin plugin, Database database, PlayerManager playerManager, CourseManager courseManager, EMessages messages, EChat chat, SectionManager sectionManager) {
		this.plugin = plugin;
		this.database = database;
		this.playerManager = playerManager;
		this.courseManager = courseManager;
		this.messages = messages;
		this.chat = chat;
		this.sectionManager = sectionManager;
	}

	public void setMinimumLevel(String courseName, int level) {
		CourseInfo.setMinimumParkourLevel(courseName, level);
	}

	public Location getLocation(String courseName) {
		io.github.a5h73y.parkour.type.course.Course course = this.courseManager.findCourse(courseName);
		if(course == null) {
			return null;
		}
		return course.getCheckpoints().get(0).getLocation();
	}

	public boolean join(Player player, String name) {
		try {
			ParkourSession session = this.playerManager.getParkourSession(player);
			if(session != null && name.equals(session.getCourseName())) {
				Log.finest("Player is in a parkour session and same course, ignoring.");
				// We return true to not fail the check
				return true;
			}

			this.sectionManager.enter(player, ParkourSection.NAME_PREFIX + name);
			return true;
		} catch(InvalidSectionException e) {
			this.chat.error(player, this.messages.invalidcourse);
		} catch(NotPartyOwnerException e) {
			this.chat.error(player, this.messages.cannotjoinnotpartyowner);
		} catch(IllegalSectionEnteringException e) {
			this.chat.error(player, this.messages.error_occurred);
		}
		return false;
	}

	public boolean joinAll(Player originator, String name) {
		name = name.trim();

		ParkourCourse course = ParkourCourse.retrieveFromName(this.database, name);
		if(course == null) {
			this.chat.error(originator, this.messages.invalidcourse);
			return false;
		}

		for(Player player : Bukkit.getOnlinePlayers()) {
			join(player, name);
		}
		return false;
	}

	public boolean leave(Player player) {
		try {
			Section section = this.sectionManager.getCurrentSection(player);
			if(section != null && section.getFullName().startsWith(ParkourSection.NAME_PREFIX)) {
				this.sectionManager.enter(player);
			}
			return true;
		} catch(Exception e) {
			this.chat.error(player, this.messages.error_occurred);
			Log.severe("Error when leaving parkour.", e);
		}
		return false;
	}

	public boolean finishWait(Player player) {
		try {
			finishWaitIterations = 0;
			Bukkit.getScheduler().runTaskTimer(this.plugin, (bukkitTask) -> {
				Log.fine("Waiting for parkour to finish session...");
				if(this.playerManager.getParkourSession(player) == null) {
					Log.fine("Parkour session finished.");
					this.leave(player);
					bukkitTask.cancel();
				}
				if(finishWaitIterations > 50) {
					finishWaitIterations = 0;
					Log.severe("Waiting for parkour session to be cleaned took too long, cancelling.");
					bukkitTask.cancel();
				}
				finishWaitIterations++;
			}, 0, 1);
			return true;
		} catch(Exception e) {
			this.chat.error(player, this.messages.error_occurred);
			Log.severe("Error when leaving parkour.", e);
		}
		return false;
	}
}