package fr.prodrivers.bukkit.parkouraddon.models;

import io.ebean.EbeanServer;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.security.Timestamp;
import java.util.List;

@Entity
@Table( name = "course" )
public class ParkourCourse {
	@Id
	@Column( name = "courseId" )
	@Getter
	@Setter
	int courseId;

	@Column( length = 180, nullable = false )
	@NotNull
	@Getter
	@Setter
	String name;

	@Column( length = 180, nullable = false )
	@NotNull
	@Getter
	@Setter
	String author;

	@CreatedTimestamp
	@Getter
	@Setter
	Timestamp created;

	@Column( length = 180, name = "displayName" )
	@Getter
	@Setter
	String displayName;

	@ManyToOne()
	@JoinColumn( name = "categoryId" )
	@Getter
	@Setter
	ParkourCategory category;

	@OneToMany( mappedBy = "course", cascade = CascadeType.PERSIST )
	@Getter
	@Setter
	List<ParkourPlayerCompletion> completions;

	public static ParkourCourse retrieveFromName( EbeanServer server, String courseName ) {
		return server.find( ParkourCourse.class ).where().ieq( "name", courseName ).findOne();
	}
}