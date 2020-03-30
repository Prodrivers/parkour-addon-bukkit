package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.sections.SectionManager;
import me.A5H73Y.parkour.event.PlayerFinishCourseEvent;
import me.A5H73Y.parkour.event.PlayerJoinCourseEvent;
import me.A5H73Y.parkour.event.PlayerLeaveCourseEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ParkourAddonListener implements Listener {
	@EventHandler
	public void onPlayerJoinCourseEvent( PlayerJoinCourseEvent event ) {
		Players.joinParkour( event.getPlayer(), event.getCourseName() );
	}

	@EventHandler
	public void onPlayerLeaveCourseEvent( PlayerLeaveCourseEvent event ) {
		Players.leaveParkour( event.getPlayer() );
	}

	@EventHandler
	public void onPlayerFinishParkour( PlayerFinishCourseEvent event ) {
		Players.insertCompletionAndRankAsync( event.getPlayer(), event.getCourseName() );
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent( PlayerCommandPreprocessEvent event ) {
		String command = event.getMessage();
		if( command.startsWith( "/pa " ) ) {
			if( command.startsWith( "/pa join " ) ) {
				event.setCancelled( true );

				Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> Players.joinParkour( event.getPlayer(), command.substring( 9 ) ) );
			} else if( command.startsWith( "/pa leave" ) ) {
				event.setCancelled( true );

				Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> SectionManager.enter( event.getPlayer() ) );
			} else if( command.startsWith( "/pa lobby" ) ) {
				event.setCancelled( true );

				Bukkit.getScheduler().runTask( ParkourAddonPlugin.plugin, () -> SectionManager.enter( event.getPlayer(), "main" ) );
			} else if( command.startsWith( "/pa joinall" ) ) {
				event.setCancelled( true );
			}
		}
	}
}
