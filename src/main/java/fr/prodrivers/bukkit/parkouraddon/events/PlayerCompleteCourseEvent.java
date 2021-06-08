package fr.prodrivers.bukkit.parkouraddon.events;

import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An Event that fires when a player completes a parkour course
 *
 * @see Event
 */
public class PlayerCompleteCourseEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private ParkourCourse course;

	/**
	 * Construct a new player complete course event
	 *
	 * @param player Player that completed the course
	 * @param course Completed course
	 */
	public PlayerCompleteCourseEvent(Player player, ParkourCourse course) {
		this.player = player;
		this.course = course;

	}

	/**
	 * Get the player that completed the course
	 *
	 * @return Player that completed the course
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Get the completed course
	 *
	 * @return Completed course
	 */
	public ParkourCourse getParkourCourse() {
		return course;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}