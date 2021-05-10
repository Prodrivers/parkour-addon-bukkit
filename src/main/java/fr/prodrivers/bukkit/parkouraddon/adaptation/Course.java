package fr.prodrivers.bukkit.parkouraddon.adaptation;

import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.course.CourseInfo;
import org.bukkit.Location;

public class Course {
	static public void setMinimumLevel( String courseName, int level ) {
		CourseInfo.setMinimumParkourLevel( courseName, level );
	}

	static public Location getLocation( String courseName ) {
		io.github.a5h73y.parkour.type.course.Course course = Parkour.getInstance().getCourseManager().findCourse( courseName );
		if( course == null ) {
			return null;
		}
		return course.getCheckpoints().get( 0 ).getLocation();
	}
}