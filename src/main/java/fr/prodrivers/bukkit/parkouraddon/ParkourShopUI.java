package fr.prodrivers.bukkit.parkouraddon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class ParkourShopUI implements Listener {
	private static ParkourShopUI instance;

	static ParkourShopUI getInstance() {
		if( instance == null )
			instance = new ParkourShopUI();
		return instance;
	}

	private Inventory inv;

	private ParkourShopUI() {
		prepare();
	}

	private ItemStack prepareItem( Material material, byte materialData, String shopName ) {
		ItemStack item = new ItemStack( material, 1, materialData );
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName( shopName );
		item.setItemMeta( meta );
		return item;
	}

	private void prepare() {
		inv = Bukkit.createInventory( null, 4 * 9, ParkourAddonPlugin.messages.parkourshopui_general_title );

		if( ParkourAddonPlugin.econ != null ) {
			inv.setItem(
					12,
					prepareItem(
							ParkourAddonPlugin.configuration.shops_ranks_material,
							ParkourAddonPlugin.configuration.shops_ranks_materialData,
							ParkourAddonPlugin.messages.parkourshopui_general_rankname
					)
			);

			inv.setItem(
					14,
					prepareItem(
							ParkourAddonPlugin.configuration.shops_converters_material,
							ParkourAddonPlugin.configuration.shops_converters_materialData,
							ParkourAddonPlugin.messages.parkourshopui_general_convertername
					)
			);
		} else {
			inv.setItem(
					13,
					prepareItem(
							ParkourAddonPlugin.configuration.shops_ranks_material,
							ParkourAddonPlugin.configuration.shops_ranks_materialData,
							ParkourAddonPlugin.messages.parkourshopui_general_rankname
					)
			);
		}

		inv.setItem(
				31,
				Utils.getCloseItem()
		);
	}

	void reload() {
		prepare();
	}

	void open( Player player ) {
		player.openInventory( inv );
	}

	@EventHandler
	public void onInventoryClick( InventoryClickEvent event ) {
		if( !( event.getWhoClicked() instanceof Player ) ) return;
		if( event.isCancelled() ) return;

		Player player = (Player) event.getWhoClicked();
		int slot = event.getSlot();
		Inventory inventory = event.getInventory();

		if( event.getView().getTitle().equals( ParkourAddonPlugin.messages.parkourshopui_general_title ) ) {
			event.setCancelled( true );

			if( ParkourAddonPlugin.econ != null ) {
				if( slot == 12 ) {
					player.closeInventory();
					ParkourShopRankUI.getInstance().open( player );
				} else if( slot == 14 ) {
					player.closeInventory();
					ParkourShopConverterUI.getInstance().open( player );
				}
			} else {
				if( slot == 13 ) {
					player.closeInventory();
					ParkourShopRankUI.getInstance().open( player );
				}
			}

			if( slot == 31 ) {
				player.closeInventory();
				player.performCommand( "bossshop shop" );
			}
		}
	}
}
