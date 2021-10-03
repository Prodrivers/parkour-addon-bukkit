package fr.prodrivers.bukkit.parkouraddon.sections;

import fr.prodrivers.bukkit.commons.parties.PartyManager;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
import io.github.a5h73y.parkour.type.course.CourseInfo;
import io.github.a5h73y.parkour.type.course.CourseManager;
import io.github.a5h73y.parkour.type.player.PlayerManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParkourSectionManager {
	private final PlayerManager playerManager;
	private final CourseManager courseManager;
	private final SectionManager sectionManager;
	private final PartyManager partyManager;
	private final Database database;
	private final EMessages messages;
	private final EChat chat;
	private final ParkourLevel parkourLevel;

	@Inject
	public ParkourSectionManager(PlayerManager playerManager, CourseManager courseManager, SectionManager sectionManager, PartyManager partyManager, Database database, EMessages messages, EChat chat, ParkourLevel parkourLevel) {
		this.playerManager = playerManager;
		this.courseManager = courseManager;
		this.sectionManager = sectionManager;
		this.partyManager = partyManager;
		this.database = database;
		this.messages = messages;
		this.chat = chat;
		this.parkourLevel = parkourLevel;
	}

	public void load() {
		Log.info("Loading parkour sections...");
		for(String courseName : CourseInfo.getAllCourseNames()) {
			if(this.sectionManager.getSection(ParkourSection.NAME_PREFIX + courseName) == null) {
				Log.fine("Creating section for parkour : " + courseName);
				this.sectionManager.register(new ParkourSection(this.partyManager, this.playerManager, this.courseManager, courseName, this.database, this.messages, this.chat, this.parkourLevel), true);
			}
		}
		Log.info("Loaded parkour sections.");
	}
}
