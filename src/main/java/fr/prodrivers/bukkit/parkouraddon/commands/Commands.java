package fr.prodrivers.bukkit.parkouraddon.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandCompletions;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import io.ebean.Database;
import io.github.a5h73y.parkour.type.course.CourseInfo;
import org.bukkit.Material;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class Commands {
	@Inject
	public Commands(BukkitCommandManager commandManager, Database database, ParkourAddonCommand parkourAddonCommand) {
		// Register ParkourAddon command
		commandManager.registerCommand(parkourAddonCommand);

		// Register completions
		registerCompletions(commandManager, database);
	}

	public void registerCompletions(BukkitCommandManager commandManager, Database database) {
		CommandCompletions<BukkitCommandCompletionContext> commandCompletions = commandManager.getCommandCompletions();
		commandCompletions.registerAsyncCompletion("materials", c -> Arrays.stream(Material.values()).map(Enum::name).toList());
		commandCompletions.registerAsyncCompletion("boolean", c -> List.of("true", "false"));
		commandCompletions.registerAsyncCompletion("parkourName", c -> CourseInfo.getAllCourseNames());
		commandCompletions.registerAsyncCompletion("categoryName", c -> ParkourCategory
				.retrieveAllNames(database)
				.stream()
				.map(ParkourCategory::getName)
				.toList()
		);
	}
}
