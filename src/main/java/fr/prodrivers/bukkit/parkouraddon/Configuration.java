package fr.prodrivers.bukkit.parkouraddon;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystemException;

class Configuration {
	private JavaPlugin plugin;

	private FileConfiguration config;
	private FileConfiguration messages;

	Configuration( JavaPlugin plugin ) {
		this.plugin = plugin;
		this.config = this.plugin.getConfig();

		this.config.options().copyDefaults( true );

		this.plugin.saveConfig();

		loadMessages();
	}

	private void copy( InputStream in, File file ) {
		try {
			OutputStream out = new FileOutputStream( file );
			byte[] buf = new byte[ 1024 ];
			int len;
			while( ( len = in.read( buf ) ) > 0 ) {
				out.write( buf, 0, len );
			}
			out.close();
			in.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private void loadMessages() {
		try {
			File pluginmessagesfile = new File( this.plugin.getDataFolder(), "messages.yml" );

			if( !pluginmessagesfile.exists() ) {
				if( pluginmessagesfile.getParentFile().exists() ) {
					copy( this.plugin.getResource( "messages.yml" ), pluginmessagesfile );
				} else {
					if( pluginmessagesfile.getParentFile().mkdirs() ) {
						copy( this.plugin.getResource( "messages.yml" ), pluginmessagesfile );
					} else {
						throw new FileSystemException( "Unable to create plugin's configuration directory." );
					}
				}
			}

			this.messages = YamlConfiguration.loadConfiguration( pluginmessagesfile );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	void reload() {
		this.plugin.reloadConfig();

		File pluginmessagesfile = new File( this.plugin.getDataFolder(), "messages.yml" );
		this.messages = YamlConfiguration.loadConfiguration( pluginmessagesfile );

		//Chat.reload();
	}

	String getString( String key ) {
		return this.config.getString( key );
	}

	Integer getInt( String key ) {
		return this.config.getInt( key );
	}

	/*String getMessage( String key ) {
		return this.messages.getString( key );
	}

	String getColoredMessage( String key ) {
		return ChatColor.translateAlternateColorCodes( '&', getMessage( key ) );
	}*/

	ConfigurationSection getConfigurationSection( String key ) {
		return this.config.getConfigurationSection( key );
	}

	boolean isSet( String key ) {
		return this.config.isSet( key );
	}
}
