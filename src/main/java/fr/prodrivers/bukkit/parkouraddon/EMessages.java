package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.configuration.Messages;
import org.bukkit.plugin.Plugin;

public class EMessages extends Messages {
	public String configurationreloaded = "Configuration reloaded!";
	public String errorocurred = "An error occured while executing this command. Please report this to the server staff.";
	public String notenougharguments = "Not enough arguments.";
	public String notaplayer = "This command can only be executed by players.";
	public String categoryentryheader = "ID. NAME, lvl BASE, MATERIAL, CHAT COLOR/HEX COLOR -> NEXT CATEGORY.";
	public String categoryentry = "%ID%. %NAME%, lvl %BASELEVEL%, %MATERIAL%, %CHATCOLOR%/%HEXCOLOR% -> %NEXTID%.";
	public String nocategoryentries = "No categories.";
	public String categoryadded = "Category n°%CATID% added.";
	public String parkourcategoryset = "Set course %COURSENAME% category to %CAT%";
	public String parkourdisplaynameset = "Set course %COURSENAME% display name to %DISPLAYNAME%";
	public String nextcategoryset = "Category %CAT%'s next category set to %NEXTCAT%";
	public String prevcategoryset = "Category %CAT%'s next category set to %PREVCAT%";
	public String parkoinsrewardcategoryset = "Category %CAT%'s parkoins reward set to %REWARD%";
	public String invalidcategory = "Invalid category.";
	public String invalidcourse = "Invalid course.";
	public String invalidnumber = "Invalid number.";
	public String invalidplayer = "Invalid player.";
	public String invalidargument = "Invalid argument.";
	public String rankup = "Well done %PLAYER%! You rank up to level %LEVEL%!";
	public String notenoughlevel = "You do not have the required level to access thoses parkours !";
	public String parkourselectionuititle = "Parkour = %CATCOLOR%%CAT%";
	public String parkourselectionuititlereduced = "P = %CATCOLOR%%CAT%";
	public String parkourselectionuilore = "&rJoin %COURSE%";
	public String parkourselectionuilorecompleted = "&rCompleted !";
	public String parkourlevelset = "Set player's %PLAYER% parkour level to %LEVEL%";
	public String parkoinsadd = "Added %PARKOINS% parkoins to %PLAYER%";
	public String parkoinsremove = "Removed %PARKOINS% parkoins to %PLAYER%";
	public String parkourshopui_general_title = "Parkour = Shop";
	public String parkourshopui_general_rankname = "Ranks";
	public String parkourshopui_general_convertername = "Convert";
	public String parkourshopui_close_title = "Back";
	public String parkourshopui_ranks_title = "Parkour = Shop Ranks";
	public String parkourshopui_ranks_rankitemname = "Rank = %CATEGORYCOLOR%%CATEGORY%";
	public String parkourshopui_ranks_rankitemlore1 = "Buy Parkour Rank = %CATEGORYCOLOR%%CATEGORY%";
	public String parkourshopui_ranks_rankitemlore2 = "for %PRICE%, level %MINLEVEL%, target %TARGETLEVEL%";
	public String parkourshopui_ranks_boughtrankitemname = "Rank = %CATEGORYCOLOR%%CATEGORY%&r (BOUGHT)";
	public String parkourshopui_ranks_boughtrankitemlore1 = "You already bought";
	public String parkourshopui_ranks_boughtrankitemlore2 = "rank %CATEGORY%!";
	public String parkourshopui_ranks_notbuyablerankitemname = "Rank = %CATEGORYCOLOR%%CATEGORY%&r (NOT BUYABLE)";
	public String parkourshopui_ranks_notbuyablerankitemlore1 = "You can't buy rank %CATEGORY%!";
	public String parkourshopui_ranks_notbuyablerankitemlore2 = "Requires %PRICE% parkoins.";
	public String parkourshopui_ranks_notbuyablerankitemlore3 = "Requires level %MINLEVEL%.";
	public String parkourshopui_ranks_bought = "You bought rank %CATEGORY% !";
	public String parkourshopui_ranks_notenoughlevel = "You do not have the required level !";
	public String parkourshopui_ranks_notenoughbalance = "You do not have enough parkoins !";
	public String parkourshopui_ranks_alreadyhave = "You already have this rank !";
	public String parkourshopui_converters_title = "Parkour = Shop Convert";
	public String parkourshopui_converters_to_name = "%AMOUNT% parkoins";
	public String parkourshopui_converters_to_lore1 = "&rConvert %PRICE% coins";
	public String parkourshopui_converters_to_lore2 = "&rto %AMOUNT% parkoins.";
	public String parkourshopui_converters_to_bought = "You converted %PRICE% coins to %AMOUNT% parkoins!";
	public String parkourshopui_converters_to_notenoughbalance = "You do not have enough coins!";
	public String parkourshopui_converters_from_name = "%AMOUNT% coins";
	public String parkourshopui_converters_from_lore1 = "&rConvert %PRICE% parkcoins";
	public String parkourshopui_converters_from_lore2 = "&rto %AMOUNT% coins.";
	public String parkourshopui_converters_from_bought = "You converted %PRICE% parkoins to %AMOUNT% coins!";
	public String parkourshopui_converters_from_notenoughbalance = "You do not have enough parkcoins!";
	public String parkourshopui_converters_unavailable = "Currency conversion is not available.";
	public String cannotjoinnotpartyowner = "Only the party owner can enter a parkour.";
	public String party_notenoughlevel = "A party member does not have the required level to access this course.";

	public EMessages( Plugin plugin ) {
		super( plugin );
	}
}