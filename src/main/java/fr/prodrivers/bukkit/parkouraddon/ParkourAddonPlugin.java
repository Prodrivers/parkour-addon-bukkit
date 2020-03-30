package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.commons.storage.SQLProvider;
import fr.prodrivers.bukkit.parkouraddon.models.Models;
import fr.prodrivers.bukkit.parkouraddon.sections.ParkourSection;
import io.ebean.EbeanServer;
import me.A5H73Y.parkour.Parkour;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ParkourAddonPlugin extends JavaPlugin implements org.bukkit.event.Listener {
	static ParkourAddonPlugin plugin;
	static EConfiguration configuration;
	static EMessages messages;
	static EChat chat;
	public static EbeanServer database = null;
	//static Configuration config = null;
	static Economy econ = null;

	public static final Logger logger = Logger.getLogger( "Minecraft" );

	@Override
	public void onDisable() {
		PluginDescriptionFile plugindescription = this.getDescription();
		configuration.save();
		logger.info( plugindescription.getName() + " has been disabled!" );
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile plugindescription = this.getDescription();
		Models.populate();

		if( plugin == null )
			plugin = this;

		chat = new EChat( plugindescription.getName() );
		configuration = new EConfiguration( this, EMessages.class, chat );
		configuration.init();
		messages = (EMessages) configuration.getMessages();

		database = SQLProvider.getEbeanServer( Models.ModelsList );
		if( database == null ) {
			logger.severe( "[ParkourAddon] ProdriversCommons SQL Provider not available, plugin is unable to start. Please check ProdriversCommons errors." );
			throw new InstantiationError( "SQL provider unavailable" );
		}

		if( !setupDatabase() ) {
			throw new InstantiationError( "Database wrongly initialized" );
		}

		if( !setupParkour() ) {
			logger.severe( "Compatible Parkour plugin is not installed." );
			throw new InstantiationError( "Unmet dependency" );
		}

		if( !setupEconomy() ) {
			logger.warning( "Vault or/and compatible economy plugin is/are not installed. Currency conversion will not be available." );
		}

		getServer().getPluginManager().registerEvents( new ParkourAddonListener(), this );
		getServer().getPluginManager().registerEvents( ParkourShopUI.getInstance(), this );
		getServer().getPluginManager().registerEvents( ParkourShopRankUI.getInstance(), this );
		getServer().getPluginManager().registerEvents( ParkourShopConverterUI.getInstance(), this );
		getServer().getPluginManager().registerEvents( this, this );

		SectionManager.register( new ParkourSection() );

		getCommand( "paddon" ).setExecutor( new Commands() );

		logger.info( plugindescription.getName() + " has been enabled!" );
	}

	private boolean setupDatabase() {
		if( database == null )
			return false;
		try {
			for( Class<?> modelClass : Models.ModelsList ) {
				database.find( modelClass ).findCount();
			}
			return true;
		} catch( RuntimeException ex ) {
			logger.info( "Installing database for " + getDescription().getName() + " due to first time usage" );
			try {
				database.createSqlUpdate( Utils.INIT_TABLES_SCRIPT ).execute();
				return true;
			} catch( RuntimeException rex ) {
				logger.severe( "Error while installing the database " + rex.getLocalizedMessage() );
				logger.severe( "Please manually execute the installation SQL script:\n" + Utils.INIT_TABLES_SCRIPT );
				rex.printStackTrace();
			}
		}
		return false;
	}

	private boolean setupEconomy() {
		if( getServer().getPluginManager().getPlugin( "Vault" ) == null ) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration( Economy.class );
		if( rsp == null )
			return false;

		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean setupParkour() {
		Plugin parkour = getServer().getPluginManager().getPlugin( "Parkour" );
		return ( parkour != null && parkour instanceof Parkour );
	}
}
