package fr.prodrivers.bukkit.parkouraddon.models;

import fr.prodrivers.bukkit.parkouraddon.Utils;
import io.ebean.EbeanServer;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import javax.persistence.*;

@Entity
@Table(name = "time_ranked")
public class ParkourTimeRanked {
	@ManyToOne(optional = false)
	@JoinColumn(name = "courseId")
	@NotNull
	@Getter
	@Setter
	ParkourCourse course;

	@Column(length = 180, nullable = false)
	@NotNull
	@Getter
	@Setter
	String player;

	@Column(length = 13, nullable = false)
	@NotNull
	@Getter
	@Setter
	int time;

	@Column(length = 5, nullable = false)
	@NotNull
	@Getter
	@Setter
	int deaths;

	@Column(length = 16, name = "playeruuid", nullable = false)
	@NotNull
	@Getter
	@Setter
	byte[] playerUniqueId;

	@Column(nullable = false)
	@NotNull
	@Getter
	@Setter
	int rank;

	public static ParkourTimeRanked retrieve(EbeanServer server, OfflinePlayer player, ParkourCourse course) {
		return server.find(ParkourTimeRanked.class).where().eq("playeruuid", Utils.getBytesFromUniqueId(player.getUniqueId())).eq("courseId", course.getCourseId()).findOne();
	}
}