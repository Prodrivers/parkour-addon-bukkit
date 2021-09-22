package fr.prodrivers.bukkit.parkouraddon;

import com.google.inject.AbstractModule;
import fr.prodrivers.bukkit.parkouraddon.models.DatabaseConfigProvider;
import io.ebean.config.DatabaseConfig;

public class ParkourAddonModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(DatabaseConfig.class).toProvider(DatabaseConfigProvider.class);
	}
}
