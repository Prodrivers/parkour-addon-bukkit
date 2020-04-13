package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class ParkourShopConverterUI implements Listener {
	private static ParkourShopConverterUI instance;

	private Inventory inv;

	private List<Integer> amounts = new ArrayList<>();
	private List<Integer> prices = new ArrayList<>();

	static ParkourShopConverterUI getInstance() {
		if( instance == null )
			instance = new ParkourShopConverterUI();
		return instance;
	}

	private ParkourShopConverterUI() {
		prepare();
	}

	private ItemStack prepareItem( int amount, int price, Material material, byte materialData, String name, String lore1, String lore2 ) {
		ItemStack item = new ItemStack( material, 1, materialData );
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName( name.replace( "%AMOUNT%", String.valueOf( amount ) ).replace( "%PRICE%", String.valueOf( price ) ) );
		String[] lores = {
				lore1.replace( "%AMOUNT%", String.valueOf( amount ) ).replace( "%PRICE%", String.valueOf( price ) ),
				lore2.replace( "%AMOUNT%", String.valueOf( amount ) ).replace( "%PRICE%", String.valueOf( price ) )
		};
		ArrayList<String> lore = new ArrayList<>( Arrays.asList( lores ) );
		meta.setLore( lore );
		item.setItemMeta( meta );
		return item;
	}

	private void prepareAmountsPrices() {
		amounts.clear();
		prices.clear();

		int count = 0;
		for( Map.Entry<String, Integer> amountStr : ParkourAddonPlugin.configuration.shops_converters_amounts.entrySet() ) {
			try {
				if( count < 5 ) {
					amounts.add( Integer.valueOf( amountStr.getKey() ) );
					prices.add( amountStr.getValue() );
					count++;
				}
			} catch( NullPointerException e ) {
				Log.warning( "Amount '" + amountStr + "' configuration lacks required values." );
			}
		}
	}

	private void prepare() {
		inv = Bukkit.createInventory( null, 6 * 9, ParkourAddonPlugin.messages.parkourshopui_converters_title );

		prepareAmountsPrices();

		for( int i = 0; i < amounts.size(); i++ ) {
			inv.setItem(
					11 + i,
					prepareItem(
							amounts.get( i ),
							prices.get( i ),
							ParkourAddonPlugin.configuration.shops_converters_to_material,
							ParkourAddonPlugin.configuration.shops_converters_to_materialData,
							ParkourAddonPlugin.messages.parkourshopui_converters_to_name,
							ParkourAddonPlugin.messages.parkourshopui_converters_to_lore1,
							ParkourAddonPlugin.messages.parkourshopui_converters_to_lore2
					)
			);

			inv.setItem(
					29 + i,
					prepareItem(
							prices.get( i ),
							amounts.get( i ),
							ParkourAddonPlugin.configuration.shops_converters_from_material,
							ParkourAddonPlugin.configuration.shops_converters_from_materialData,
							ParkourAddonPlugin.messages.parkourshopui_converters_from_name,
							ParkourAddonPlugin.messages.parkourshopui_converters_from_lore1,
							ParkourAddonPlugin.messages.parkourshopui_converters_from_lore2
					)
			);
		}

		inv.setItem(
				49,
				Utils.getCloseItem()
		);
	}

	void reload() {
		prepare();
	}

	void open( Player player ) {
		if( ParkourAddonPlugin.econ != null ) {
			player.openInventory( inv );
		} else {
			ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.parkourshopui_converters_unavailable );
			Log.severe( "Player " + player.getName() + " tried to access currency conversion UI, but no compatible economy plugin was found." );
		}
	}

	@EventHandler
	public void onInventoryClick( InventoryClickEvent event ) {
		if( !( event.getWhoClicked() instanceof Player ) ) return;
		if( event.isCancelled() ) return;

		Player player = (Player) event.getWhoClicked();
		int slot = event.getSlot();
		Inventory inventory = event.getInventory();

		if( event.getView().getTitle().equals( ParkourAddonPlugin.messages.parkourshopui_converters_title ) ) {
			event.setCancelled( true );

			if( slot >= 11 && slot <= 15 ) {
				if( convertCoinsToParkoins( player, amounts.get( slot - 11 ), prices.get( slot - 11 ) ) )
					player.closeInventory();
			} else if( slot >= 29 && slot <= 33 ) {
				if( convertCoinsFromParkoins( player, prices.get( slot - 29 ), amounts.get( slot - 29 ) ) )
					player.closeInventory();
			} else if( slot == 49 ) {
				player.closeInventory();
				ParkourShopUI.getInstance().open( player );
			}
		}
	}

	private boolean convertCoinsToParkoins( Player player, int amount, int price ) {
		if( ParkourAddonPlugin.econ.getBalance( player ) >= price ) {

			ParkourAddonPlugin.econ.withdrawPlayer( player, price );
			Parkoins.add( player, amount );

			ParkourAddonPlugin.chat.success( player, ParkourAddonPlugin.messages.parkourshopui_converters_to_bought.replace( "%AMOUNT%", String.valueOf( amount ) ).replace( "%PRICE%", String.valueOf( price ) ) );

			return true;
		} else {
			ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.parkourshopui_converters_to_notenoughbalance );
		}

		return false;
	}

	private boolean convertCoinsFromParkoins( Player player, int amount, int price ) {
		if( Parkoins.get( player ) >= price ) {

			Parkoins.remove( player, price );
			ParkourAddonPlugin.econ.depositPlayer( player, amount );

			ParkourAddonPlugin.chat.success( player, ParkourAddonPlugin.messages.parkourshopui_converters_from_bought.replace( "%AMOUNT%", String.valueOf( amount ) ).replace( "%PRICE%", String.valueOf( price ) ) );

			return true;
		} else {
			ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.parkourshopui_converters_from_notenoughbalance );
		}

		return false;
	}
}
