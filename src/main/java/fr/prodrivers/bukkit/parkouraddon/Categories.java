package fr.prodrivers.bukkit.parkouraddon;

import io.ebean.SqlRow;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class Categories {
	private static HashMap<Integer, ParkourCategory> categories = new HashMap<>();
	private static HashMap<Integer, Integer> numberOfCategories = new HashMap<>();

	public static int getNumberOfCompletedCoursesInCategory( UUID playerUniqueId, ParkourCategory category ) {
		SqlRow row = ParkourAddonPlugin.database
				.createSqlQuery( "SELECT COUNT( courseId ) FROM parkourplayercompletion NATURAL JOIN course GROUP BY playeruuid, categoryId HAVING playeruuid = :playeruuid AND categoryId = :categoryid;" )
				.setParameter( "playeruuid", Utils.getBytesFromUniqueId( playerUniqueId ) )
				.setParameter( "categoryid", category.getCategoryId() )
				.findOne();
		return row.getInteger( "count( courseid )" );

		/*// Retrieve player's completed courses
		ParkourPlayerCompletion present = ParkourPlayerCompletion.retrieve( ParkourAddonPlugin.database, Utils.getBytesFromUniqueId( playerUniqueId ), category );
		if( present != null ) { // If the player has already completed courses in this category
			// Return the number of completed courses
			return present.getNumber();
		} else {
			// Return 0 as the player hasn't completed any courses in this category
			return 0;
		}*/
	}

	static int getNumberOfCoursesInCategory( ParkourCategory category ) {
		// Get the Category ID
		int cid = category.getCategoryId();

		if( numberOfCategories.containsKey( cid ) ) { // If the number of courses for this category is already cached
			// Return the cached number
			return numberOfCategories.get( cid );
		} else {
			// Retrieve the number from the database
			int nb = ParkourAddonPlugin.database.createQuery( ParkourCourse.class ).setUseQueryCache( true ).where().eq( "categoryId", cid ).findCount();

			// Store it inthe cache
			numberOfCategories.put( cid, nb );

			//Return it
			return nb;
		}
	}

	static void reload() {
		// Clear the cache on configuration reload
		numberOfCategories.clear();
		categories.clear();
	}

	private static void generateCategories() {
		categories.clear();
		for( ParkourCategory category : ParkourAddonPlugin.database.find( ParkourCategory.class ).select( "*" ).findList() ) {
			categories.put( category.getCategoryId(), category );
		}
	}

	public static Collection<ParkourCategory> retrieveAll() {
		if( categories.isEmpty() ) {
			generateCategories();
		}
		return categories.values();
	}

	public static ParkourCategory retrieve( int id ) {
		if( categories.isEmpty() ) {
			generateCategories();
		}
		return categories.get( id );
	}
}
