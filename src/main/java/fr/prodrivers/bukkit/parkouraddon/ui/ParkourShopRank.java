package fr.prodrivers.bukkit.parkouraddon.ui;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EConfiguration;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ParkourShopRank implements Listener {
	private final Database database;
	private final EConfiguration configuration;
	private final EMessages messages;
	private final EChat chat;

	private final ParkourShop parkourShop;
	private final ParkourLevel parkourLevel;
	private final Parkoins parkoins;

	private final ArrayList<RankItem> rankItems = new ArrayList<>(), boughtRankItems = new ArrayList<>(), notBuyableRankItems = new ArrayList<>();

	private int lines, closeSlot;

	private static class RankItem implements Cloneable {
		final String name;
		final int price;
		final int minLevel;
		final int targetLevel;
		final Material material;
		ItemStack item;
		int slot;
		final ChatColor chatColor;

		RankItem(String name, int price, int minLevel, int targetLevel, ChatColor chatColor, Material material) {
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
			} catch(CloneNotSupportedException cnse) {
				cnse.printStackTrace(System.err);
			}

			return item;
		}
	}

	@Inject
	private ParkourShopRank(Database database, EConfiguration configuration, EMessages messages, EChat chat, ParkourShop parkourShop, ParkourLevel parkourLevel, Parkoins parkoins) {
		this.database = database;
		this.configuration = configuration;
		this.messages = messages;
		this.chat = chat;
		this.parkourShop = parkourShop;
		this.parkourLevel = parkourLevel;
		this.parkoins = parkoins;
		prepare();
	}

	private RankItem prepareItem(RankItem rankItem, Material material, String rankName, String rankLore1, String rankLore2) {
		return prepareItem(rankItem, material, rankName, rankLore1, rankLore2, null);
	}

	private RankItem prepareItem(RankItem rankItem, Material material, String rankName, String... lores) {
		rankItem.item = new ItemStack(material, 1);
		ItemMeta meta = rankItem.item.getItemMeta();

		if(meta != null) {
			meta.setDisplayName(rankName.replace("%CATEGORYCOLOR%", rankItem.chatColor.toString()).replace("%CATEGORY%", rankItem.name).replace("%PRICE%", String.valueOf(rankItem.price)).replace("%TARGETLEVEL%", String.valueOf(rankItem.targetLevel)));

			List<String> loreList = Arrays
					.stream(lores)
					.map(lore -> lore
							.replace("%CATEGORYCOLOR%", rankItem.chatColor.toString())
							.replace("%CATEGORY%", rankItem.name)
							.replace("%PRICE%", String.valueOf(rankItem.price))
							.replace("%TARGETLEVEL%", String.valueOf(rankItem.targetLevel))
							.replace("%MINLEVEL%", String.valueOf(rankItem.minLevel)))
					.collect(Collectors.toList());

			meta.setLore(loreList);
		}

		rankItem.item.setItemMeta(meta);

		return rankItem;
	}

	private RankItem prepareItem(RankItem rankItem) {
		return prepareItem(
				rankItem,
				rankItem.material,
				this.messages.parkourshopui_ranks_rankitemname,
				this.messages.parkourshopui_ranks_rankitemlore.toArray(String[]::new)
		);
	}

	private RankItem prepareBoughtItem(RankItem rankItem) {
		return prepareItem(
				rankItem,
				this.configuration.shops_ranks_alreadyBought_material,
				this.messages.parkourshopui_ranks_boughtrankitemname,
				this.messages.parkourshopui_ranks_boughtrankitemlore.toArray(String[]::new)
		);
	}

	private RankItem prepareNotBuyableItem(RankItem rankItem) {
		return prepareItem(
				rankItem,
				this.configuration.shops_ranks_notBuyable_material,
				this.messages.parkourshopui_ranks_notbuyablerankitemname,
				this.messages.parkourshopui_ranks_notbuyablerankitemlore.toArray(String[]::new)
		);
	}

	private void prepareItems() {
		RankItem item;
		int count = 0;

		rankItems.clear();
		boughtRankItems.clear();
		notBuyableRankItems.clear();

		List<ParkourCategory> categories = ParkourCategory.retrieveAll(this.database);

		for(ParkourCategory category : categories) {
			try {
				if(count < 45 && category.getBaseLevel() > 0 && category.getPrice() > 0) {
					ParkourCategory prevCat = null;
					if(category.getPreviousCategory() != null)
						prevCat = category.forceGetPreviousCategory(this.database);
					item = new RankItem(
							category.getName(),
							category.getPrice(),
							(prevCat != null ? prevCat.getBaseLevel() : 0),
							category.getBaseLevel(),
							ChatColor.valueOf(category.getChatColor()),
							Material.valueOf(category.getMaterial())
					);
					rankItems.add(prepareItem(item));
					boughtRankItems.add(prepareBoughtItem(item.clone()));
					notBuyableRankItems.add(prepareNotBuyableItem(item.clone()));
					count++;
				}
			} catch(Exception e) {
				Log.warning("Rank '" + category.getName() + "' configuration lacks required values.", e);
			}
		}
	}

	private void prepareSlots() {
		boolean ending = false;
		int remainder = rankItems.size() % 9;
		lines = rankItems.size() / 9 + (remainder != 0 ? 1 : 0);
		int size = rankItems.size(), left = size;
		int currentSlot = 0, slotOffset = 0;

		for(int i = 0; i < size; i++) {
			if(left <= remainder) {
				if(!ending) {
					slotOffset = currentSlot;
					currentSlot = (9 - remainder) / 2;
					ending = true;
				}
			}
			rankItems.get(i).slot = currentSlot + slotOffset;
			boughtRankItems.get(i).slot = currentSlot + slotOffset;
			notBuyableRankItems.get(i).slot = currentSlot + slotOffset;
			currentSlot++;
			left--;
		}

		if(lines < 5) {
			lines += 2;
		} else {
			lines++;
		}

		closeSlot = (lines - 1) * 9 + 4;
	}

	private void prepare() {
		prepareItems();
		prepareSlots();
	}

	public void reload() {
		prepare();
	}

	private boolean isBought(Player player, RankItem item) {
		return (this.parkourLevel.getLevel(player) >= item.targetLevel);
	}

	private boolean isBuyable(Player player, RankItem item) {
		return (this.parkoins.get(player) >= item.price && this.parkourLevel.getLevel(player) >= item.minLevel);
	}

	void open(Player player) {
		Inventory inv = Bukkit.createInventory(null, lines * 9, this.messages.parkourshopui_ranks_title);
		RankItem item, boughtItem, notBuyableItem;

		for(int i = 0; i < rankItems.size(); i++) {
			item = rankItems.get(i);

			if(isBought(player, item)) {
				boughtItem = boughtRankItems.get(i);
				inv.setItem(boughtItem.slot, boughtItem.item);
			} else if(isBuyable(player, item)) {
				inv.setItem(item.slot, item.item);
			} else {
				notBuyableItem = notBuyableRankItems.get(i);
				inv.setItem(notBuyableItem.slot, notBuyableItem.item);
			}
		}

		inv.setItem(
				closeSlot,
				Utils.getCloseItem(this.configuration, this.messages)
		);

		player.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player player)) return;
		if(event.isCancelled()) return;

		ItemStack clicked = event.getCurrentItem();

		if(event.getView().getTitle().equals(this.messages.parkourshopui_ranks_title)) {
			event.setCancelled(true);

			if(event.getSlot() == closeSlot) {
				player.closeInventory();
				this.parkourShop.open(player);
			} else if(clicked != null && clicked.getItemMeta() != null) {
				for(RankItem item : rankItems) {
					ItemMeta currentRankItemMeta = item.item.getItemMeta();
					if(currentRankItemMeta != null) {
						if(clicked.getItemMeta().getDisplayName().equals(currentRankItemMeta.getDisplayName())) {
							if(buyRank(player, item))
								player.closeInventory();
							break;
						}
					}
				}
			}
		}
	}

	private boolean buyRank(Player player, RankItem item) {
		int lvl = this.parkourLevel.getLevel(player);

		if(lvl >= item.minLevel) {
			if(lvl < item.targetLevel) {
				if(this.parkoins.get(player) >= item.price) {

					this.parkoins.remove(player, item.price);
					this.parkourLevel.setLevel(player, item.targetLevel);

					this.chat.success(player, this.messages.parkourshopui_ranks_bought.replace("%CATEGORY%", item.name).replace("%BASELEVEL%", String.valueOf(item.targetLevel)));

					return true;
				} else {
					this.chat.error(player, this.messages.parkourshopui_ranks_notenoughbalance);
				}
			} else {
				this.chat.error(player, this.messages.parkourshopui_ranks_alreadyhave);
			}
		} else {
			this.chat.error(player, this.messages.parkourshopui_ranks_notenoughlevel);
		}

		return false;
	}

	public void unregister() {
		InventoryClickEvent.getHandlerList().unregister(this);
	}
}
