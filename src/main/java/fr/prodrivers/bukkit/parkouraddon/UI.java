package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourTimeRanked;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

class UI {
	static private void spawnFirework(Location loc, Color color, Color fadeColor, float yOffset) {
		FireworkEffect effect = FireworkEffect.builder().trail(false).flicker(false).withColor(color).withFade(fadeColor).with(FireworkEffect.Type.BALL).build();
		final Firework fw = loc.getWorld().spawn(loc.clone().add(0, yOffset, 0), Firework.class);
		FireworkMeta meta = fw.getFireworkMeta();
		meta.addEffect(effect);
		meta.setPower(0);
		fw.setFireworkMeta(meta);

		Bukkit.getScheduler().runTaskLater(ParkourAddonPlugin.plugin, fw::detonate, 2L);
	}

	static void rankUp(Player player, int level) {
		spawnFirework(player.getLocation(), Color.RED, Color.GREEN, 0f);
		spawnFirework(player.getLocation(), Color.RED, Color.GREEN, 0.75f);
		spawnFirework(player.getLocation(), Color.RED, Color.GREEN, 1.5f);
		ParkourAddonPlugin.chat.success(player, ParkourAddonPlugin.messages.rankup.replaceAll("%LEVEL%", String.valueOf(level)).replaceAll("%PLAYER%", player.getName()));
	}

	static void courseCompleted(final Player player, final ParkourCourse course) {
		Bukkit.getScheduler().runTaskAsynchronously(ParkourAddonPlugin.plugin, () -> {
			ParkourTimeRanked entry = ParkourTimeRanked.retrieve(ParkourAddonPlugin.plugin.getDatabase(), player, course);
			Bukkit.getScheduler().runTask(ParkourAddonPlugin.plugin, () -> {
				if(course.getDisplayName() == null) {
					Log.warning("Tried to show completion of course '" + course.getName() + "' to player '" + player.getName() + "' but it has no display name.");
					return;
				}
				ParkourAddonPlugin.chat.send(player, ParkourAddonPlugin.messages.rankoncompletion.replaceAll("%RANK%", String.valueOf(entry.getRank())).replaceAll("%COURSE%", course.getDisplayName()));
			});
		});
	}
}
