package fr.prodrivers.bukkit.parkouraddon.advancements;

import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import io.ebean.Database;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class Criterions {
	private final Set<String> criterion;

	@Inject
	Criterions(Database database) {
		HashSet<String> newCriterion = new HashSet<>();
		ParkourCategory.retrieveAll(database)
				.stream()
				.map(this::get)
				.forEach(newCriterion::add);
		criterion = Collections.unmodifiableSet(newCriterion);
	}

	Set<String> all() {
		return criterion;
	}

	String get(ParkourCategory category) {
		return "parkourcategory." + category.getCategoryId();
	}

	void grant(Advancement advancement, Player player, ParkourCategory category) {
		player.getAdvancementProgress(advancement).awardCriteria(get(category));
	}
}
