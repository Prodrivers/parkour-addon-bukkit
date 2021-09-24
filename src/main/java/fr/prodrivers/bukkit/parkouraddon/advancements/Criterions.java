package fr.prodrivers.bukkit.parkouraddon.advancements;

import fr.prodrivers.bukkit.parkouraddon.ParkourAddonPlugin;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Criterions {
	static Set<String> criterion;

	static void init() {
		HashSet<String> newCriterion = new HashSet<>();
		ParkourCategory.retrieveAll(ParkourAddonPlugin.plugin.getDatabase())
				.stream()
				.map(Criterions::get)
				.forEach(newCriterion::add);
		criterion = Collections.unmodifiableSet(newCriterion);
	}

	static Set<String> all() {
		return criterion;
	}

	static String get(ParkourCategory category) {
		return "parkourcategory." + category.getCategoryId();
	}

	static void grant(Advancement advancement, Player player, ParkourCategory category) {
		player.getAdvancementProgress(advancement).awardCriteria(get(category));
	}
}
