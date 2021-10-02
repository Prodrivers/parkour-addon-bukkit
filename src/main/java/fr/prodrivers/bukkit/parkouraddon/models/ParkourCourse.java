package fr.prodrivers.bukkit.parkouraddon.models;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Course;
import io.ebean.Database;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.security.Timestamp;
import java.util.List;

@Entity
@Table(name = "course")
public class ParkourCourse {
	@Id
	@Column(name = "courseId")
	@Getter
	@Setter
	int courseId;

	@Column(length = 180, nullable = false)
	@NotNull
	@Getter
	@Setter
	String name;

	@Column()
	@Getter
	@Setter
	String description;

	@Column(length = 180, nullable = false)
	@NotNull
	@Getter
	@Setter
	String author;

	@WhenCreated
	@Getter
	@Setter
	Timestamp created;

	@Column(length = 180, name = "displayName")
	@Getter
	@Setter
	String displayName;

	@Column(name = "minimumProtocolVersion")
	@Getter
	@Setter
	Integer minimumProtocolVersion;

	@Column(name = "positionX")
	@Getter
	@Setter
	Double positionX;

	@Column(name = "positionY")
	@Getter
	@Setter
	Double positionY;

	@Column(name = "positionZ")
	@Getter
	@Setter
	Double positionZ;

	@Column(length = 24, name = "positionWorld")
	@Getter
	@Setter
	String positionWorld;

	@ManyToOne()
	@JoinColumn(name = "categoryId")
	@Getter
	ParkourCategory category;

	@OneToMany(mappedBy = "course", cascade = CascadeType.PERSIST)
	@Getter
	@Setter
	List<ParkourPlayerCompletion> completions;

	public void setCategory(Course course, ParkourCategory category) {
		if(category == null) {
			course.setMinimumLevel(getName(), -1);
		} else {
			course.setMinimumLevel(getName(), category.getBaseLevel());
		}
		this.category = category;
	}

	public static ParkourCourse retrieveFromName(Database server, String courseName) {
		return server.find(ParkourCourse.class).where().ieq("name", courseName).findOne();
	}

	public static List<ParkourCourse> retrieveAll(Database server) {
		return server.find(ParkourCourse.class).select("*").findList();
	}
}