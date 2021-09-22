package fr.prodrivers.bukkit.parkouraddon.models;

import javax.inject.Provider;
import javax.inject.Singleton;

import io.ebean.config.DatabaseConfig;

@Singleton
public class DatabaseConfigProvider implements Provider<DatabaseConfig> {
	@Override
	public DatabaseConfig get() {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setClasses(Models.ModelsList);
		return dbConfig;
	}
}
