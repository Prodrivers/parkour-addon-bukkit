package fr.prodrivers.bukkit.parkouraddon.plugin;

import fr.prodrivers.bukkit.commons.Chat;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EChat extends Chat {
	private final EMessages messages;

	@Inject
	public EChat(Main plugin, EMessages messages) {
		super(plugin.getDescription().getName());

		this.messages = messages;
	}

	public void internalError(CommandSender receiver) {
		error(receiver, this.messages.errorocurred);
	}
}
