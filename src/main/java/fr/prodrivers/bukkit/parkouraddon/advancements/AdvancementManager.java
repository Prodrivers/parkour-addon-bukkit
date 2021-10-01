package fr.prodrivers.bukkit.parkouraddon.advancements;

import com.google.inject.Injector;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Iterator;

@Singleton
public class AdvancementManager {
	private final Advancements advancements;

	@Inject
	public AdvancementManager(Advancements advancements) {
		this.advancements = advancements;

		for(Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
			this.advancements.load(it.next());
		}
	}

	public interface CriterionGranter {
		void grant(Advancement advancement);
	}

	private void grant(Player player, Collection<Advancement> advancements, CriterionGranter criterionGranter) {
		if(!advancements.isEmpty()) {
			for(Advancement advancement : advancements) {
				criterionGranter.grant(advancement);
			}
		}
	}

	public void grant(Player player, ParkourCategory category) {
		Collection<Advancement> advancements = this.advancements.get(category);
		if(!advancements.isEmpty()) {
			Log.info("Grant criterion for category " + category.getName() + "(" + category.getCategoryId() + ") to player \"" + player.getName() + "\".");

			grant(player, advancements, advancement -> this.advancements.grant(advancement, player, category));
		} else {
			Log.warning("Tried to grant criterion for category " + category.getName() + "(" + category.getCategoryId() + "), but no corresponding advancement exists.");
		}
	}
}
