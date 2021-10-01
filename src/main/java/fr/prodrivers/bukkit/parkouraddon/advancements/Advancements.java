package fr.prodrivers.bukkit.parkouraddon.advancements;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class Advancements {
	private final Multimap<String, Advancement> advancements;
	private final Criterions criterions;

	@Inject
	Advancements(Criterions criterions) {
		this.criterions = criterions;
		advancements = HashMultimap.create();
	}

	Collection<Advancement> get(ParkourCategory category) {
		return advancements.get(this.criterions.get(category));
	}

	void load(Advancement advancement) {
		for(String criterion : advancement.getCriteria()) {
			if(this.criterions.all().contains(criterion)) {
				advancements.put(criterion, advancement);
			}
		}
	}

	void grant(Advancement advancement, Player player, ParkourCategory category) {
		this.criterions.grant(advancement, player, category);
	}
}
