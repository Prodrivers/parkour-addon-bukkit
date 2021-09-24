package fr.prodrivers.bukkit.parkouraddon.tasks;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import org.bukkit.Location;

import java.util.List;

public class ParkourPositionUpdater implements Runnable {
	public void run() {
		List<ParkourCourse> courses = ParkourCourse.retrieveAll(ParkourAddonPlugin.plugin.getDatabase());

		for(ParkourCourse course : courses) {
			Location location = Course.getLocation(course.getName());

			if(location != null && location.getWorld() != null) {
				course.setPositionX(location.getX());
				course.setPositionY(location.getY());
				course.setPositionZ(location.getZ());
				course.setPositionWorld(location.getWorld().getName());
			} else {
				Log.warning("Course " + course.getName() + " has no checkpoint, hence cannot get its location and update its position.");
			}
		}

		ParkourAddonPlugin.plugin.getDatabase().saveAll(courses);
	}
}
