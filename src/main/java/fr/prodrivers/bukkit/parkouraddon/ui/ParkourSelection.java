package fr.prodrivers.bukkit.parkouraddon.ui;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EConfiguration;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import me.eddie.inventoryguiapi.gui.contents.UnlimitedGUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.AbstractGUIElement;
import me.eddie.inventoryguiapi.gui.elements.FormImage;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.view.BedrockGUIPresenter;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Stream;

@Singleton
public class ParkourSelection {
	private final Database database;
	private final EChat chat;
	private final EMessages messages;
	private final EConfiguration configuration;
	private final ParkourLevel parkourLevel;
	private final Course course;

	private final Map<UUID, Map<Integer, InventoryGUI>> uis = new HashMap<>();

	@Inject
	public ParkourSelection(Database database, EChat chat, EMessages messages, EConfiguration configuration, ParkourLevel parkourLevel, Course course) {
		this.database = database;
		this.chat = chat;
		this.messages = messages;
		this.configuration = configuration;
		this.parkourLevel = parkourLevel;
		this.course = course;
	}

	public void reload() {
		uis.clear();
	}

	public void reload(Player player) {
		uis.remove(player.getUniqueId());
	}

	public void reload(Player player, Integer categoryId) {
		Map<Integer, InventoryGUI> playerUis = uis.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
		playerUis.remove(categoryId);
	}

	public void open(Player player, ParkourCategory category) {
		try {
			if(!category.isHidden() || category.isHidden() && player.hasPermission("Parkour.Admin.Bypass")) {
				if(this.parkourLevel.getLevel(player) >= category.getBaseLevel() || player.hasPermission("Parkour.Admin.Bypass")) {
					Map<Integer, InventoryGUI> playerUis = uis.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
					if(!playerUis.containsKey(category.getCategoryId())) {
						playerUis.put(category.getCategoryId(), generate(player, category));
					}
					playerUis.get(category.getCategoryId()).open(player);
				} else {
					this.chat.error(player, this.messages.notenoughlevel);
				}
			} else {
				this.chat.error(player, this.messages.invalidcategory);
			}
		} catch(NullPointerException e) {
			this.chat.internalError(player);
			Log.severe("Cannot show selection UI to player " + player.getName() + " .", e);
		}
	}

	private InventoryGUI generate(Player player, ParkourCategory category) throws NullPointerException {
		String title = this.messages.parkourselectionui_title_normal
				.replace("%CAT%", category.getName())
				.replace("%CATCOLOR%", ChatColor.valueOf(category.getChatColor()).toString())
				+ ChatColor.RESET;

		if(Utils.hasBedrockSession(player)) {
			return new GUIBuilder()
					.guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION)
					.inventoryType(InventoryType.CHEST)
					.dynamicallyResizeToWrapContent(true)
					.size(54)
					.presenter(new BedrockGUIPresenter())
					.populator(new UnlimitedGUIPopulator())
					.contents(
							title,
							genContent(true, player, category),
							false,
							false,
							false
					)
					.build();
		}

		if(category.getName().length() > 8) {
			title = this.messages.parkourselectionui_title_reduced
					.replace("%CAT%", category.getName())
					.replace("%CATCOLOR%", ChatColor.valueOf(category.getChatColor()).toString())
					+ ChatColor.RESET;
		}

		return new GUIBuilder()
				.guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION)
				.inventoryType(InventoryType.CHEST)
				.dynamicallyResizeToWrapContent(true)
				.size(54)
				.contents(
						title,
						genContent(false, player, category),
						true,
						true,
						true
				)
				.build();
	}

	private List<GUIElement> genContent(boolean isBedrockContent, Player player, ParkourCategory category) throws NullPointerException {
		List<String> lores = this.messages.parkourselectionui_item_lore_normal;
		List<String> lores_completed = this.messages.parkourselectionui_item_lore_completed;
		List<GUIElement> contents = new ArrayList<>();

		if(isBedrockContent) {
			lores = this.messages.parkourselectionui_item_lore_bedrock;
		}

		SqlQuery query = this.database.sqlQuery(Utils.GET_PARKOURS_WITH_COMPLETION_QUERY);
		query.setParameter(1, Utils.getBytesFromUniqueId(player.getUniqueId()));
		query.setParameter(2, category.getCategoryId());
		List<SqlRow> results = query.findList();

		for(SqlRow row : results) {
			String internalName = row.getString("name");
			String author = row.getString("author");
			String description = row.getString("description");
			final String finalDescription = (description == null ? "" : description);
			String name = ChatColor.valueOf(row.getString("chatColor")) + row.getString("displayName");
			boolean completed = row.get("playeruuid") != null;
			Material material = Material.valueOf(row.getString("material"));
			GUIElement element;

			Stream<String> loreStream = completed ? Stream.concat(lores.stream(), lores_completed.stream()) : lores.stream();

			String[] formattedLore = loreStream
					.skip(1)
					.map(lore -> lore
							.replace("%NAME%", name)
							.replace("%AUTHOR%", author)
							.replace("%DESCRIPTION%", finalDescription)
							.split("\n")
					)
					.flatMap(Arrays::stream)
					.filter(lore -> !ChatColor.stripColor(lore).isEmpty())
					.toArray(String[]::new);
			element = createJoinParkourElement(
					completed,
					internalName,
					lores.get(0).replace("%NAME%", name)
							.replace("%AUTHOR%", author),
					material,
					formattedLore
			);
			contents.add(element);
		}

		return contents;
	}

	private GUIElement createJoinParkourElement(boolean completed, final String name, String displayName, Material material, String... lore) {
		ItemStack item = new ItemStack(material, 1);
		if(completed) {
			ItemMeta meta = item.getItemMeta();
			if(meta != null) {
				meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			item.setItemMeta(meta);
		}

		return GUIElementFactory.createActionItem(
				AbstractGUIElement.NO_DESIRED_SLOT,
				GUIElementFactory.formatItem(
						item,
						displayName,
						lore
				),
				(Callback<Player>) player -> Bukkit.getScheduler().runTaskLater(InventoryGUIAPI.getInstance(),
						() -> {
							player.closeInventory();
							this.course.join(player, name);
						}, 1L),
				completed ? this.configuration.selection_image_check : FormImage.NONE
		);
	}
}