package fr.prodrivers.bukkit.parkouraddon.sections;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import fr.prodrivers.bukkit.commons.parties.Party;
import fr.prodrivers.bukkit.commons.parties.PartyManager;
import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionCapabilities;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.other.ParkourValidation;
import io.github.a5h73y.parkour.type.course.Course;
import io.github.a5h73y.parkour.type.player.ParkourSession;
import io.github.a5h73y.parkour.type.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ParkourSection extends Section {
	public static final String NAME_PREFIX = "parkour.";

	private final PartyManager partyManager;
	private final EMessages messages;
	private final EChat chat;
	private final PlayerManager playerManager;
	private final ParkourLevel parkourLevel;

	private final String courseName;
	private final Course pluginCourse;
	private int baseLevel;
	private int minimumProtocolVersion;

	ParkourSection(PartyManager partyManager, Parkour parkour, String courseName, Database database, EMessages messages, EChat chat, ParkourLevel parkourLevel) {
		super(NAME_PREFIX + courseName);
		this.partyManager = partyManager;
		this.messages = messages;
		this.chat = chat;
		this.playerManager = parkour.getPlayerManager();
		this.courseName = courseName;
		this.parkourLevel = parkourLevel;

		this.minimumProtocolVersion = 0;
		this.baseLevel = 0;

		ParkourCourse course = ParkourCourse.retrieveFromName(database, this.courseName);
		if(course != null) {
			this.minimumProtocolVersion = course.getMinimumProtocolVersion() != null ? course.getMinimumProtocolVersion() : 0;

			ParkourCategory category = course.getCategory();
			if(category != null) {
				this.baseLevel = category.getBaseLevel();
			}
		}

		this.pluginCourse = parkour.getCourseManager().findCourse(this.courseName);
	}

	@Override
	public @NonNull Set<SectionCapabilities> getCapabilities() {
		return Collections.emptySet();
	}

	@Override
	public boolean preJoin(@NonNull Player player, Section targetSection, boolean fromParty) {
		Log.finest("Player wants to join " + this.courseName);

		int level = this.parkourLevel.getLevel(player);

		@SuppressWarnings("unchecked") ViaAPI<Player> api = (ViaAPI<Player>) Via.getAPI();

		Party party = this.partyManager.getParty(player.getUniqueId());
		if(party != null) {
			for(UUID partyPlayerUUID : party.getPlayers()) {
				Player partyPlayer = Bukkit.getPlayer(partyPlayerUUID);
				if(partyPlayer != null) {
					if(api != null && api.getPlayerVersion(player) < this.minimumProtocolVersion) {
						party.broadcast(this.chat, this.messages.party_clienttooold);
						return false;
					}
					if(level < this.baseLevel) {
						party.broadcast(this.chat, this.messages.party_notenoughlevel);
						return false;
					}
				}
			}
		}

		// Check player protocol version
		if(api != null && api.getPlayerVersion(player) < this.minimumProtocolVersion) {
			this.chat.error(player, this.messages.clienttooold);
			return false;
		}

		// Check player level
		if(level < this.baseLevel) {
			this.chat.error(player, this.messages.notenoughlevel);
			return false;
		}

		// Check if player can join course
		if(this.pluginCourse != null && !ParkourValidation.canJoinCourse(player, pluginCourse)) {
			Log.warning("Parkour plugin refused player " + player.getName() + " to join parkour " + this.courseName);
			return false;
		}

		return true;
	}

	public boolean join(@NonNull Player player) {
		Log.finest("Proceeding with course join.");
		this.playerManager.joinCourse(player, this.courseName);
		ParkourSession session = this.playerManager.getParkourSession(player);
		Log.finest("Player is in parkour session: " + (session != null) + ", in course " + ((session != null) && (session.getCourse() != null) ? session.getCourse().getName() : "NULL_COURSE"));
		return (session != null);
	}

	@Override
	public boolean preLeave(@NonNull OfflinePlayer offlinePlayer, Section targetSection, boolean fromParty) {
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
