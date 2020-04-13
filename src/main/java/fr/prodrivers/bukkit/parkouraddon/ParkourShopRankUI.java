package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

class ParkourShopRankUI implements Listener {
	private static ParkourShopRankUI instance;

	static ParkourShopRankUI getInstance() {
		if( instance == null )
			instance = new ParkourShopRankUI();
		return instance;
	}

	private class RankItem implements Cloneable {
		String name;
		int price;
		int minLevel;
		int targetLevel;
		Material material;
		ItemStack item;
		int slot;
		ChatColor chatColor;

		RankItem( String name, int price, int minLevel, int targetLevel, ChatColor chatColor, Material material ) {
			this.name = name;
			this.price = price;
			this.minLevel = minLevel;
			this.targetLevel = targetLevel;
			this.material = material;
			this.chatColor = chatColor;
		}

		public RankItem clone() {
			RankItem item = null;
			try {
				item = (RankItem) super.clone();
				item.item = null;
			} catch( CloneNotSupportedException cnse ) {
				cnse.printStackTrace( System.err );
			}

			return item;
		}
	}

	private ArrayList<RankItem> rankItems = new ArrayList<>(), boughtRankItems = new ArrayList<>(), notBuyableRankItems = new ArrayList<>();

	private int lines, closeSlot;

	private ParkourShopRankUI() {
		prepare();
	}

	private RankItem prepareItem( RankItem rankItem, Material material, String rankName, String rankLore1, String rankLore2 ) {
		return prepareItem( rankItem, material, rankName, rankLore1, rankLore2, null );
	}

	private RankItem prepareItem( RankItem rankItem, Material material, String rankName, String rankLore1, String rankLore2, String rankLore3 ) {
		rankItem.item = new ItemStack( material, 1 );
		ItemMeta meta = rankItem.item.getItemMeta();
		meta.setDisplayName( rankName.replace( "%CATEGORYCOLOR%", rankItem.chatColor.toString() ).replace( "%CATEGORY%", rankItem.name ).replace( "%PRICE%", String.valueOf( rankItem.price ) ).replace( "%TARGETLEVEL%", String.valueOf( rankItem.targetLevel ) ) );
		ArrayList<String> lore = new ArrayList<>();
		lore.add( rankLore1.replace( "%CATEGORYCOLOR%", rankItem.chatColor.toString() ).replace( "%CATEGORY%", rankItem.name ).replace( "%PRICE%", String.valueOf( rankItem.price ) ).replace( "%TARGETLEVEL%", String.valueOf( rankItem.targetLevel ) ).replace( "%MINLEVEL%", String.valueOf( rankItem.minLevel ) ) );
		lore.add( rankLore2.replace( "%CATEGORYCOLOR%", rankItem.chatColor.toString() ).replace( "%CATEGORY%", rankItem.name ).replace( "%PRICE%", String.valueOf( rankItem.price ) ).replace( "%TARGETLEVEL%", String.valueOf( rankItem.targetLevel ) ).replace( "%MINLEVEL%", String.valueOf( rankItem.minLevel ) ) );
		if( rankLore3 != null )
			lore.add( rankLore3.replace( "%CATEGORYCOLOR%", rankItem.chatColor.toString() ).replace( "%CATEGORY%", rankItem.name ).replace( "%PRICE%", String.valueOf( rankItem.price ) ).replace( "%TARGETLEVEL%", String.valueOf( rankItem.targetLevel ) ).replace( "%MINLEVEL%", String.valueOf( rankItem.minLevel ) ) );
		meta.setLore( lore );
		rankItem.item.setItemMeta( meta );
		return rankItem;
	}

	private RankItem prepareItem( RankItem rankItem ) {
		return prepareItem(
				rankItem,
				rankItem.material,
				ParkourAddonPlugin.messages.parkourshopui_ranks_rankitemname,
				ParkourAddonPlugin.messages.parkourshopui_ranks_rankitemlore1,
				ParkourAddonPlugin.messages.parkourshopui_ranks_rankitemlore2
		);
	}

	private RankItem prepareBoughtItem( RankItem rankItem ) {
		return prepareItem(
				rankItem,
				ParkourAddonPlugin.configuration.shops_ranks_alreadyBought_material,
				ParkourAddonPlugin.messages.parkourshopui_ranks_boughtrankitemname,
				ParkourAddonPlugin.messages.parkourshopui_ranks_boughtrankitemlore1,
				ParkourAddonPlugin.messages.parkourshopui_ranks_boughtrankitemlore2
		);
	}

	private RankItem prepareNotBuyableItem( RankItem rankItem ) {
		return prepareItem(
				rankItem,
				ParkourAddonPlugin.configuration.shops_ranks_notBuyable_material,
				ParkourAddonPlugin.messages.parkourshopui_ranks_notbuyablerankitemname,
				ParkourAddonPlugin.messages.parkourshopui_ranks_notbuyablerankitemlore1,
				ParkourAddonPlugin.messages.parkourshopui_ranks_notbuyablerankitemlore2,
				ParkourAddonPlugin.messages.parkourshopui_ranks_notbuyablerankitemlore3
		);
	}

	private void prepareItems() {
		RankItem item;
		int count = 0;

		rankItems.clear();
		boughtRankItems.clear();
		notBuyableRankItems.clear();

		List<ParkourCategory> categories = ParkourCategory.retrieveAll( ParkourAddonPlugin.database );

		for( ParkourCategory category : categories ) {
			try {
				if( count < 45 && category.getBaseLevel() > 0 && category.getPrice() > 0 ) {
					ParkourCategory prevCat = null;
					if( category.getPreviousCategory() != null )
						prevCat = category.forceGetPreviousCategory( ParkourAddonPlugin.database );
					item = new RankItem(
							category.getName(),
							category.getPrice(),
							( prevCat != null ? prevCat.getBaseLevel() : 0 ),
							category.getBaseLevel(),
							ChatColor.valueOf( category.getChatColor() ),
							Material.valueOf( category.getMaterial() )
					);
					rankItems.add( prepareItem( item ) );
					boughtRankItems.add( prepareBoughtItem( item.clone() ) );
					notBuyableRankItems.add( prepareNotBuyableItem( item.clone() ) );
					count++;
				}
			} catch( Exception e ) {
				Log.warning( "Rank '" + category.getName() + "' configuration lacks required values.", e );
			}
		}
	}

	private void prepareSlots() {
		boolean ending = false;
		int remainder = rankItems.size() % 9;
		lines = rankItems.size() / 9 + ( remainder != 0 ? 1 : 0 );
		int size = rankItems.size(), left = size;
		int currentSlot = 0, slotOffset = 0;

		for( int i = 0; i < size; i++ ) {
			if( left <= remainder ) {
				if( !ending ) {
					slotOffset = currentSlot;
					currentSlot = ( 9 - remainder ) / 2;
					if( currentSlot < 0 )
						currentSlot = 0;
					ending = true;
				}
			}
			rankItems.get( i ).slot = currentSlot + slotOffset;
			boughtRankItems.get( i ).slot = currentSlot + slotOffset;
			notBuyableRankItems.get( i ).slot = currentSlot + slotOffset;
			currentSlot++;
			left--;
		}

		if( lines < 5 ) {
			lines += 2;
		} else {
			lines++;
		}

		closeSlot = ( lines - 1 ) * 9 + 4;
	}

	private void prepare() {
		prepareItems();
		prepareSlots();
	}

	void reload() {
		prepare();
	}

	private static boolean isBought( Player player, RankItem item ) {
		return ( ParkourLevel.getLevel( player ) >= item.targetLevel );
	}

	private static boolean isBuyable( Player player, RankItem item ) {
		return ( Parkoins.get( player ) >= item.price && ParkourLevel.getLevel( player ) >= item.minLevel );
	}

	void open( Player player ) {
		Inventory inv = Bukkit.createInventory( null, lines * 9, ParkourAddonPlugin.messages.parkourshopui_ranks_title );
		RankItem item, boughtItem, notBuyableItem;

		for( int i = 0; i < rankItems.size(); i++ ) {
			item = rankItems.get( i );

			if( isBought( player, item ) ) {
				boughtItem = boughtRankItems.get( i );
				inv.setItem( boughtItem.slot, boughtItem.item );
			} else if( isBuyable( player, item ) ) {
				inv.setItem( item.slot, item.item );
			} else {
				notBuyableItem = notBuyableRankItems.get( i );
				inv.setItem( notBuyableItem.slot, notBuyableItem.item );
			}
		}

		inv.setItem(
				closeSlot,
				Utils.getCloseItem()
		);

		player.openInventory( inv );
	}

	@EventHandler
	public void onInventoryClick( InventoryClickEvent event ) {
		if( !( event.getWhoClicked() instanceof Player ) ) return;
		if( event.isCancelled() ) return;

		Player player = (Player) event.getWhoClicked();
		ItemStack clicked = event.getCurrentItem();
		Inventory inventory = event.getInventory();

		if( event.getView().getTitle().equals( ParkourAddonPlugin.messages.parkourshopui_ranks_title ) ) {
			event.setCancelled( true );

			if( event.getSlot() == closeSlot ) {
				player.closeInventory();
				ParkourShopUI.getInstance().open( player );
			} else if( clicked != null && clicked.getItemMeta() != null ) {
				for( RankItem item : rankItems ) {
					if( clicked.getItemMeta().getDisplayName().equals( item.item.getItemMeta().getDisplayName() ) ) {
						if( buyRank( player, item ) )
							player.closeInventory();
						break;
					}
				}
			}
		}
	}

	private static boolean buyRank( Player player, RankItem item ) {
		int lvl = ParkourLevel.getLevel( player );

		if( lvl >= item.minLevel ) {
			if( lvl < item.targetLevel ) {
				if( Parkoins.get( player ) >= item.price ) {

					Parkoins.remove( player, item.price );
					ParkourLevel.setLevel( player, item.targetLevel );

					ParkourAddonPlugin.chat.success( player, ParkourAddonPlugin.messages.parkourshopui_ranks_bought.replace( "%CATEGORY%", item.name ).replace( "%BASELEVEL%", String.valueOf( item.targetLevel ) ) );

					return true;
				} else {
					ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.parkourshopui_ranks_notenoughbalance );
				}
			} else {
				ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.parkourshopui_ranks_alreadyhave );
			}
		} else {
			ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.parkourshopui_ranks_notenoughlevel );
		}

		return false;
	}
}
