package fr.prodrivers.bukkit.parkouraddon.listeners;

import fr.prodrivers.bukkit.parkouraddon.Utils;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.enums.QuestionType;
import io.github.a5h73y.parkour.manager.QuestionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SuppressWarnings("ClassCanBeRecord")
public class HotBarActionsListener implements Listener {
	private final Parkour parkour;

	@Inject
	public HotBarActionsListener(Parkour parkour) {
		this.parkour = parkour;
	}
	private void handleItemHeld(Cancellable event, Player player, ItemStack itemInHand) {
		if(itemInHand == null || !itemInHand.hasItemMeta()) {
			return;
		}

		Material materialInHand = itemInHand.getType();

		if (materialInHand == parkour.getConfig().getLastCheckpointTool()) {
			if (parkour.getPlayerManager().delayPlayer(player, 1)) {
				event.setCancelled(true);
				Bukkit.getScheduler().runTask(parkour, () -> parkour.getPlayerManager().playerDie(player));
			}

		} else if (materialInHand == parkour.getConfig().getHideAllDisabledTool()
				|| materialInHand == parkour.getConfig().getHideAllEnabledTool()) {
			if (parkour.getPlayerManager().delayPlayer(player, 1)) {
				event.setCancelled(true);
				parkour.getPlayerManager().toggleVisibility(player);
				player.getInventory().remove(materialInHand);
				String configPath = parkour.getPlayerManager().hasHiddenPlayers(player)
						? "ParkourTool.HideAllEnabled" : "ParkourTool.HideAll";
				parkour.getPlayerManager().giveParkourTool(player, configPath, configPath);
			}

		} else if (materialInHand == parkour.getConfig().getLeaveTool()) {
			if (parkour.getPlayerManager().delayPlayer(player, 1)) {
				event.setCancelled(true);
				parkour.getPlayerManager().leaveCourse(player);
			}

		} else if (materialInHand == parkour.getConfig().getRestartTool()) {
			if (parkour.getPlayerManager().delayPlayer(player,
					parkour.getConfig().getInt("ParkourTool.Restart.SecondCooldown"))) {

				if (parkour.getConfig().getBoolean("OnRestart.RequireConfirmation")) {
					if (!parkour.getQuestionManager().hasBeenAskedQuestion(player, QuestionType.RESTART_COURSE)) {
						//noinspection ConstantConditions
						String courseName = parkour.getPlayerManager().getParkourSession(player).getCourseName();
						parkour.getQuestionManager().askRestartProgressQuestion(player, courseName);
					} else {
						parkour.getQuestionManager().answerQuestion(player, QuestionManager.YES);
					}
				} else {
					event.setCancelled(true);
					Bukkit.getScheduler().runTask(parkour, () -> parkour.getPlayerManager().restartCourse(player));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerItemHeldChanged(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();

		ItemStack itemInHand = player.getInventory().getItem(event.getNewSlot());
		if(itemInHand == null) {
			return;
		}

		if(itemInHand.getType() == Material.AIR || itemInHand.getType() == Material.VOID_AIR || itemInHand.getType() == Material.CAVE_AIR) {
			return;
		}

		if(parkour.getPlayerManager().getParkourSession(player) == null) {
			return;
		}

		if(!Utils.isPlayerUsingTouchControls(player)) {
			return;
		}

		handleItemHeld(event, player, itemInHand);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if(parkour.getPlayerManager().getParkourSession(player) == null) {
			return;
		}

		event.setCancelled(true);

		handleItemHeld(event, player, event.getItemDrop().getItemStack());
	}

	public void unregister() {
		PlayerItemHeldEvent.getHandlerList().unregister(this);
		PlayerDropItemEvent.getHandlerList().unregister(this);
	}
}
