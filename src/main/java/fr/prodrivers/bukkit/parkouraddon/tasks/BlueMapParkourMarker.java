package fr.prodrivers.bukkit.parkouraddon.tasks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import org.bukkit.Location;

import java.util.Optional;

public class BlueMapParkourMarker implements Runnable {
	public static final String PARKOUR_MARKER_SET = "parkours";

	public void run() {
		try {
			BlueMapAPI.getInstance().ifPresent( api -> {
				try {
					MarkerAPI markerApi = api.getMarkerAPI();

					markerApi.load();

					markerApi.removeMarkerSet( PARKOUR_MARKER_SET );
					MarkerSet set = markerApi.createMarkerSet( PARKOUR_MARKER_SET );

					set.setLabel( ParkourAddonPlugin.messages.bluemap_parkours_label );
					set.setToggleable( true );
					set.setDefaultHidden( true );

					for( ParkourCourse course : ParkourCourse.retrieveAll( ParkourAddonPlugin.database ) ) {
						createMarker( api, set, course );
					}

					markerApi.save();

					Log.info( set.getMarkers().size() + " markers generated for BlueMap." );
				} catch( Exception e ) {
					Log.severe( "Could not generate markers for BlueMap.", e );
				}
			} );
		} catch( NoClassDefFoundError e ) {
			Log.warning( "Cannot create BlueMap markers as plugin is not installed." );
		}
	}

	public void createMarker( BlueMapAPI api, MarkerSet set, ParkourCourse course ) {
		ParkourCategory category = course.getCategory();

		Location location = Course.getLocation( course.getName() );
		assert location != null;
		assert location.getWorld() != null;

		Optional<BlueMapMap> map = api.getMap( location.getWorld().getName() );

		// Do not show maps that do not have a display name
		if(map.isPresent() && course.getDisplayName() != null) {
			String id;
			String html;
			if(category != null) {
				if(category.isHidden()) {
					return;
				}
				id = String.format(
						ParkourAddonPlugin.messages.bluemap_parkours_markers_withcategory_id,
						category.getName(),
						course.getDisplayName()
				);
				html = String.format(
						ParkourAddonPlugin.messages.bluemap_parkours_markers_withcategory_html,
						Integer.toHexString( category.getHexColor() ),
						( Utils.isColorLight( category.getHexColor() ) ? "#000" : "#fff" ),
						category.getBaseLevel(),
						course.getDisplayName()
				);
			} else {
				id = String.format(
						ParkourAddonPlugin.messages.bluemap_parkours_markers_nocategory_id,
						course.getDisplayName()
				);
				html = String.format(
						ParkourAddonPlugin.messages.bluemap_parkours_markers_nocategory_html,
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
			Log.warning( "BlueMap map " + location.getWorld().getName() + " does not exists." );
		}
	}
}
