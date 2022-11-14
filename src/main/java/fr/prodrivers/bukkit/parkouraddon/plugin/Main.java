package fr.prodrivers.bukkit.parkouraddon.plugin;

import co.aikar.commands.BukkitCommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.bluecolored.bluemap.api.BlueMapAPI;
import fr.prodrivers.bukkit.commons.ProdriversCommons;
import fr.prodrivers.bukkit.commons.chat.Chat;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.advancements.AdvancementManager;
import fr.prodrivers.bukkit.parkouraddon.commands.Commands;
import fr.prodrivers.bukkit.parkouraddon.listeners.HotBarActionsListener;
import fr.prodrivers.bukkit.parkouraddon.listeners.PlayerLoginLogoutListener;
import fr.prodrivers.bukkit.parkouraddon.listeners.PluginListener;
import fr.prodrivers.bukkit.parkouraddon.models.Models;
import fr.prodrivers.bukkit.parkouraddon.sections.ParkourSectionManager;
import fr.prodrivers.bukkit.parkouraddon.tasks.TasksRunner;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShop;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShopConverter;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShopRank;
import io.ebean.Database;
import io.github.a5h73y.parkour.Parkour;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Singleton;

@Singleton
public class Main extends JavaPlugin {
	private Database database;

	public EConfiguration configuration;

	private Parkour parkour;
	private Economy economy;

	private Injector injector;

	@Override
	public void onDisable() {
		PluginDescriptionFile plugindescription = this.getDescription();

		if(this.configuration != null) {
			configuration.save();
			getLogger().info("Saved configuration.");
		}

		teardown();

		this.parkour = null;
		this.economy = null;

		getLogger().info(plugindescription.getName() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile plugindescription = this.getDescription();

		if(!setupParkour()) {
			throw new InstantiationError("Compatible Parkour plugin is not installed.");
		}

		if(!setupEconomy()) {
			getLogger().warning("Vault or/and compatible economy plugin is/are not installed. Currency conversion will not be available.");
		}

		if(!setupBluemap()) {
			getLogger().warning("BlueMap is not installed. Marker generation will not be available.");
		}

		Models.populate();

		setup();

		getLogger().info(plugindescription.getName() + " has been enabled!");
	}

	private void setup() {
		this.injector = Guice.createInjector(
				ProdriversCommons.getGuiceModule(),
				new PluginModule(this, parkour, economy)
		);

		// Preload configuration and messages
		this.injector.getInstance(EMessages.class);
		Chat chat = this.injector.getInstance(EChat.class);
		this.configuration = this.injector.getInstance(EConfiguration.class);
		chat.setName(this.getDescription().getName());
		configuration.setChat(chat);

		// Setup logging
		Log.init(getLogger(), configuration.logLevel);

		// Setup database
		this.database = this.injector.getInstance(Database.class);
		if(this.database == null) {
			getLogger().severe("ProdriversCommons SQL Provider not available, plugin is unable to start. Please check ProdriversCommons errors.");
			throw new InstantiationError("SQL provider unavailable");
		}

		if(!setupDatabase()) {
			throw new InstantiationError("Database was not initialized correctly.");
		}

		// Preload advancements
		this.injector.getInstance(AdvancementManager.class);

		// Register listeners
		getServer().getPluginManager().registerEvents(this.injector.getInstance(PluginListener.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(HotBarActionsListener.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(PlayerLoginLogoutListener.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourShop.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourShopRank.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourShopConverter.class), this);

		// Load sections
		ParkourSectionManager parkourSectionManager = this.injector.getInstance(ParkourSectionManager.class);
		parkourSectionManager.load();

		// Load command manager
		this.injector.getInstance(Commands.class);

		// Run tasks
		TasksRunner tasksRunner = this.injector.getInstance(TasksRunner.class);
		tasksRunner.run();
	}

	private void teardown() {
		// Unregister listeners
		if(this.injector != null) {
			this.injector.getInstance(PluginListener.class).unregister();
			this.injector.getInstance(HotBarActionsListener.class).unregister();
			this.injector.getInstance(PlayerLoginLogoutListener.class).unregister();
			this.injector.getInstance(ParkourShop.class).unregister();
			this.injector.getInstance(ParkourShopRank.class).unregister();
			this.injector.getInstance(ParkourShopConverter.class).unregister();
			this.injector.getInstance(BukkitCommandManager.class).unregisterCommands();
		}
		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
		PluginDisableEvent.getHandlerList().unregister(this);

		// Unregister command executor
		PluginCommand executor = getCommand("paddon");
		if(executor != null) {
			executor.setExecutor(null);
		}

		if(this.database != null) {
			this.database.shutdown();
		}
		this.configuration = null;
		this.injector = null;
	}

	public void reload() {
		this.teardown();
		this.setup();
	}

	private boolean setupDatabase() {
		if(this.database == null) {
			return false;
		}
		try {
			for(Class<?> modelClass : Models.ModelsList) {
				this.database.find(modelClass).findCount();
			}
			return true;
		} catch(RuntimeException ex) {
			Log.severe("Cannot verify that database schema is valid.", ex);
			Log.severe("Please manually execute the installation SQL script:\n" + Utils.INIT_TABLES_SCRIPT);
		}
		return false;
	}

	private boolean setupBluemap() {
		try {
			// Only try to call BlueMapAPI as it may not be ready when calling here, for some reason.
			// As we use onEnable, it is not a problem for us.
			//noinspection ResultOfMethodCallIgnored
			BlueMapAPI.getInstance();
			return true;
		} catch(Exception | NoClassDefFoundError e) {
			return false;
		}
	}

	private boolean setupEconomy() {
		if(getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null)
			return false;

		this.economy = rsp.getProvider();
		return true;
	}

	private boolean setupParkour() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Parkour");
		if(plugin instanceof Parkour) {
			this.parkour = (Parkour) plugin;
			return true;
		}
		return false;
	}
}
