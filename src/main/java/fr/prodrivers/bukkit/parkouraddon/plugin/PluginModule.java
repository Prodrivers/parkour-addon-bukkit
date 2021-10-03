package fr.prodrivers.bukkit.parkouraddon.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import fr.prodrivers.bukkit.commons.Chat;
import fr.prodrivers.bukkit.commons.configuration.Configuration;
import fr.prodrivers.bukkit.commons.configuration.Messages;
import fr.prodrivers.bukkit.parkouraddon.models.DatabaseConfigProvider;
import io.ebean.config.DatabaseConfig;
import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.course.CourseManager;
import io.github.a5h73y.parkour.type.player.PlayerManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginModule extends AbstractModule {
	private final Main plugin;
	private final Parkour parkour;
	private final Economy economy;

	public PluginModule(Main plugin, Parkour parkour, Economy economy) {
		this.plugin = plugin;
		this.parkour = parkour;
		this.economy = economy;
	}

	@Override
	protected void configure() {
		bind(Plugin.class).toInstance(this.plugin);
		bind(JavaPlugin.class).toInstance(this.plugin);
		bind(Main.class).toInstance(this.plugin);
		bind(Chat.class).to(EChat.class);
		bind(Configuration.class).to(EConfiguration.class);
		bind(Messages.class).to(EMessages.class);
		bind(DatabaseConfig.class).toProvider(DatabaseConfigProvider.class);

		bind(Parkour.class).toInstance(this.parkour);
		bind(PlayerManager.class).toInstance(this.parkour.getPlayerManager());
		bind(CourseManager.class).toInstance(this.parkour.getCourseManager());

		if(this.economy != null) {
			bind(Economy.class).toInstance(this.economy);
		} else {
			bind(Economy.class).toProvider(Providers.of(null));
		}
	}
}