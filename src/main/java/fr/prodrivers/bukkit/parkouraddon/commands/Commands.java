package fr.prodrivers.bukkit.parkouraddon.commands;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class Commands implements CommandExecutor {
	private final EMessages messages;
	private final EChat chat;
	private final Database database;
	private final ParkourSelection parkourSelectionUi;
	private final ParkourShop parkourShopUi;
	private final Main plugin;
	private final Course course;
	private final Parkoins parkoins;
	private final ParkourLevel parkourLevel;

	@Inject
	public Commands(EMessages messages, EChat chat, Database database, ParkourSelection parkourSelectionUi, ParkourShop parkourShopUi, Main plugin, Course course, ParkourLevel parkourLevel, Parkoins parkoins) {
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

	public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, String label, @Nonnull String[] args) {
		if(label.equalsIgnoreCase("paddon")) {
			if(args.length > 0) {
				switch(args[0]) {
					case "shop":
						return openShop(sender);
					case "open":
						return openSelection(sender, args);
					case "listcategories":
						return listCategories(sender);
					case "addcategory":
						return addCategory(sender, args);
					case "setpreviouscategory":
						return setPreviousCategory(sender, args);
					case "setreqcoursesnbinprevcat":
						return setRequiredCoursesNumberInPreviousCategoryForRankup(sender, args);
					case "setparkoinsreward":
						return setParkoinsReward(sender, args);
					case "sethidden":
						return setHidden(sender, args);
					case "setbaselevel":
						return setBaseLevel(sender, args);
					case "setparkourcategory":
						return setParkourCategory(sender, args);
					case "setparkourdisplayname":
						return setParkourDisplayName(sender, args);
					case "setparkourdescription":
						return setParkourDescription(sender, args);
					case "setparkourmcversion":
						return setParkourMcVersion(sender, args);
					case "setplayerlevel":
						return setPlayerLevel(sender, args);
					case "addparkoins":
						return addParkoins(sender, args);
					case "removeparkoins":
						return removeParkoins(sender, args);
					case "reload":
						return reload(sender);
					case "help":
						return help(sender);
				}
			} else {
				if(sender.hasPermission("parkouraddon.help")) {
					this.chat.error(sender, "Unknown command, use /paddon help");
					return true;
				}
			}
		}

		return false;
	}

	private boolean help(CommandSender sender) {
		if(sender.hasPermission("parkouraddon.help")) {
			this.chat.send(sender, "=== USAGE ===");
			this.chat.send(sender, "--- General ---");
			this.chat.send(sender, "/paddon shop");
			this.chat.send(sender, "/paddon reload");
			this.chat.send(sender, "--- Categories ---");
			this.chat.send(sender, "/paddon listcategories");
			this.chat.send(sender, "/paddon addcategory <name> <baseLevel> <material> <materialDataValue> <chatColor> <hexColor>");
			this.chat.send(sender, "/paddon setpreviouscategory <categoryId> <previousCategoryId>");
			this.chat.send(sender, "/paddon setreqcoursesnbinprevcat <categoryId> <numberOfParkourToComplete>");
			this.chat.send(sender, "/paddon setparkoinsreward <categoryId> <parkoinsReward>");
			this.chat.send(sender, "/paddon sethidden <categoryId> <isHidden>");
			this.chat.send(sender, "--- Parkours ---");
			this.chat.send(sender, "/paddon open <categoryId>");
			this.chat.send(sender, "/paddon setparkourcategory <courseName> <categoryId>");
			this.chat.send(sender, "/paddon setparkourdisplayname <courseName> <displayName>");
			this.chat.send(sender, "/paddon setparkourdescription <courseName> <line 1> [line 2] ...");
			this.chat.send(sender, "/paddon setparkourmcversion <courseName> [protocolVersion]");
			this.chat.send(sender, "--- Levels ---");
			this.chat.send(sender, "/paddon setlevel <playerName> <level>");
			this.chat.send(sender, "--- Parkoins ---");
			this.chat.send(sender, "/paddon addparkoins <playerName> <parkoins>");
			this.chat.send(sender, "/paddon removeparkoins <playerName> <parkoins>");
		} else {
			this.chat.send(sender, "Usage:");
			this.chat.send(sender, "/paddon open <category>");
			this.chat.send(sender, "/paddon shop");
		}
		return true;
	}

	private boolean reload(CommandSender sender) {
		if(sender.hasPermission("parkouraddon.reload")) {
			this.plugin.reload();
			return true;
		}
		return false;
	}

	private boolean openShop(CommandSender sender) {
		if(sender instanceof Player) {
			try {
				this.parkourShopUi.open((Player) sender);
			} catch(Exception e) {
				this.chat.internalError(sender);
				Log.severe("Error while trying to open Parkour Shop UI.", e);
			}
		} else {
			this.chat.error(sender, this.messages.notaplayer);
		}
		return true;
	}

	private boolean openSelection(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			if(args.length > 1) {
				try {
					ParkourCategory cat = this.database.find(ParkourCategory.class, Integer.valueOf(args[1]));
					if(cat != null) {
						this.parkourSelectionUi.open((Player) sender, cat);
					} else {
						this.chat.error(sender, this.messages.invalidcategory);
					}
				} catch(NumberFormatException e) {
					this.chat.error(sender, this.messages.invalidnumber);
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
				} catch(IllegalArgumentException e) {
					this.chat.error(sender, this.messages.invalidargument);
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
				} catch(Exception e) {
					this.chat.internalError(sender);
					Log.severe("Error while trying to open Parkour Selection UI.", e);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
		} else {
			this.chat.error(sender, this.messages.notaplayer);
		}
		return true;
	}

	private boolean listCategories(CommandSender sender) {
		if(sender.hasPermission("parkouraddon.category.list")) {
			List<ParkourCategory> categories = this.database.find(ParkourCategory.class).select("*").findList();

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
			return true;
		}
		return false;
	}

	private boolean addCategory(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.category.add")) {
			if(args.length > 6) {
				try {
					ParkourCategory cat = new ParkourCategory();
					cat.setName(args[1]);
					cat.setBaseLevel(Integer.parseInt(args[2]));
					cat.setMaterial(Material.valueOf(args[3]).toString());
					cat.setChatColor(ChatColor.valueOf(args[5]).toString());
					cat.setHexColor(Utils.parseColor(args[6]));
					this.database.save(cat);
					this.chat.success(sender, this.messages.categoryadded.replace("%CATID%", String.valueOf(cat.getCategoryId())));
				} catch(NumberFormatException e) {
					this.chat.error(sender, this.messages.invalidnumber);
				} catch(IllegalArgumentException e) {
					this.chat.error(sender, this.messages.invalidargument);
				} catch(Exception e) {
					this.chat.internalError(sender);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setPreviousCategory(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.category.setprevious")) {
			if(args.length > 2) {
				try {
					ParkourCategory cat = this.database.find(ParkourCategory.class, Integer.valueOf(args[1]));
					if(cat != null) {
						if(args[2].equalsIgnoreCase("null")) {
							cat.setPreviousCategory(null);
							this.database.save(cat);
							this.chat.success(sender, this.messages.prevcategoryset.replace("%CAT%", cat.getName()).replace("%PREVCAT%", "None"));
						} else {
							ParkourCategory prevcat = this.database.find(ParkourCategory.class, Integer.valueOf(args[2]));
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
				} catch(NumberFormatException e) {
					this.chat.error(sender, this.messages.invalidnumber);
				} catch(Exception e) {
					this.chat.internalError(sender);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setRequiredCoursesNumberInPreviousCategoryForRankup(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.category.setreqcoursesnbinprevcat")) {
			if(args.length > 2) {
				try {
					ParkourCategory cat = this.database.find(ParkourCategory.class, Integer.valueOf(args[1]));
					if(cat != null) {
						cat.setRequiredCoursesNumberInPreviousCategoryForRankup(Integer.parseInt(args[2]));
						this.database.save(cat);
						this.chat.success(sender, this.messages.reqcoursesnbinprevcatset.replace("%CAT%", cat.getName()).replace("%NUMBER%", args[2]));
					} else {
						this.chat.error(sender, this.messages.invalidcategory);
					}
				} catch(NumberFormatException e) {
					this.chat.error(sender, this.messages.invalidnumber);
				} catch(Exception e) {
					this.chat.internalError(sender);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setParkoinsReward(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.category.setparkoinsreward")) {
			if(args.length > 2) {
				try {
					ParkourCategory cat = this.database.find(ParkourCategory.class, Integer.valueOf(args[1]));
					if(cat != null) {
						cat.setParkoinsReward(Integer.parseInt(args[2]));
						this.database.save(cat);
						this.chat.success(sender, this.messages.parkoinsrewardcategoryset.replace("%CAT%", cat.getName()).replace("%REWARD%", args[2]));
					} else {
						this.chat.error(sender, this.messages.invalidcategory);
					}
				} catch(NumberFormatException e) {
					this.chat.error(sender, this.messages.invalidnumber);
				} catch(Exception e) {
					this.chat.internalError(sender);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setHidden(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.category.sethidden")) {
			if(args.length > 2) {
				try {
					ParkourCategory cat = this.database.find(ParkourCategory.class, Integer.valueOf(args[1]));
					if(cat != null) {
						cat.setHidden(Boolean.parseBoolean(args[2]));
						this.database.save(cat);
						this.chat.success(sender, (cat.isHidden() ? this.messages.categorysettohidden : this.messages.categorysettoshown).replace("%CAT%", cat.getName()));
					} else {
						this.chat.error(sender, this.messages.invalidcategory);
					}
				} catch(Exception e) {
					this.chat.internalError(sender);
					Log.severe("Cannot set category hidden state.", e);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setBaseLevel(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.category.setbaselevel")) {
			if(args.length > 2) {
				ParkourCategory category = this.database.find(ParkourCategory.class, Integer.valueOf(args[1]));
				if(category != null) {
					try {
						int level = Integer.parseInt(args[2]);
						category.setBaseLevel(level);
						for(ParkourCourse course : category.getCourses()) {
							this.course.setMinimumLevel(course.getName(), level);
						}
						this.chat.success(sender, this.messages.categorybaselevelset.replace("%CAT%", category.getName()).replace("%LEVEL%", args[2]));
					} catch(NumberFormatException e) {
						this.chat.error(sender, this.messages.invalidnumber);
					} catch(Exception e) {
						this.chat.internalError(sender);
					}
				} else {
					this.chat.error(sender, this.messages.invalidcategory);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setParkourCategory(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.parkour.setcategory")) {
			if(args.length > 2) {
				try {
					ParkourCourse course = ParkourCourse.retrieveFromName(this.database, args[1]);
					if(course != null) {
						if(args[2].equalsIgnoreCase("null")) {
							course.setCategory(this.course, null);
							this.database.save(course);
							this.chat.success(sender, this.messages.parkourcategoryset.replace("%COURSENAME%", course.getName()).replace("%CAT%", "None"));
						} else {
							ParkourCategory cat = this.database.find(ParkourCategory.class, Integer.valueOf(args[2]));
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
				} catch(NumberFormatException e) {
					this.chat.error(sender, this.messages.invalidnumber);
				} catch(Exception e) {
					this.chat.internalError(sender);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setParkourDisplayName(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.parkour.setdisplayname")) {
			if(args.length > 2) {
				ParkourCourse course = ParkourCourse.retrieveFromName(this.database, args[1]);
				if(course != null) {
					course.setDisplayName(args[2]);
					this.database.save(course);
					this.chat.success(sender, this.messages.parkourdisplaynameset.replace("%COURSENAME%", course.getName()).replace("%DISPLAYNAME%", course.getDisplayName()));
				} else {
					this.chat.error(sender, this.messages.invalidcourse);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setParkourDescription(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.parkour.setdescription")) {
			if(args.length > 2) {
				ParkourCourse course = ParkourCourse.retrieveFromName(this.database, args[1]);
				if(course != null) {
					String description = Stream.of(args)
							.skip(2)
							.collect(Collectors.joining("\n"));
					course.setDescription(description);
					this.database.save(course);
					this.chat.success(sender, this.messages.parkourdisplaynameset.replace("%COURSENAME%", course.getName()).replace("%DISPLAYNAME%", course.getDisplayName()));
				} else {
					this.chat.error(sender, this.messages.invalidcourse);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setParkourMcVersion(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.parkour.setmcversion")) {
			if(args.length > 1) {
				ParkourCourse course = ParkourCourse.retrieveFromName(this.database, args[1]);
				if(course != null) {
					try {
						if(args.length > 2) {
							if(args[2].equals("null")) {
								course.setMinimumProtocolVersion(null);
							} else {
								int version = Integer.parseInt(args[2]);
								course.setMinimumProtocolVersion(version);
							}
						} else {
							@SuppressWarnings("unchecked") ViaAPI<Player> api = (ViaAPI<Player>) Via.getAPI();
							if(api != null) {
								if(sender instanceof Player) {
									course.setMinimumProtocolVersion(api.getPlayerVersion((Player) sender));
								} else {
									this.chat.error(sender, this.messages.notaplayer);
								}
							}
						}
						this.database.save(course);
						this.chat.success(sender, this.messages.parkourminimumprotocolversionset.replace("%COURSENAME%", course.getName()).replace("%PROTOCOLVERSION%", String.valueOf(course.getMinimumProtocolVersion())));
					} catch(NumberFormatException e) {
						this.chat.error(sender, this.messages.invalidnumber);
					}
				} else {
					this.chat.error(sender, this.messages.invalidcourse);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean setPlayerLevel(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.player.setlevel")) {
			if(args.length > 2) {
				Player player = Bukkit.getPlayer(args[1]);
				if(player != null) {
					try {
						this.parkourLevel.setLevel(player, Integer.parseInt(args[2]));
						this.chat.success(sender, this.messages.parkourlevelset.replace("%PLAYER%", player.getName()).replace("%LEVEL%", args[2]));
					} catch(NumberFormatException e) {
						this.chat.error(sender, this.messages.invalidnumber);
					} catch(Exception e) {
						this.chat.internalError(sender);
					}
				} else {
					this.chat.error(sender, this.messages.invalidplayer);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean addParkoins(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.parkoins.add")) {
			if(args.length > 2) {
				Player player = Bukkit.getPlayer(args[1]);
				if(player != null) {
					try {
						this.parkoins.add(player, Integer.parseInt(args[2]));
						Log.info("Player " + sender.getName() + " added " + args[2] + " parkoins to player " + player.getName());
						this.chat.success(sender, this.messages.parkoinsadd.replace("%PLAYER%", player.getName()).replace("%PARKOINS%", args[2]));
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send Colasix " + sender.getName() + " a ajouté " + args[2] + " parkoins à " + player.getName());
					} catch(NumberFormatException e) {
						this.chat.error(sender, this.messages.invalidnumber);
					} catch(Exception e) {
						this.chat.internalError(sender);
					}
				} else {
					this.chat.error(sender, this.messages.invalidplayer);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}

	private boolean removeParkoins(CommandSender sender, String[] args) {
		if(sender.hasPermission("parkouraddon.parkoins.deduct")) {
			if(args.length > 2) {
				Player player = Bukkit.getPlayer(args[1]);
				if(player != null) {
					try {
						this.parkoins.remove(player, Integer.parseInt(args[2]));
						Log.info("Player " + sender.getName() + " deducted " + args[2] + " parkoins to player " + player.getName());
						this.chat.success(sender, this.messages.parkoinsremove.replace("%PLAYER%", player.getName()).replace("%PARKOINS%", args[2]));
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send Colasix " + sender.getName() + " a retiré " + args[2] + " parkoins à " + player.getName());
					} catch(NumberFormatException e) {
						this.chat.error(sender, this.messages.invalidnumber);
					} catch(Exception e) {
						this.chat.internalError(sender);
					}
				} else {
					this.chat.error(sender, this.messages.invalidplayer);
				}
			} else {
				this.chat.error(sender, this.messages.notenougharguments);
			}
			return true;
		}
		return false;
	}
}
