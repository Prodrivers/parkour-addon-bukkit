package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.Chat;
import fr.prodrivers.bukkit.commons.configuration.Configuration;
import fr.prodrivers.bukkit.commons.configuration.Messages;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class EConfiguration extends Configuration {
	public Material shops_close_material = Material.BARRIER;
	public byte shops_close_materialData = 0;
	public Material shops_ranks_material = Material.EMERALD;
	public byte shops_ranks_materialData = 0;
	public Material shops_ranks_alreadyBought_material = Material.EMERALD_BLOCK;
	public byte shops_ranks_alreadyBought_materialData = 0;
	public Material shops_ranks_notBuyable_material = Material.BEDROCK;
	public byte shops_ranks_notBuyable_materialData = 0;
	public Material shops_converters_material = Material.GOLD_INGOT;
	public byte shops_converters_materialData = 0;
	public Material shops_converters_to_material = Material.GOLD_INGOT;
	public byte shops_converters_to_materialData = 0;
	public Material shops_converters_from_material = Material.IRON_INGOT;
	public byte shops_converters_from_materialData = 0;
	public Map<String, Integer> shops_converters_amounts = new LinkedHashMap<>();

	{
		shops_converters_amounts.put( String.valueOf( 10 ) , 5 );
		shops_converters_amounts.put( String.valueOf( 100 ) , 50 );
		shops_converters_amounts.put( String.valueOf( 200 ) , 100 );
		shops_converters_amounts.put( String.valueOf( 1000 ) , 500 );
		shops_converters_amounts.put( String.valueOf( 2000 ) , 1000 );
	}

	public EConfiguration( Plugin plugin, Class<? extends Messages> messagesClass, Chat chat ) {
		super( plugin, messagesClass, chat );
	}

	@Override
	public void reload() {
		super.reload();
		Categories.reload();
	}
}