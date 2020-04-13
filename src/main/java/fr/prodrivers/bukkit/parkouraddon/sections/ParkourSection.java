package fr.prodrivers.bukkit.parkouraddon.sections;

import fr.prodrivers.bukkit.commons.sections.IProdriversSection;
import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import me.A5H73Y.parkour.course.CourseMethods;
import me.A5H73Y.parkour.player.PlayerMethods;
import org.bukkit.entity.Player;

public class ParkourSection implements IProdriversSection {
	public static String name = "parkour";

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
		if( PlayerMethods.getParkourSession( player.getName() ) == null ) {
			ParkourAddonPlugin.logger.info( "[ParkourAddon] Player is not in a parkour session, wants to join " + subSection );
			if(subSection == null) {
				ParkourAddonPlugin.logger.info( "[ParkourAddon] Refused because name is null" );
				return false;
			}
			if(subSection.length() == 0) {
				ParkourAddonPlugin.logger.info( "[ParkourAddon] Refused because name is empty" );
				return false;
			}
			ParkourAddonPlugin.logger.info( "[ParkourAddon] Proceeding" );
			CourseMethods.joinCourse( player, subSection );
			ParkourAddonPlugin.logger.info( "[ParkourAddon] Player is in parkour session : " + ( PlayerMethods.getParkourSession( player.getName() ) != null ) + ", in course " + ( ( PlayerMethods.getParkourSession( player.getName() ) != null )  && ( PlayerMethods.getParkourSession( player.getName() ).getCourse() != null ) ? PlayerMethods.getParkourSession( player.getName() ).getCourse().getName() : "NULL_COURSE" ) );
			return ( PlayerMethods.getParkourSession( player.getName() ) != null );
		}
		ParkourAddonPlugin.logger.info( "[ParkourAddon] Player is already in a parkour session, wants to join " + subSection + ", is in course " + ( PlayerMethods.getParkourSession( player.getName() ).getCourse() != null ? PlayerMethods.getParkourSession( player.getName() ).getCourse().getName() : "NULL_COURSE" ) );
		return true;
	}

	@Override
	public void postJoin( Player player, String subSection, String leavedSection ) {}

	@Override
	public boolean leave( Player player, String enteredSection ) {
		if( PlayerMethods.getParkourSession( player.getName() ) != null ) {
			PlayerMethods.playerLeave( player );
			return ( PlayerMethods.getParkourSession( player.getName() ) == null );
		}
		return true;
	}

	@Override
	public void postLeave( Player player, String enteredSection ) {}
}
