package fr.prodrivers.bukkit.parkouraddon.sections;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import fr.prodrivers.bukkit.commons.parties.Party;
import fr.prodrivers.bukkit.commons.parties.PartyManager;
import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionCapabilities;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.other.ParkourValidation;
import io.github.a5h73y.parkour.type.course.Course;
import io.github.a5h73y.parkour.type.player.ParkourSession;
import io.github.a5h73y.parkour.type.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ParkourSection extends Section {
	public static final String NAME_PREFIX = "parkour.";

	private final PartyManager partyManager;

	private final Parkour parkour;
	private final PlayerManager playerManager;
	private final String courseName;
	private Course pluginCourse;
	private int baseLevel;
	private int minimumProtocolVersion;

	ParkourSection(PartyManager partyManager, Parkour parkour, String courseName) {
		super(NAME_PREFIX + courseName);
		this.partyManager = partyManager;
		this.parkour = parkour;
		this.playerManager = parkour.getPlayerManager();
		this.courseName = courseName;

		load();
	}

	void load() {
		this.minimumProtocolVersion = 0;
		this.baseLevel = 0;

		ParkourCourse course = ParkourCourse.retrieveFromName(ParkourAddonPlugin.plugin.getDatabase(), this.courseName);
		if(course != null) {
			this.minimumProtocolVersion = course.getMinimumProtocolVersion() != null ? course.getMinimumProtocolVersion() : 0;

			ParkourCategory category = course.getCategory();
			if(category != null) {
				this.baseLevel = category.getBaseLevel();
			}
		}

		pluginCourse = this.parkour.getCourseManager().findCourse(this.courseName);
	}

	@Override
	public Set<SectionCapabilities> getCapabilities() {
		return Collections.emptySet();
	}

	@Override
	public boolean preJoin(Player player, Section targetSection, boolean fromParty) {
		Log.finest("Player wants to join " + this.courseName);

		int level = ParkourLevel.getLevel(player);

		@SuppressWarnings("unchecked") ViaAPI<Player> api = (ViaAPI<Player>) Via.getAPI();

		Party party = this.partyManager.getParty(player.getUniqueId());
		if(party != null) {
			for(UUID partyPlayerUUID : party.getPlayers()) {
				Player partyPlayer = Bukkit.getPlayer(partyPlayerUUID);
				if(partyPlayer != null) {
					if(api != null && api.getPlayerVersion(player) < this.minimumProtocolVersion) {
						party.broadcast(ParkourAddonPlugin.chat, ParkourAddonPlugin.messages.party_clienttooold);
						return false;
					}
					if(level < this.baseLevel) {
						party.broadcast(ParkourAddonPlugin.chat, ParkourAddonPlugin.messages.party_notenoughlevel);
						return false;
					}
				}
			}
		}

		// Check player protocol version
		if(api != null && api.getPlayerVersion(player) < this.minimumProtocolVersion) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.clienttooold);
			return false;
		}

		// Check player level
		if(level < this.baseLevel) {
			ParkourAddonPlugin.chat.error(player, ParkourAddonPlugin.messages.notenoughlevel);
			return false;
		}

		// Check if player can join course
		if(this.pluginCourse != null && !ParkourValidation.canJoinCourse(player, pluginCourse)) {
			Log.warning("Parkour plugin refused player " + player.getName() + " to join parkour " + this.courseName);
			return false;
		}

		return true;
	}

	public boolean join(Player player) {
		Log.finest("Proceeding with course join.");
		this.playerManager.joinCourse(player, this.courseName);
		ParkourSession session = this.playerManager.getParkourSession(player);
		Log.finest("Player is in parkour session: " + (session != null) + ", in course " + ((session != null) && (session.getCourse() != null) ? session.getCourse().getName() : "NULL_COURSE"));
		return (session != null);
	}

	@Override
	public boolean preLeave(OfflinePlayer offlinePlayer, Section targetSection, boolean fromParty) {
		return true;
	}

	@Override
	public boolean leave(OfflinePlayer offlinePlayer) {
		Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
		if(player != null) {
			ParkourSession session = this.playerManager.getParkourSession(player);
			if(session != null) {
				Log.finest("Player wants to leave, has parkour session.");
				this.playerManager.leaveCourse(player);

				session = this.playerManager.getParkourSession(player);
				Log.finest("Player now has no parkour session: " + (session == null));
				return (session == null);
			}
			Log.finest("Player wants to leave, but is not in parkour.");
			return true;
		}
		Log.finest("Player " + offlinePlayer.getUniqueId() + " wants to leave, but is already disconnected.");
		return true;
	}
}
