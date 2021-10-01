package fr.prodrivers.bukkit.parkouraddon.tasks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import io.ebean.Database;
import org.bukkit.Location;

import java.util.Optional;

public class BlueMapParkourMarker implements Runnable {
	public static final String PARKOUR_MARKER_SET = "parkours";

	private final EMessages messages;
	private final Database database;
	private final Course course;

	public BlueMapParkourMarker(EMessages messages, Database database, Course course) {
		this.messages = messages;
		this.database = database;
		this.course = course;
	}

	public void run() {
		try {
			BlueMapAPI.getInstance().ifPresent(api -> {
				try {
					MarkerAPI markerApi = api.getMarkerAPI();

					markerApi.load();

					markerApi.removeMarkerSet(PARKOUR_MARKER_SET);
					MarkerSet set = markerApi.createMarkerSet(PARKOUR_MARKER_SET);

					set.setLabel(this.messages.bluemap_parkours_label);
					set.setToggleable(true);
					set.setDefaultHidden(true);

					for(ParkourCourse course : ParkourCourse.retrieveAll(this.database)) {
						createMarker(api, set, course);
					}

					markerApi.save();

					Log.info(set.getMarkers().size() + " markers generated for BlueMap.");
				} catch(Exception e) {
					Log.severe("Could not generate markers for BlueMap.", e);
				}
			});
		} catch(NoClassDefFoundError e) {
			Log.warning("Cannot create BlueMap markers as plugin is not installed.");
		}
	}

	public void createMarker(BlueMapAPI api, MarkerSet set, ParkourCourse course) {
		ParkourCategory category = course.getCategory();

		Location location = this.course.getLocation(course.getName());
		if(location == null || location.getWorld() == null) {
			Log.warning("Course " + course.getName() + " has no checkpoint, hence cannot get its location and update its marker.");
			return;
		}

		Optional<BlueMapMap> map = api.getMap(location.getWorld().getName());

		// Do not show maps that do not have a display name
		if(map.isPresent() && course.getDisplayName() != null) {
			String id;
			String html;
			if(category != null) {
				if(category.isHidden()) {
					return;
				}
				id = String.format(
						this.messages.bluemap_parkours_markers_withcategory_id,
						category.getName(),
						course.getDisplayName()
				);
				html = String.format(
						this.messages.bluemap_parkours_markers_withcategory_html,
						Integer.toHexString(category.getHexColor()),
						(Utils.isColorLight(category.getHexColor()) ? "#000" : "#fff"),
						category.getBaseLevel(),
						course.getDisplayName()
				);
			} else {
				id = String.format(
						this.messages.bluemap_parkours_markers_nocategory_id,
						course.getDisplayName()
				);
				html = String.format(
						this.messages.bluemap_parkours_markers_nocategory_html,
						course.getDisplayName()
				);
			}
			set.createHtmlMarker(
					id,
					map.get(),
					location.getX(),
					location.getY(),
					location.getZ(),
					html
			);
		} else {
			Log.warning("BlueMap map " + location.getWorld().getName() + " does not exists.");
		}
	}
}
