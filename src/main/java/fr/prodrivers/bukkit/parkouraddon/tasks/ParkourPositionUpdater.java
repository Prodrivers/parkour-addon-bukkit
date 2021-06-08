package fr.prodrivers.bukkit.parkouraddon.tasks;

import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import org.bukkit.Location;

import java.util.List;

public class ParkourPositionUpdater implements Runnable {
	public void run() {
		List<ParkourCourse> courses = ParkourCourse.retrieveAll(ParkourAddonPlugin.database);

		for(ParkourCourse course : courses) {
			Location location = Course.getLocation(course.getName());
			assert location != null;
			assert location.getWorld() != null;

			course.setPositionX(location.getX());
			course.setPositionY(location.getY());
			course.setPositionZ(location.getZ());
			course.setPositionWorld(location.getWorld().getName());
		}

		ParkourAddonPlugin.database.saveAll(courses);
	}
}