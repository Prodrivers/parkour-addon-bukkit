package fr.prodrivers.bukkit.parkouraddon.sections;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import fr.prodrivers.bukkit.commons.sections.IProdriversSection;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.other.ParkourValidation;
import io.github.a5h73y.parkour.type.course.Course;
import io.github.a5h73y.parkour.type.player.ParkourSession;
import org.bukkit.entity.Player;

public class ParkourSection implements IProdriversSection {
	public static String name = "parkour";

	private Parkour parkour;

	public ParkourSection(Parkour parkour) {
		this.parkour = parkour;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPreferredNextSection() {
		return null;
	}

	@Override
	public boolean forceNextSection() {
		return false;
	}

	@Override
	public boolean isHub() {
		return false;
	}

	@Override
	public boolean shouldMoveParty() {
		return true;
	}

	@Override
	public boolean join(Player player, String subSection, String leavedSection) {
		// Check parameter validity
		if(subSection == null) {
			Log.warning("Refused player " + player.getName() + " to join section because name is null.");
			return false;
		}
		if(subSection.length() == 0) {
			Log.warning("Refused player " + player.getName() + " to join section because name is empty.");
			return false;
		}

		Log.finest("Player wants to join " + subSection);

		// Get course from Parkour plugin side
		Course pluginCourse = parkour.getCourseManager().findCourse(subSection);
		if(pluginCourse == null) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.invalidcourse);
			return false;
		}
		// Check if player can join course
		if(!ParkourValidation.canJoinCourse(player, pluginCourse)) {
			Log.warning("Parkour plugin refused player " + player.getName() + " to join parkour " + subSection);
			return false;
		}

		@SuppressWarnings("unchecked") ViaAPI<Player> api = (ViaAPI<Player>) Via.getAPI();

		// Get course from ParkourAddon side
		ParkourCourse course = ParkourCourse.retrieveFromName(ParkourAddonPlugin.database, subSection);
		if(course == null) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.invalidcourse);
			return false;
		}

		// Check player protocol version
		if(api != null) {
			int playerVersion = api.getPlayerVersion(player);
			if(course.getMinimumProtocolVersion() != null && playerVersion < course.getMinimumProtocolVersion()) {
				ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.clienttooold);
				Log.warning("Refused player " + player.getName() + " to join parkour " + subSection + " because client is too old. (has " + playerVersion + ", required " + course.getMinimumProtocolVersion() + ")");
				return false;
			}
		}

		Log.finest("Proceeding with course join.");
		parkour.getPlayerManager().joinCourse(player, subSection);

		ParkourSession session = parkour.getPlayerManager().getParkourSession(player);
		Log.finest("Player is in parkour session: " + (session != null) + ", in course " + ((session != null) && (session.getCourse() != null) ? session.getCourse().getName() : "NULL_COURSE"));

		return (session != null);
	}

	@Override
	public void postJoin(Player player, String subSection, String leavedSection) {
	}

	@Override
	public boolean leave(Player player, String enteredSection) {
		ParkourSession session = parkour.getPlayerManager().getParkourSession(player);
		if(session != null) {
			Log.finest("Player wants to leave, has parkour session.");
			parkour.getPlayerManager().leaveCourse(player);

			session = parkour.getPlayerManager().getParkourSession(player);
			Log.finest("Player now has no parkour session: " + (session == null));
			return (session == null);
		}
		Log.finest("Player wants to leave, but is not in parkour.");
		return true;
	}

	@Override
	public void postLeave(Player player, String enteredSection) {
	}
}
