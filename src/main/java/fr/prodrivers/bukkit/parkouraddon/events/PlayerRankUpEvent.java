package fr.prodrivers.bukkit.parkouraddon.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * An Event that fires when a player
 *
 * @see Event
 */
public class PlayerRankUpEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final int newLevel;

	/**
	 * Construct a new player complete course event
	 *
	 * @param player   Player that completed the course
	 * @param newLevel Player's new level
	 */
	public PlayerRankUpEvent(Player player, int newLevel) {
		this.player = player;
		this.newLevel = newLevel;

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
	 * Get the player's new level
	 *
	 * @return Player's new level
	 */
	public int getNewLevel() {
		return newLevel;
	}

	@Override
	public @Nonnull
	HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}