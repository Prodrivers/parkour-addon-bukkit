package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;

class Courses {
	static void setCategory( ParkourCourse course, ParkourCategory category ) {
		if( category == null ) {
			Course.setMinimumLevel( course.getName(), -1 );
		} else {
			Course.setMinimumLevel( course.getName(), category.getBaseLevel() );
		}
		course.setCategory( category );
		ParkourAddonPlugin.database.save( course );
	}
}