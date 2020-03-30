package fr.prodrivers.bukkit.parkouraddon.models;

import io.ebean.EbeanServer;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table( name = "parkourplayercompletion" )
public class ParkourPlayerCompletion {
	@ManyToOne( optional = false )
	@Column( length = 16, name="playeruuid", nullable = false )
	@NotNull
	@Getter
	@Setter
	byte[] playerUniqueId;

	@ManyToOne( optional = false )
	@JoinColumn( name = "courseId" )
	@NotNull
	@Getter
	@Setter
	ParkourCourse course;

	public static ParkourPlayerCompletion retrieve( EbeanServer server, byte[] playerUniqueId, ParkourCourse course ) {
		return server.find( ParkourPlayerCompletion.class ).where().eq( "playeruuid", playerUniqueId ).eq( "courseId", course.getCourseId() ).findOne();
	}
}
