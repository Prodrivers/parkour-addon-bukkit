package fr.prodrivers.bukkit.parkouraddon.advancements;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Iterator;

public class AdvancementManager {
	private static Plugin plugin;

	public static void init( Plugin plugin ) {
		AdvancementManager.plugin = plugin;

		Advancements.init();
		Criterions.init();

		for( Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
			Advancements.load( it.next() );
		}
	}

	public static void reload() {
		init( plugin );
	}

	public interface CriterionGranter {
		public void grant( Advancement advancement );
	}

	private static void grant( Player player, Collection<Advancement> advancements, CriterionGranter criterionGranter ) {
		if( !advancements.isEmpty() ) {
			for( Advancement advancement : advancements ) {
				criterionGranter.grant( advancement );
			}
		}
	}

	public static void grant( Player player, ParkourCategory category ) {
		Collection<Advancement> advancements = Advancements.get( category );
		if( !advancements.isEmpty() ) {
			Log.info( "Grant criterion for category " + category.getName() + "(" + category.getCategoryId() + ") to player \"" + player.getName() + "\"." );

			grant( player, advancements, advancement -> Criterions.grant( advancement, player, category ) );
		} else {
			Log.warning( "Tried to grant criterion for category " + category.getName() + "(" + category.getCategoryId() + "), but no corresponding advancement exists." );
		}
	}
}
