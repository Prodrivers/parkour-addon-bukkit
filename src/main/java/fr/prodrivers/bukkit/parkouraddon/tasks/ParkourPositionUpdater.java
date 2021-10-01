package fr.prodrivers.bukkit.parkouraddon.tasks;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import io.ebean.Database;
import org.bukkit.Location;

import java.util.List;

public class ParkourPositionUpdater implements Runnable {
	private final Database database;
	private final Course course;

	public ParkourPositionUpdater(Database database, Course course) {
		this.database = database;
		this.course = course;
	}

	public void run() {
		List<ParkourCourse> courses = ParkourCourse.retrieveAll(this.database);

		for(ParkourCourse course : courses) {
			Location location = this.course.getLocation(course.getName());

			if(location != null && location.getWorld() != null) {
				course.setPositionX(location.getX());
				course.setPositionY(location.getY());
				course.setPositionZ(location.getZ());
				course.setPositionWorld(location.getWorld().getName());
			} else {
				Log.warning("Course " + course.getName() + " has no checkpoint, hence cannot get its location and update its position.");
			}
		}

		this.database.saveAll(courses);
	}
}
