package fr.prodrivers.bukkit.parkouraddon.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.bluecolored.bluemap.api.BlueMapAPI;
import fr.prodrivers.bukkit.commons.ProdriversCommons;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.advancements.AdvancementManager;
import fr.prodrivers.bukkit.parkouraddon.commands.Commands;
import fr.prodrivers.bukkit.parkouraddon.models.Models;
import fr.prodrivers.bukkit.parkouraddon.sections.ParkourSectionManager;
import fr.prodrivers.bukkit.parkouraddon.tasks.TasksRunner;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShop;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShopConverter;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShopRank;
import io.ebean.Database;
import io.github.a5h73y.parkour.Parkour;
import net.milkbowl.vault.economy.Economy;
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

		teardown();

		if(this.configuration != null) {
			configuration.save();
			getLogger().info("Saved configuration.");
		}

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
		this.injector.getInstance(EChat.class);
		this.configuration = this.injector.getInstance(EConfiguration.class);

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
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourAddonListener.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourShop.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourShopRank.class), this);
		getServer().getPluginManager().registerEvents(this.injector.getInstance(ParkourShopConverter.class), this);

		// Load sections
		ParkourSectionManager parkourSectionManager = this.injector.getInstance(ParkourSectionManager.class);
		parkourSectionManager.load();

		// Register command executor
		getCommand("paddon").setExecutor(this.injector.getInstance(Commands.class));

		// Run tasks
		TasksRunner tasksRunner = this.injector.getInstance(TasksRunner.class);
		tasksRunner.run();
	}

	private void teardown() {
		// Unregister listeners
		if(this.injector != null) {
			this.injector.getInstance(ParkourAddonListener.class).unregister();
			this.injector.getInstance(ParkourShop.class).unregister();
			this.injector.getInstance(ParkourShopRank.class).unregister();
			this.injector.getInstance(ParkourShopConverter.class).unregister();
		}

		// Unregister command executor
		getCommand("paddon").setExecutor(null);
	}

	public void reload() {
		this.setup();
		this.teardown();
	}

	private boolean setupDatabase() {
		if(this.database == null)
			return false;
		try {
			for(Class<?> modelClass : Models.ModelsList) {
				this.database.find(modelClass).findCount();
			}
			return true;
		} catch(RuntimeException ex) {
			Log.info("Installing database for " + getDescription().getName() + " due to first time usage");
			try {
				this.database.sqlUpdate(Utils.INIT_TABLES_SCRIPT).execute();
				return true;
			} catch(RuntimeException rex) {
				Log.severe("Cannot install the database.", rex);
				Log.severe("Please manually execute the installation SQL script:\n" + Utils.INIT_TABLES_SCRIPT);
			}
		}
		return false;
	}

	private boolean setupBluemap() {
		try {
			return BlueMapAPI.getInstance().isPresent();
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
