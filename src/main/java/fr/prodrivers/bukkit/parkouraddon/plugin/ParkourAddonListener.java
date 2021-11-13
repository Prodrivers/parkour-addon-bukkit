package fr.prodrivers.bukkit.parkouraddon.plugin;

import fr.prodrivers.bukkit.parkouraddon.Players;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourSelection;
import io.github.a5h73y.parkour.event.PlayerFinishCourseEvent;
import io.github.a5h73y.parkour.event.PlayerJoinCourseEvent;
import io.github.a5h73y.parkour.event.PlayerLeaveCourseEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParkourAddonListener implements Listener {
	private final JavaPlugin plugin;
	private final ParkourSelection parkourSelectionUi;
	private final Players players;
	private final Course course;

	@Inject
	public ParkourAddonListener(JavaPlugin plugin, ParkourSelection parkourSelectionUi, Players players, Course course) {
		this.plugin = plugin;
		this.parkourSelectionUi = parkourSelectionUi;
		this.players = players;
		this.course = course;
	}

	@EventHandler
	public void onPlayerJoinCourseEvent(PlayerJoinCourseEvent event) {
		if(!this.course.join(event.getPlayer(), event.getCourseName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerLeaveCourseEvent(PlayerLeaveCourseEvent event) {
		if(!this.course.leave(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerFinishParkour(PlayerFinishCourseEvent event) {
		this.course.leave(event.getPlayer());

		this.players.insertCompletionAndRankAsync(event.getPlayer(), event.getCourseName());
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		String command = event.getMessage();
		if(command.startsWith("/pa ")) {
			if(command.startsWith("/pa join ")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.join(event.getPlayer(), command.substring(9)));
			} else if(command.startsWith("/pa leave")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.leave(event.getPlayer()));
			} else if(command.startsWith("/pa lobby")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.leave(event.getPlayer()));
			} else if(command.startsWith("/pa joinall")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.joinAll(event.getPlayer(), command.substring(11)));
			}
		} else if(command.startsWith("/parkour ")) {
			if(command.startsWith("/parkour join ")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.join(event.getPlayer(), command.substring(14)));
			} else if(command.startsWith("/parkour leave")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.leave(event.getPlayer()));
			} else if(command.startsWith("/parkour lobby")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.leave(event.getPlayer()));
			} else if(command.startsWith("/parkour joinall")) {
				event.setCancelled(true);

				Bukkit.getScheduler().runTask(this.plugin, () -> this.course.joinAll(event.getPlayer(), command.substring(16)));
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.parkourSelectionUi.reload(event.getPlayer());
	}

	public void unregister() {
		PlayerJoinCourseEvent.getHandlerList().unregister(this);
		PlayerLeaveCourseEvent.getHandlerList().unregister(this);
		PlayerFinishCourseEvent.getHandlerList().unregister(this);
		PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}
}
