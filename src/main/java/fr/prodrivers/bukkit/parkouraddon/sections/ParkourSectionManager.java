package fr.prodrivers.bukkit.parkouraddon.sections;

import fr.prodrivers.bukkit.commons.parties.PartyManager;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.parkouraddon.Log;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.course.CourseInfo;

public class ParkourSectionManager {
	private final Parkour parkour;
	private final SectionManager sectionManager;
	private final PartyManager partyManager;

	public ParkourSectionManager(Parkour parkour, SectionManager sectionManager, PartyManager partyManager) {
		this.parkour = parkour;
		this.sectionManager = sectionManager;
		this.partyManager = partyManager;
	}

	public void load() {
		Log.info("Loading parkour sections...");
		for(String courseName : CourseInfo.getAllCourseNames()) {
			if(this.sectionManager.getSection(ParkourSection.NAME_PREFIX + courseName) == null) {
				Log.fine("Creating section for parkour : " + courseName);
				this.sectionManager.register(new ParkourSection(this.partyManager, this.parkour, courseName), true);
			}
		}
		Log.info("Loaded parkour sections.");
	}
}
