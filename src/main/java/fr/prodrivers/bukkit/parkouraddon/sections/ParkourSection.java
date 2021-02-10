package fr.prodrivers.bukkit.parkouraddon.sections;

import fr.prodrivers.bukkit.commons.sections.IProdriversSection;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.player.ParkourSession;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

public class ParkourSection implements IProdriversSection {
	public static String name = "parkour";

	private Parkour parkour;

	public ParkourSection( Parkour parkour ) {
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
	public boolean join( Player player, String subSection, String leavedSection ) {
		ParkourSession session = parkour.getPlayerManager().getParkourSession( player );
		if( session == null ) {
			Log.finest( "Player is not in a parkour session, wants to join " + subSection );
			if(subSection == null) {
				Log.warning( "Refused player " + player.getName() + " to join section because name is null." );
				return false;
			}
			if(subSection.length() == 0) {
				Log.warning( "Refused player " + player.getName() + " to join section because name is empty." );
				return false;
			}
			ViaAPI api = Via.getAPI();
			ParkourCourse course = ParkourCourse.retrieveFromName( ParkourAddonPlugin.database, subSection );
			if( course == null ) {
				ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.invalidcourse );
				return false;
			}
			if( api != null && course.getMinimumProtocolVersion() != null && api.getPlayerVersion( player ) < course.getMinimumProtocolVersion() ) {
				ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.clienttooold );
				return false;
			}
			Log.finest( "Proceeding with course join." );
			parkour.getPlayerManager().joinCourse( player, subSection );
			session = parkour.getPlayerManager().getParkourSession( player );
			Log.finest( "Player is in parkour session: " + ( session != null ) + ", in course " + ( ( session != null )  && ( session.getCourse() != null ) ? session.getCourse().getName() : "NULL_COURSE" ) );
			return ( session != null );
		}
		Log.finest( "Player is already in a parkour session, wants to join " + subSection + ", is in course " + ( session.getCourse() != null ? session.getCourse().getName() : "NULL_COURSE" ) );
		return true;
	}

	@Override
	public void postJoin( Player player, String subSection, String leavedSection ) {}

	@Override
	public boolean leave( Player player, String enteredSection ) {
		ParkourSession session = parkour.getPlayerManager().getParkourSession( player );
		if( session != null ) {
			Log.finest( "Player wants to leave, has parkour session." );
			parkour.getPlayerManager().leaveCourse( player );

			session = parkour.getPlayerManager().getParkourSession( player );
			Log.finest( "Player now has no parkour session: " + ( session == null ) );
			return ( session == null );
		}
		Log.finest( "Player wants to leave, but is not in parkour." );
		return true;
	}

	@Override
	public void postLeave( Player player, String enteredSection ) {}
}
