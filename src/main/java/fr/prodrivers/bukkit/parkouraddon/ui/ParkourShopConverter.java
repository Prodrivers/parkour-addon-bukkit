package fr.prodrivers.bukkit.parkouraddon.ui;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EConfiguration;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ParkourShopConverter implements Listener {
	private final Economy economy;
	private final EConfiguration configuration;
	private final EMessages messages;
	private final EChat chat;

	private final ParkourShop parkourShop;
	private final Parkoins parkoins;

	private Inventory inv;

	private final List<Integer> amounts = new ArrayList<>();
	private final List<Integer> prices = new ArrayList<>();

	@Inject
	private ParkourShopConverter(@Nullable Economy economy, EConfiguration configuration, EMessages messages, EChat chat, ParkourShop parkourShop, Parkoins parkoins) {
		this.economy = economy;
		this.configuration = configuration;
		this.messages = messages;
		this.chat = chat;
		this.parkourShop = parkourShop;
		this.parkoins = parkoins;
		prepare();
	}

	private ItemStack prepareItem(int amount, int price, Material material, String name, String... lores) {
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();

		if(meta != null) {
			meta.setDisplayName(name.replace("%AMOUNT%", String.valueOf(amount)).replace("%PRICE%", String.valueOf(price)));

			List<String> loreList = Arrays
					.stream(lores)
					.map(lore -> lore
							.replace("%AMOUNT%", String.valueOf(amount))
							.replace("%PRICE%", String.valueOf(price)))
					.collect(Collectors.toList());
			meta.setLore(loreList);
		}

		item.setItemMeta(meta);

		return item;
	}

	private void prepareAmountsPrices() {
		amounts.clear();
		prices.clear();

		int count = 0;
		for(Map.Entry<String, Integer> amountStr : this.configuration.shops_converters_amounts.entrySet()) {
			try {
				if(count < 5) {
					amounts.add(Integer.valueOf(amountStr.getKey()));
					prices.add(amountStr.getValue());
					count++;
				}
			} catch(NullPointerException e) {
				Log.warning("Amount '" + amountStr + "' configuration lacks required values.");
			}
		}
	}

	private void prepare() {
		inv = Bukkit.createInventory(null, 6 * 9, this.messages.parkourshopui_converters_title);

		prepareAmountsPrices();

		for(int i = 0; i < amounts.size(); i++) {
			inv.setItem(
					11 + i,
					prepareItem(
							amounts.get(i),
							prices.get(i),
							this.configuration.shops_converters_to_material,
							this.messages.parkourshopui_converters_to_name,
							this.messages.parkourshopui_converters_to_lore.toArray(String[]::new)
					)
			);

			inv.setItem(
					29 + i,
					prepareItem(
							prices.get(i),
							amounts.get(i),
							this.configuration.shops_converters_from_material,
							this.messages.parkourshopui_converters_from_name,
							this.messages.parkourshopui_converters_from_lore.toArray(String[]::new)
					)
			);
		}

		inv.setItem(
				49,
				Utils.getCloseItem(this.configuration, this.messages)
		);
	}

	public void reload() {
		prepare();
	}

	void open(Player player) {
		if(this.economy != null) {
			player.openInventory(inv);
		} else {
			this.chat.error(player, this.messages.parkourshopui_converters_unavailable);
			Log.severe("Player " + player.getName() + " tried to access currency conversion UI, but no compatible economy plugin was found.");
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player player)) return;
		if(event.isCancelled()) return;

		int slot = event.getSlot();
		Inventory inventory = event.getInventory();

		if(event.getView().getTitle().equals(this.messages.parkourshopui_converters_title)) {
			event.setCancelled(true);

			if(slot >= 11 && slot <= 15) {
				if(convertCoinsToParkoins(player, amounts.get(slot - 11), prices.get(slot - 11)))
					player.closeInventory();
			} else if(slot >= 29 && slot <= 33) {
				if(convertCoinsFromParkoins(player, prices.get(slot - 29), amounts.get(slot - 29)))
					player.closeInventory();
			} else if(slot == 49) {
				player.closeInventory();
				this.parkourShop.open(player);
			}
		}
	}

	private boolean convertCoinsToParkoins(Player player, int amount, int price) {
		if(this.economy.getBalance(player) >= price) {

			this.economy.withdrawPlayer(player, price);
			this.parkoins.add(player, amount);

			this.chat.success(player, this.messages.parkourshopui_converters_to_bought.replace("%AMOUNT%", String.valueOf(amount)).replace("%PRICE%", String.valueOf(price)));

			return true;
		} else {
			this.chat.error(player, this.messages.parkourshopui_converters_to_notenoughbalance);
		}

		return false;
	}

	private boolean convertCoinsFromParkoins(Player player, int amount, int price) {
		if(this.parkoins.get(player) >= price) {

			this.parkoins.remove(player, price);
			this.economy.depositPlayer(player, amount);

			this.chat.success(player, this.messages.parkourshopui_converters_from_bought.replace("%AMOUNT%", String.valueOf(amount)).replace("%PRICE%", String.valueOf(price)));

			return true;
		} else {
			this.chat.error(player, this.messages.parkourshopui_converters_from_notenoughbalance);
		}

		return false;
	}

	public void unregister() {
		InventoryClickEvent.getHandlerList().unregister(this);
	}
}
