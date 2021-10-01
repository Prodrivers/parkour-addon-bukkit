package fr.prodrivers.bukkit.parkouraddon.tasks;

import fr.prodrivers.bukkit.parkouraddon.Log;
import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import io.ebean.Database;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class TasksRunner implements Runnable {
	private final JavaPlugin plugin;
	private final List<Runnable> tasks;

	@Inject
	public TasksRunner(JavaPlugin plugin, EMessages messages, Database database, Course course) {
		this.plugin = plugin;
		this.tasks = Arrays.asList(
				new ParkourPositionUpdater(database, course),
				new BlueMapParkourMarker(messages, database, course)
		);
	}

	public void run() {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			for(Runnable task : tasks) {
				try {
					task.run();
					Log.info("Executed task " + task.getClass().getCanonicalName());
				} catch(Exception e) {
					Log.severe("Could not run task " + task.getClass().getCanonicalName(), e);
				}
			}
		});
	}
}
