package fr.prodrivers.bukkit.parkouraddon;

import com.google.inject.*;
import com.google.inject.Module;
import de.bluecolored.bluemap.api.BlueMapAPI;
import fr.prodrivers.bukkit.commons.ProdriversCommons;
import fr.prodrivers.bukkit.commons.parties.PartyManager;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.parkouraddon.advancements.AdvancementManager;
import fr.prodrivers.bukkit.parkouraddon.models.Models;
import fr.prodrivers.bukkit.parkouraddon.sections.ParkourSectionManager;
import fr.prodrivers.bukkit.parkouraddon.tasks.TasksRunner;
import io.ebean.Database;
import io.github.a5h73y.parkour.Parkour;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

public class ParkourAddonPlugin extends JavaPlugin implements org.bukkit.event.Listener {
	public static ParkourAddonPlugin plugin;
	public static EConfiguration configuration;
	public static EMessages messages;
	public static EChat chat;
	public static Database database = null;
	static Economy econ = null;

	Parkour parkour = null;

	private Injector injector;

	private SectionManager sectionManager;
	private PartyManager partyManager;

	private TasksRunner tasksRunner;
	private ParkourSectionManager parkourSectionManager;

	@Override
	public void onDisable() {
		PluginDescriptionFile plugindescription = this.getDescription();

		configuration.save();
		Log.info(" Saved configuration.");

		Log.info(plugindescription.getName() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile plugindescription = this.getDescription();

		if(plugin == null)
			plugin = this;

		Models.populate();

		chat = new EChat(plugindescription.getName());
		messages = new EMessages(this);
		configuration = new EConfiguration(this, messages, chat);

		Log.init();

		injector = Guice.createInjector(
				ProdriversCommons.getGuiceModule(),
				new ParkourAddonModule()
		);

		this.sectionManager = injector.getInstance(SectionManager.class);
		this.partyManager = injector.getInstance(PartyManager.class);

		database = injector.getInstance(Database.class);
		if(database == null) {
			Log.severe("ProdriversCommons SQL Provider not available, plugin is unable to start. Please check ProdriversCommons errors.");
			throw new InstantiationError("SQL provider unavailable");
		}

		if(!setupDatabase()) {
			Log.severe("Database was not initialized correctly.");
			throw new InstantiationError("Database wrongly initialized");
		}

		if(!setupParkour()) {
			Log.severe("Compatible Parkour plugin is not installed.");
			throw new InstantiationError("Unmet dependency");
		}

		if(!setupEconomy()) {
			Log.warning("Vault or/and compatible economy plugin is/are not installed. Currency conversion will not be available.");
		}

		if(!setupBluemap()) {
			Log.warning("BlueMap is not installed. Marker generation will not be available.");
		}

		AdvancementManager.init(this);

		this.tasksRunner = new TasksRunner(this);

		getServer().getPluginManager().registerEvents(new ParkourAddonListener(), this);
		getServer().getPluginManager().registerEvents(ParkourShopUI.getInstance(), this);
		getServer().getPluginManager().registerEvents(ParkourShopRankUI.getInstance(), this);
		getServer().getPluginManager().registerEvents(ParkourShopConverterUI.getInstance(), this);
		getServer().getPluginManager().registerEvents(this, this);

		this.parkourSectionManager = new ParkourSectionManager(this.parkour, this.sectionManager, this.partyManager);
		this.parkourSectionManager.load();

		getCommand("paddon").setExecutor(new Commands());

		this.tasksRunner.run();

		Log.info(plugindescription.getName() + " has been enabled!");
	}

	private boolean setupDatabase() {
		if(database == null)
			return false;
		try {
			for(Class<?> modelClass : Models.ModelsList) {
				database.find(modelClass).findCount();
			}
			return true;
		} catch(RuntimeException ex) {
			Log.info("Installing database for " + getDescription().getName() + " due to first time usage");
			try {
				database.createSqlUpdate(Utils.INIT_TABLES_SCRIPT).execute();
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

		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean setupParkour() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Parkour");
		if(plugin instanceof Parkour) {
			parkour = (Parkour) plugin;
			return true;
		}
		return false;
	}

	public Connection getConnection() {
		return injector.getInstance(Connection.class);
	}

	public SectionManager getSectionManager() {
		return sectionManager;
	}

	public PartyManager getPartyManager() {
		return partyManager;
	}

	public ParkourSectionManager getParkourSectionManager() {
		return parkourSectionManager;
	}

	public TasksRunner getTasksRunner() {
		return tasksRunner;
	}

	public Parkour getParkour() {
		return parkour;
	}
}
