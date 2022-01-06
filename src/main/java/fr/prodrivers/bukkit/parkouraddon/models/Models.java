package fr.prodrivers.bukkit.parkouraddon.models;

import java.util.ArrayList;
import java.util.List;

public class Models {
	public final static List<Class<?>> ModelsList = new ArrayList<>();

	public static void populate() {
		ModelsList.clear();
		ModelsList.add(EStoredPlayer.class);
		ModelsList.add(ParkourCourse.class);
		ModelsList.add(ParkourCategory.class);
		ModelsList.add(ParkourPlayerCompletion.class);
		ModelsList.add(ParkourTimeRanked.class);
	}
}
