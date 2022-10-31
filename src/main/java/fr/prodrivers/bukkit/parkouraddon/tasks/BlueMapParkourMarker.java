package fr.prodrivers.bukkit.parkouraddon.tasks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
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
			BlueMapAPI.onEnable(this::load);
		} catch(NoClassDefFoundError e) {
			Log.warning("Cannot create BlueMap markers as plugin is not installed.");
		}
	}

	public void load(BlueMapAPI api) {
		try {
			for(World world: Bukkit.getWorlds()) {
				MarkerSet set = MarkerSet.builder()
						.label(this.messages.bluemap_parkours_label)
						.toggleable(true)
						.defaultHidden(true)
						.build();

				api.getWorld(world).ifPresent(blueMapWorld -> {
					for(ParkourCourse course : ParkourCourse.retrieveForWorld(this.database, world.getName())) {
						createMarker(set, course);
					}

					if(!set.getMarkers().isEmpty()) {
						for(BlueMapMap map : blueMapWorld.getMaps()) {
							map.getMarkerSets().put("parkours-" + world.getName(), set);
						}
					}

					Log.info("Created " + set.getMarkers().size() + " BlueMap markers for world " + world.getName());
				});
			}
		} catch(Exception e) {
			Log.severe("Could not generate markers for BlueMap.", e);
		}
	}

	public void createMarker(MarkerSet set, ParkourCourse course) {
		ParkourCategory category = course.getCategory();

		Location location = this.course.getLocation(course.getName());
		if(location == null || location.getWorld() == null) {
			Log.warning("Course " + course.getName() + " has no checkpoint, hence cannot get its location and update its marker.");
			return;
		}

		// Do not show courses that do not have a display name
		if(course.getDisplayName() != null) {
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
			HtmlMarker marker = HtmlMarker.builder()
					.label(id)
					.position((int) Math.round(location.getX()), (int) Math.round(location.getY()),  (int) Math.round(location.getZ()))
					.html(html)
					.build();

			set.getMarkers().put(course.getName(), marker);

			Log.fine("Created BlueMap marker for course " + course);
		} else {
			Log.warning("Course " + course + " does not have a display name, ignoring.");
		}
	}
}
