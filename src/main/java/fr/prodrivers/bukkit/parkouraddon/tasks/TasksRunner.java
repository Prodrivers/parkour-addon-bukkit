package fr.prodrivers.bukkit.parkouraddon.tasks;

import fr.prodrivers.bukkit.parkouraddon.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class TasksRunner implements Runnable {
	private final JavaPlugin plugin;
	private final List<Runnable> tasks;

	public TasksRunner( JavaPlugin plugin ) {
		this.plugin = plugin;
		this.tasks = Arrays.asList(
				new ParkourPositionUpdater(),
				new BlueMapParkourMarker()
		);
	}

	public void run() {
		Bukkit.getScheduler().runTaskAsynchronously( plugin, () -> {
			for(Runnable task : tasks) {
				try {
					task.run();
					Log.info( "Executed task " + task.getClass().getCanonicalName() );
				} catch( Exception e ) {
					Log.severe( "Could not run task " + task.getClass().getCanonicalName(), e );
				}
			}
		});
	}
}
