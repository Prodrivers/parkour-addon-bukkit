package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.Chat;
import org.bukkit.command.CommandSender;

public class EChat extends Chat {
	public EChat(String name) {
		super(name);
	}

	public void internalError(CommandSender receiver) {
		error(receiver, ParkourAddonPlugin.messages.errorocurred);
	}
}
