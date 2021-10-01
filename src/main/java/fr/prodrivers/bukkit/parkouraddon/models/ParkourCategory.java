package fr.prodrivers.bukkit.parkouraddon.models;

import fr.prodrivers.bukkit.parkouraddon.plugin.Main;
import fr.prodrivers.bukkit.parkouraddon.Utils;
import io.ebean.Database;
import io.ebean.SqlRow;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "parkourcategory")
public class ParkourCategory {
	@Id
	@Column(name = "categoryId")
	@Getter
	@Setter
	int categoryId;

	@Column(length = 180, nullable = false)
	@NotNull
	@Getter
	@Setter
	String name;

	@Column(name = "baseLevel", columnDefinition = "integer default 0 not null")
	@Getter
	@Setter
	int baseLevel;

	@ManyToOne
	@JoinColumn(name = "previousCategoryId")
	@Getter
	@Setter
	ParkourCategory previousCategory;

	@OneToMany(mappedBy = "previousCategory")
	@Getter
	List<ParkourCategory> nextCategories;

	@Column(name = "requiredCoursesNumberInPreviousCategoryForRankup", columnDefinition = "integer default 0 not null")
	@Getter
	@Setter
	int requiredCoursesNumberInPreviousCategoryForRankup;

	@Column(name = "parkoinsReward", columnDefinition = "integer default 0 not null")
	@Getter
	@Setter
	int parkoinsReward;

	@Column(length = 180, nullable = false)
	@NotNull
	@Getter
	@Setter
	String material;

	@Column(name = "chatColor", length = 180, nullable = false)
	@NotNull
	@Getter
	@Setter
	String chatColor;

	@Column(name = "hexColor", columnDefinition = "integer default 0 not null")
	@Getter
	@Setter
	int hexColor;

	@Column(columnDefinition = "integer default 0 not null")
	@Getter
	@Setter
	int price;

	@Column(columnDefinition = "tinyint(1) default 0 not null")
	@Getter
	@Setter
	boolean hidden;

	@OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
	@Getter
	@Setter
	List<ParkourCourse> courses;

	public int getNumberOfCompletedCourses(Database server, UUID playerUniqueId) {
		SqlRow row = server
				.sqlQuery("SELECT COUNT( courseId ) FROM `parkourplayercompletion` NATURAL JOIN `course` GROUP BY `playeruuid`, `categoryId` HAVING `playeruuid` = :playeruuid AND `categoryId` = :categoryid;")
				.setParameter("playeruuid", Utils.getBytesFromUniqueId(playerUniqueId))
				.setParameter("categoryid", getCategoryId())
				.findOne();
		return (row != null ? row.getInteger("count( courseid )") : 0);
	}

	public ParkourCategory forceGetPreviousCategory(Database server) {
		return server.find(ParkourCategory.class, previousCategory.getCategoryId());
	}

	public List<ParkourCategory> forceGetNextCategories(Database server) {
		return server.find(ParkourCategory.class).where().ieq("previousCategoryId", String.valueOf(this.getCategoryId())).findList();
	}

	public static List<ParkourCategory> retrieveAll(Database server) {
		return server.find(ParkourCategory.class).select("*").findList();
	}
}
