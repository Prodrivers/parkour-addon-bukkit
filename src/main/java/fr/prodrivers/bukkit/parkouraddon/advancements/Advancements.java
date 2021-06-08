package fr.prodrivers.bukkit.parkouraddon.advancements;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import org.bukkit.advancement.Advancement;

import java.util.Collection;

public class Advancements {
	private static Multimap<String, Advancement> advancements;

	static void init() {
		advancements = HashMultimap.create();
	}

	static Collection<Advancement> get(ParkourCategory category) {
		return advancements.get(Criterions.get(category));
	}

	static void load(Advancement advancement) {
		for(String criterion : advancement.getCriteria()) {
			if(Criterions.all().contains(criterion)) {
				advancements.put(criterion, advancement);
			}
		}
	}
}
