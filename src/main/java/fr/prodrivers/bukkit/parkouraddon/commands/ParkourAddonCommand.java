package fr.prodrivers.bukkit.parkouraddon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import fr.prodrivers.bukkit.parkouraddon.plugin.EChat;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import fr.prodrivers.bukkit.parkouraddon.plugin.Main;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourSelection;
import fr.prodrivers.bukkit.parkouraddon.ui.ParkourShop;
import io.ebean.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@CommandAlias("paddon")
public class ParkourAddonCommand extends BaseCommand {
	public final EMessages messages;
	public final EChat chat;
	public final Database database;
	public final ParkourSelection parkourSelectionUi;
	public final ParkourShop parkourShopUi;
	public final Main plugin;
	public final Course course;
	public final Parkoins parkoins;
	public final ParkourLevel parkourLevel;

	@Inject
	public ParkourAddonCommand(EMessages messages, EChat chat, Database database, ParkourSelection parkourSelectionUi, ParkourShop parkourShopUi, Main plugin, Course course, ParkourLevel parkourLevel, Parkoins parkoins) {
		this.messages = messages;
		this.chat = chat;
		this.database = database;
		this.parkourSelectionUi = parkourSelectionUi;
		this.parkourShopUi = parkourShopUi;
		this.plugin = plugin;
		this.course = course;
		this.parkourLevel = parkourLevel;
		this.parkoins = parkoins;
	}

	@HelpCommand
	public void onHelp(CommandSender sender, CommandHelp help) {
		if(sender.hasPermission("parkouraddon.help")) {
			help.showHelp();
		} else {
			this.chat.send(sender, "Usage:");
			this.chat.send(sender, "/paddon open <category>");
			this.chat.send(sender, "/paddon shop");
		}
	}

	@Subcommand("reload")
	@CommandPermission("parkouraddon.reload")
	public void reload(CommandSender sender) {
		try {
			this.plugin.reload();
			this.chat.success(sender, this.messages.configurationreloaded);
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Error while reloading ParkourAddon.", e);
		}
	}

	@Subcommand("shop")
	public void openShop(Player player) {
		try {
			this.parkourShopUi.open(player);
		} catch(Exception e) {
			this.chat.internalError(player);
			Log.severe("Error while trying to open Parkour Shop UI.", e);
		}
	}

	@Default
	@Subcommand("open")
	@Syntax("<category>")
	@CommandCompletion("@categoryName")
	public void openSelection(Player player, String categoryName) {
		try {
			ParkourCategory cat = ParkourCategory.retrieveFromName(this.database, categoryName);
			if(cat != null) {
				this.parkourSelectionUi.open(player, cat);
			} else {
				this.chat.error(player, this.messages.invalidcategory);
			}
		} catch(Exception e) {
			this.chat.internalError(player);
			Log.severe("Error while trying to open Parkour Selection UI.", e);
		}
	}

	@Subcommand("category list")
	@CommandPermission("parkouraddon.category.list")
	public void listCategories(CommandSender sender) {
		List<ParkourCategory> categories = ParkourCategory.retrieveAll(this.database);

		if(categories.size() > 0) {
			this.chat.send(sender, this.messages.categoryentryheader);
			for(ParkourCategory cat : categories) {
				this.chat.send(sender,
						this.messages.categoryentry
								.replace("%ID%", String.valueOf(cat.getCategoryId()))
								.replace("%NAME%", cat.getName())
								.replace("%BASELEVEL%", String.valueOf(cat.getBaseLevel()))
								.replace("%PREVID%", String.valueOf((cat.getPreviousCategory() != null ? cat.getPreviousCategory().getCategoryId() : "None")))
								.replace("%HEXCOLOR%", "#" + Integer.toHexString(cat.getHexColor()))
								.replace("%CHATCOLOR%", cat.getChatColor())
								.replace("%MATERIAL%", cat.getMaterial())
				);
			}
		} else {
			this.chat.error(sender, this.messages.nocategoryentries);
		}
	}

	@Subcommand("category add")
	@CommandPermission("parkouraddon.category.add")
	@Syntax("<name> <baseLevel> <material> <chatColor> <hexColor>")
	@CommandCompletion("@nothing @nothing @materials @chatcolors @nothing")
	public void addCategory(CommandSender sender, String name, Integer baseLevel, Material material, ChatColor chatColor, String hexColor) {
		try {
			ParkourCategory cat = new ParkourCategory();
			cat.setName(name);
			cat.setBaseLevel(baseLevel);
			cat.setMaterial(material.toString());
			cat.setChatColor(chatColor.toString());
			cat.setHexColor(Utils.parseColor(hexColor));
			this.database.save(cat);
			this.chat.success(sender, this.messages.categoryadded.replace("%CATID%", String.valueOf(cat.getCategoryId())));
		} catch(IllegalArgumentException e) {
			this.chat.error(sender, this.messages.invalidargument);
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Error while adding category.", e);
		}
	}

	@Subcommand("category setprevious")
	@CommandPermission("parkouraddon.category.setprevious")
	@Syntax("<category> <previousCategoryName>")
	@CommandCompletion("@categoryName @categoryName")
	public void setPreviousCategory(CommandSender sender, String categoryName, String previousCategoryName) {
		try {
			ParkourCategory cat = ParkourCategory.retrieveFromName(this.database, categoryName);
			if(cat != null) {
				if(previousCategoryName.isEmpty() || previousCategoryName.equalsIgnoreCase("null")) {
					cat.setPreviousCategory(null);
					this.database.save(cat);
					this.chat.success(sender, this.messages.prevcategoryset.replace("%CAT%", cat.getName()).replace("%PREVCAT%", "None"));
				} else {
					ParkourCategory prevcat = this.database.find(ParkourCategory.class, Integer.valueOf(previousCategoryName));
					if(prevcat != null) {
						cat.setPreviousCategory(prevcat);
						this.database.save(cat);
						this.chat.success(sender, this.messages.prevcategoryset.replace("%CAT%", cat.getName()).replace("%PREVCAT%", prevcat.getName()));
					} else {
						this.chat.error(sender, this.messages.invalidcategory);
					}
				}
			} else {
				this.chat.error(sender, this.messages.invalidcategory);
			}
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Error while adding category.", e);
		}
	}

	@Subcommand("category setreqcoursesnbinprevcat")
	@CommandPermission("parkouraddon.category.setreqcoursesnbinprevcat")
	@Syntax("<category> <requiredCoursesNumberInPreviousCategoryForRankup>")
	@CommandCompletion("@categoryName @nothing")
	public void setRequiredCoursesNumberInPreviousCategoryForRankup(CommandSender sender, String categoryName, Integer requiredCoursesNumberInPreviousCategoryForRankup) {
		try {
			ParkourCategory cat = ParkourCategory.retrieveFromName(this.database, categoryName);
			if(cat != null) {
				cat.setRequiredCoursesNumberInPreviousCategoryForRankup(requiredCoursesNumberInPreviousCategoryForRankup);
				this.database.save(cat);
				this.chat.success(sender, this.messages.reqcoursesnbinprevcatset.replace("%CAT%", cat.getName()).replace("%NUMBER%", requiredCoursesNumberInPreviousCategoryForRankup.toString()));
			} else {
				this.chat.error(sender, this.messages.invalidcategory);
			}
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Error while setting category's required courses number in previous category for rankup.", e);
		}
	}

	@Subcommand("category setparkoinsreward")
	@CommandPermission("parkouraddon.category.setparkoinsreward")
	@Syntax("<category> <parkoinsReward>")
	@CommandCompletion("@categoryName @nothing")
	public void setParkoinsReward(CommandSender sender, String categoryName, Integer parkoinsReward) {
		try {
			ParkourCategory cat = ParkourCategory.retrieveFromName(this.database, categoryName);
			if(cat != null) {
				cat.setParkoinsReward(parkoinsReward);
				this.database.save(cat);
				this.chat.success(sender, this.messages.parkoinsrewardcategoryset.replace("%CAT%", cat.getName()).replace("%REWARD%", parkoinsReward.toString()));
			} else {
				this.chat.error(sender, this.messages.invalidcategory);
			}
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot set category's parkoins reward.", e);
		}
	}

	@Subcommand("category sethidden")
	@CommandPermission("parkouraddon.category.sethidden")
	@Syntax("<category> <isHidden>")
	@CommandCompletion("@categoryName @boolean")
	public void setHidden(CommandSender sender, String categoryName, Boolean isHidden) {
		try {
			ParkourCategory cat = ParkourCategory.retrieveFromName(this.database, categoryName);
			if(cat != null) {
				cat.setHidden(isHidden);
				this.database.save(cat);
				this.chat.success(sender, (cat.isHidden() ? this.messages.categorysettohidden : this.messages.categorysettoshown).replace("%CAT%", cat.getName()));
			} else {
				this.chat.error(sender, this.messages.invalidcategory);
			}
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot set category hidden state.", e);
		}
	}

	@Subcommand("category setbaselevel")
	@CommandPermission("parkouraddon.category.setbaselevel")
	@Syntax("<category> <baseLevel>")
	@CommandCompletion("@categoryName @nothing")
	public void setBaseLevel(CommandSender sender, String categoryName, Integer baseLevel) {
		try {
			ParkourCategory category = ParkourCategory.retrieveFromName(this.database, categoryName);
			if(category != null) {
				category.setBaseLevel(baseLevel);
				for(ParkourCourse course : category.getCourses()) {
					this.course.setMinimumLevel(course.getName(), baseLevel);
				}
				this.chat.success(sender, this.messages.categorybaselevelset.replace("%CAT%", category.getName()).replace("%LEVEL%", baseLevel.toString()));
			} else {
				this.chat.error(sender, this.messages.invalidcategory);
			}
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot set category's base level.", e);
		}
	}

	@Subcommand("parkour setcategory")
	@CommandPermission("parkouraddon.parkour.setcategory")
	@Syntax("<parkour> <category>")
	@CommandCompletion("@parkourName @categoryName")
	public void setParkourCategory(CommandSender sender, String parkourName, String categoryName) {
		try {
			ParkourCourse course = ParkourCourse.retrieveFromName(this.database, parkourName);
			if(course != null) {
				if(categoryName.isEmpty() || categoryName.equalsIgnoreCase("null")) {
					course.setCategory(this.course, null);
					this.database.save(course);
					this.chat.success(sender, this.messages.parkourcategoryset.replace("%COURSENAME%", course.getName()).replace("%CAT%", "None"));
				} else {
					ParkourCategory cat = ParkourCategory.retrieveFromName(this.database, categoryName);
					if(cat != null) {
						course.setCategory(this.course, cat);
						this.database.save(course);
						this.chat.success(sender, this.messages.parkourcategoryset.replace("%COURSENAME%", course.getName()).replace("%CAT%", cat.getName()));
					} else {
						this.chat.error(sender, this.messages.invalidcategory);
					}
				}
			} else {
				this.chat.error(sender, this.messages.invalidcourse);
			}
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot set parkour's category.", e);
		}
	}

	@Subcommand("parkour setdisplayname")
	@CommandPermission("parkouraddon.parkour.setdisplayname")
	@Syntax("<parkour> <displayName>")
	@CommandCompletion("@parkourName @nothing")
	public void setParkourDisplayName(CommandSender sender, String parkourName, String displayName) {
		ParkourCourse course = ParkourCourse.retrieveFromName(this.database, parkourName);
		if(course != null) {
			course.setDisplayName(displayName);
			this.database.save(course);
			this.chat.success(sender, this.messages.parkourdisplaynameset.replace("%COURSENAME%", course.getName()).replace("%DISPLAYNAME%", course.getDisplayName()));
		} else {
			this.chat.error(sender, this.messages.invalidcourse);
		}
	}

	@Subcommand("parkour setdescription")
	@CommandPermission("parkouraddon.parkour.setdescription")
	@Syntax("<parkour> <description> [additional description line...]")
	@CommandCompletion("@parkourName @nothing")
	public void setParkourDescription(CommandSender sender, String parkourName, String[] descriptionArgsSplit) {
		ParkourCourse course = ParkourCourse.retrieveFromName(this.database, parkourName);
		if(course != null) {
			// Join arguments with spaces
			String descriptionArgsNotParsed = String.join(" ", descriptionArgsSplit);
			// Split them, being aware of double-quotes
			List<String> descriptionArgs = Arrays.stream(Utils.parseArgumentsWithQuotes(descriptionArgsNotParsed)).map(s -> {
				if(s.startsWith("\"")) {
					s = s.substring(1);
				}
				if(s.endsWith("\"")) {
					s = s.substring(0, s.length() - 1);
				}
				return s;
			}).collect(Collectors.toList());
			// Join them with line returns
			String description = String.join("\n", descriptionArgs);

			course.setDescription(description);
			this.database.save(course);
			this.chat.success(sender, this.messages.parkourdescriptionset.replace("%COURSENAME%", course.getName()).replace("%DESCRIPTION%", course.getDescription()));
		} else {
			this.chat.error(sender, this.messages.invalidcourse);
		}
	}

	@Subcommand("parkour setmcversion")
	@CommandPermission("parkouraddon.parkour.setmcversion")
	@Syntax("<parkour> <protocol version>")
	@CommandCompletion("@parkourName @nothing")
	public void setParkourMcVersion(CommandSender sender, String parkourName, @Default() String protocolVersion) {
		ParkourCourse course = ParkourCourse.retrieveFromName(this.database, parkourName);
		if(course != null) {
			try {
				if(protocolVersion.equals("null")) {
					course.setMinimumProtocolVersion(null);
				} else if(!protocolVersion.isEmpty()) {
					int version = Integer.parseInt(protocolVersion);
					course.setMinimumProtocolVersion(version);
				} else {
					@SuppressWarnings("unchecked") ViaAPI<Player> api = (ViaAPI<Player>) Via.getAPI();
					if(api != null) {
						if(sender instanceof Player) {
							course.setMinimumProtocolVersion(api.getPlayerVersion((Player) sender));
						} else {
							this.chat.error(sender, this.messages.not_a_player);
						}
					}
				}
				this.database.save(course);
				this.chat.success(sender, this.messages.parkourminimumprotocolversionset.replace("%COURSENAME%", course.getName()).replace("%PROTOCOLVERSION%", String.valueOf(course.getMinimumProtocolVersion())));
			} catch(NumberFormatException e) {
				this.chat.error(sender, this.messages.invalid_number);
			}
		} else {
			this.chat.error(sender, this.messages.invalidcourse);
		}
	}

	@Subcommand("player setlevel")
	@CommandPermission("parkouraddon.player.setlevel")
	@Syntax("<player> <level>")
	@CommandCompletion("@player @nothing")
	public void setPlayerLevel(CommandSender sender, Player player, Integer level) {
		try {
			this.parkourLevel.setLevel(player, level);
			this.chat.success(sender, this.messages.parkourlevelset.replace("%PLAYER%", player.getName()).replace("%LEVEL%", level.toString()));
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot set player's level.", e);
		}
	}

	@Subcommand("player addparkoins")
	@CommandPermission("parkouraddon.player.addparkoins")
	@Syntax("<player> <parkoins>")
	@CommandCompletion("@player @nothing")
	public void addParkoins(CommandSender sender, Player player, Integer parkoins) {
		try {
			this.parkoins.add(player, parkoins);
			Log.info("Player " + sender.getName() + " added " + parkoins + " parkoins to player " + player.getName());
			this.chat.success(sender, this.messages.parkoinsadd.replace("%PLAYER%", player.getName()).replace("%PARKOINS%", parkoins.toString()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send Colasix " + sender.getName() + " a ajouté " + parkoins + " parkoins à " + player.getName());
		} catch(NumberFormatException e) {
			this.chat.error(sender, this.messages.invalid_number);
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot add parkoins to player.", e);
		}
	}

	@Subcommand("player deductparkoins")
	@CommandPermission("parkouraddon.player.deductparkoins")
	@Syntax("<player> <parkoins>")
	@CommandCompletion("@player @nothing")
	public void removeParkoins(CommandSender sender, Player player, Integer parkoins) {
		try {
			this.parkoins.remove(player, parkoins);
			Log.info("Player " + sender.getName() + " deducted " + parkoins + " parkoins to player " + player.getName());
			this.chat.success(sender, this.messages.parkoinsremove.replace("%PLAYER%", player.getName()).replace("%PARKOINS%", parkoins.toString()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send Colasix " + sender.getName() + " a retiré " + parkoins + " parkoins à " + player.getName());
		} catch(NumberFormatException e) {
			this.chat.error(sender, this.messages.invalid_number);
		} catch(Exception e) {
			this.chat.internalError(sender);
			Log.severe("Cannot deduct parkoins from player.", e);
		}
	}
}
