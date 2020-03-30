package fr.prodrivers.bukkit.parkouraddon.models;

import io.ebean.EbeanServer;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table( name = "parkourcategory" )
public class ParkourCategory {
	@Id
	@Column( name = "categoryId" )
	@Getter
	@Setter
	int categoryId;

	@Column( length = 180, nullable = false )
	@NotNull
	@Getter
	@Setter
	String name;

	@Column( name = "baseLevel", columnDefinition = "integer default 0 not null" )
	@Getter
	@Setter
	int baseLevel;

	@OneToOne
	@JoinColumn( name = "previousCategoryId" )
	@Getter
	@Setter
	ParkourCategory previousCategory;

	@OneToOne
	@JoinColumn( name = "nextCategoryId" )
	@Getter
	@Setter
	ParkourCategory nextCategory;

	@Column( name="requiredCoursesNumberRankup", columnDefinition = "integer default 0 not null" )
	@Getter
	@Setter
	int requiredCoursesNumberRankup;

	@Column( name="parkoinsReward", columnDefinition = "integer default 0 not null" )
	@Getter
	@Setter
	int parkoinsReward;

	@Column( length = 180, nullable = false )
	@NotNull
	@Getter
	@Setter
	String material;

	@Column( name="chatColor", length = 180, nullable = false )
	@NotNull
	@Getter
	@Setter
	String chatColor;

	@Column( name="hexColor", columnDefinition = "integer default 0 not null" )
	@Getter
	@Setter
	int hexColor;

	@Column( columnDefinition = "integer default 0 not null" )
	@Getter
	@Setter
	int price;

	@OneToMany( mappedBy = "category", cascade = CascadeType.PERSIST )
	@Getter
	@Setter
	List<ParkourCourse> courses;

	public ParkourCategory forceGetPreviousCategory( EbeanServer server ) {
		return server.find( ParkourCategory.class, previousCategory.getCategoryId() );
	}

	public ParkourCategory forceGetNextCategory( EbeanServer server ) {
		return server.find( ParkourCategory.class, nextCategory.getCategoryId() );
	}

	public static List<ParkourCategory> retrieveAll( EbeanServer server ) {
		return server.find( ParkourCategory.class ).select( "*" ).findList();
	}
}
