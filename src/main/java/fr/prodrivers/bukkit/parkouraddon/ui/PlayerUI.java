package fr.prodrivers.bukkit.parkouraddon.ui;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourTimeRanked;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerUI {
	private final JavaPlugin plugin;
	private final Database database;
	private final EMessages messages;
	private final EChat chat;

	@Inject
	public PlayerUI(JavaPlugin plugin, Database database, EMessages messages, EChat chat) {
		this.plugin = plugin;
		this.database = database;
		this.messages = messages;
		this.chat = chat;
	}

	private void spawnFirework(Location loc, Color color, Color fadeColor, float yOffset) {
		FireworkEffect effect = FireworkEffect.builder().trail(false).flicker(false).withColor(color).withFade(fadeColor).with(FireworkEffect.Type.BALL).build();
		final World world = loc.getWorld();
		if(world != null) {
			final Firework fw = world.spawn(loc.clone().add(0, yOffset, 0), Firework.class);
			FireworkMeta meta = fw.getFireworkMeta();
			meta.addEffect(effect);
			meta.setPower(0);
			fw.setFireworkMeta(meta);

			Bukkit.getScheduler().runTaskLater(this.plugin, fw::detonate, 2L);
		}
	}

	public void rankUp(Player player, int level) {
		spawnFirework(player.getLocation(), Color.RED, Color.GREEN, 0f);
		spawnFirework(player.getLocation(), Color.RED, Color.GREEN, 0.75f);
		spawnFirework(player.getLocation(), Color.RED, Color.GREEN, 1.5f);
		this.chat.success(player, this.messages.rankup.replaceAll("%LEVEL%", String.valueOf(level)).replaceAll("%PLAYER%", player.getName()));
	}

	public void courseCompleted(final Player player, final ParkourCourse course) {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			ParkourTimeRanked entry = ParkourTimeRanked.retrieve(this.database, player, course);
			Bukkit.getScheduler().runTask(this.plugin, () -> {
				if(course.getDisplayName() == null) {
					Log.warning("Tried to show completion of course '" + course.getName() + "' to player '" + player.getName() + "' but it has no display name.");
					return;
				}
				this.chat.send(player, this.messages.rankoncompletion.replaceAll("%RANK%", String.valueOf(entry.getRank())).replaceAll("%COURSE%", course.getDisplayName()));
			});
		});
	}
}
