package fr.prodrivers.bukkit.parkouraddon.plugin;

import fr.prodrivers.bukkit.commons.chat.Chat;
import fr.prodrivers.bukkit.commons.chat.MessageSender;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EChat extends Chat {
	private final EMessages messages;

	@Inject
	public EChat(EMessages messages, MessageSender messageSender) {
		super(messageSender);

		this.messages = messages;
	}

	public void internalError(CommandSender receiver) {
		error(receiver, this.messages.error_occurred);
	}
}
