package fr.prodrivers.bukkit.parkouraddon.adaptation;

import io.github.a5h73y.parkour.type.course.CourseInfo;

public class Course {
	static public void setMinimumLevel( String courseName, int level ) {
		CourseInfo.setMinimumParkourLevel( courseName, level );
	}
}