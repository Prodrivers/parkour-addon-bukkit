package fr.prodrivers.bukkit.parkouraddon.ui;

import com.google.inject.Injector;
import fr.prodrivers.bukkit.parkouraddon.Utils;
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

@Singleton
public class ParkourShop implements Listener {
	private final Economy economy;
	private final EConfiguration configuration;
	private final EMessages messages;

	private final Injector injector;

	private Inventory inv;

	@Inject
	public ParkourShop(@Nullable Economy economy, EConfiguration configuration, EMessages messages, Injector injector) {
		this.economy = economy;
		this.configuration = configuration;
		this.messages = messages;
		// Use injector to avoid circular dependency
		this.injector = injector;
		prepare();
	}

	private ItemStack prepareItem(Material material, String shopName) {
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		if(meta != null) {
			meta.setDisplayName(shopName);
		}
		item.setItemMeta(meta);
		return item;
	}

	private void prepare() {
		inv = Bukkit.createInventory(null, 4 * 9, this.messages.parkourshopui_general_title);

		if(this.economy != null) {
			inv.setItem(
					12,
					prepareItem(
							this.configuration.shops_ranks_material,
							this.messages.parkourshopui_general_rankname
					)
			);

			inv.setItem(
					14,
					prepareItem(
							this.configuration.shops_converters_material,
							this.messages.parkourshopui_general_convertername
					)
			);
		} else {
			inv.setItem(
					13,
					prepareItem(
							this.configuration.shops_ranks_material,
							this.messages.parkourshopui_general_rankname
					)
			);
		}

		inv.setItem(
				31,
				Utils.getCloseItem(this.configuration, this.messages)
		);
	}

	public void reload() {
		prepare();
	}

	public void open(Player player) {
		player.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player player)) return;
		if(event.isCancelled()) return;

		int slot = event.getSlot();
		Inventory inventory = event.getInventory();

		if(event.getView().getTitle().equals(this.messages.parkourshopui_general_title)) {
			event.setCancelled(true);

			if(this.economy != null) {
				if(slot == 12) {
					player.closeInventory();
					this.injector.getInstance(ParkourShopRank.class).open(player);
				} else if(slot == 14) {
					player.closeInventory();
					this.injector.getInstance(ParkourShopConverter.class).open(player);
				}
			} else {
				if(slot == 13) {
					player.closeInventory();
					this.injector.getInstance(ParkourShopRank.class).open(player);
				}
			}

			if(slot == 31) {
				player.closeInventory();
				player.performCommand("bossshop shop");
			}
		}
	}

	public void unregister() {
		InventoryClickEvent.getHandlerList().unregister(this);
	}
}
