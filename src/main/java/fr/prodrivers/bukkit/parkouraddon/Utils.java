package fr.prodrivers.bukkit.parkouraddon;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.UUID;

public class Utils {
	public static String INIT_TABLES_SCRIPT = "alter table course add displayName varchar(180);\n" +
			"alter table course add categoryId integer;\n" +
			"alter table course add description varchar(255) null;\n" +
			"alter table course add minimumProtocolVersion integer null;\n" +
			"alter table course add positionX double null;\n" +
			"alter table course add positionY double null;\n" +
			"alter table course add positionZ double null;\n" +
			"alter table course add positionWorld varchar(24) null;" +
			"\n" +
			"create table parkourcategory (\n" +
			"  categoryId                                        integer auto_increment not null,\n" +
			"  name                                              varchar(180) not null,\n" +
			"  baseLevel                                         integer default 0 not null,\n" +
			"  previousCategoryId                                integer,\n" +
			"  parkoinsReward                                    integer default 0 not null,\n" +
			"  requiredCoursesNumberInPreviousCategoryForRankup  integer default 0 not null,\n" +
			"  material                                          varchar(180) not null,\n" +
			"  materialData                                      integer default 0 not null,\n" +
			"  chatColor                                         varchar(180) not null,\n" +
			"  hexColor                                          integer default 0 not null,\n" +
			"  price                                             integer default 0 not null,\n" +
			"  hidden                                            tinyint(1) default 0 not null,\n" +
			"  constraint pk_parkourcategory primary key (categoryId))\n" +
			";\n" +
			"\n" +
			"create table parkourplayercompletion (\n" +
			"  playeruuid                varbinary(16) not null,\n" +
			"  courseId                  integer not null)\n" +
			";\n" +
			"\n" +
			"alter table parkourcategory add constraint fk_parkourcategory_nextCategory_1 foreign key (nextCategoryId) references parkourcategory (categoryId) on delete restrict on update restrict;\n" +
			"alter table parkourcategory add constraint fk_parkourcategory_prevCategory_1 foreign key (previousCategoryId) references parkourcategory (categoryId) on delete restrict on update restrict;\n" +
			"create index ix_parkourcategory_nextCategory_1 on parkourcategory (nextCategoryId);\n" +
			"create index ix_parkourcategory_previousCategory_1 on parkourcategory (previousCategoryId);\n" +
			"alter table course add constraint fk_course_category_2 foreign key (categoryId) references parkourcategory (categoryId) on delete restrict on update restrict;\n" +
			"create index ix_course_category_2 on course (categoryId);\n" +
			"alter table parkourplayercompletion add constraint fk_parkourplayercompletion_course_3 foreign key (courseId) references course (courseId) on delete restrict on update restrict;\n" +
			"alter table parkourplayercompletion add constraint pk_parkourplayercompletion primary key (playeruuid, courseId);\n" +
			"alter table `course` add index `course_idx_categoryid_courseid` (`categoryId`,`courseId`);\n" +
			"alter table `parkourcategory` add index `parkourcategory_idx_hidden` (`hidden`);\n" +
			"alter table `time` add index `time_idx_courseid_time` (`courseId`,`time`);\n" +
			"create view time_ranked as select `time`.`courseId`                      AS `courseId`,\n" +
			"       `time`.`player`                                AS `player`,\n" +
			"       `time`.`time`                                  AS `time`,\n" +
			"       `time`.`deaths`                                AS `deaths`,\n" +
			"       `time`.`playeruuid`                            AS `playeruuid`,\n" +
			"       (select count(`b`.`timeId`)\n" +
			"        from `time` `b`\n" +
			"        where ((`time`.`time` >= `b`.`time`) and\n" +
			"               (`time`.`courseId` = `b`.`courseId`))) AS `rank`\n" +
			"from `time`\n" +
			"order by `time`.`courseId`, `time`.`time`;\n" +
			"create view time_sorted as select `time`.`courseId` AS `courseId`,\n" +
			"       `time`.`player`   AS `player`,\n" +
			"       `time`.`time`     AS `time`,\n" +
			"       `time`.`deaths`   AS `deaths`\n" +
			"from `time`\n" +
			"order by `time`.`courseId`, `time`.`time`;\n" +
			"\n";

	public static String GET_PARKOURS_WITH_COMPLETION_QUERY = "SELECT *\n" +
			"FROM `parkourcategory`\n" +
			"         JOIN `course` ON `parkourcategory`.`categoryId` = `course`.`categoryId`\n" +
			"         LEFT JOIN `parkourplayercompletion`\n" +
			"                   on `course`.`courseId` = `parkourplayercompletion`.`courseId` AND `parkourplayercompletion`.`playeruuid` = ?\n" +
			"WHERE `parkourcategory`.`categoryId` = ?\n" +
			"ORDER BY `course`.`name`";

	public static String SET_PLAYER_PARKOINS_QUERY = "UPDATE `players` SET `parkoins` = ? WHERE `playeruuid` = ?";
	public static String SET_PLAYER_PARKOUR_LEVEL_QUERY = "UPDATE `players` SET `parkourLevel` = ? WHERE `playeruuid` = ?";

	public static byte[] getBytesFromUniqueId( UUID uniqueId ) {
		ByteBuffer bb = ByteBuffer.wrap( new byte[ 16 ] );
		bb.putLong( uniqueId.getMostSignificantBits() );
		bb.putLong( uniqueId.getLeastSignificantBits() );
		return bb.array();
	}

	public static UUID getUniqueIdFromBytes( byte[] uniqueId ) {
		return UUID.nameUUIDFromBytes( uniqueId );
	}

	public static int parseColor( String colorString ) {
		if( colorString.charAt( 0 ) == '#' ) {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong( colorString.substring( 1 ), 16 );
			if( colorString.length() != 7 ) {
				throw new IllegalArgumentException( "Unknown color" );
			}
			return (int) color;
		}
		throw new IllegalArgumentException( "Unknown color" );
	}

	public static ItemStack getCloseItem() {
		ItemStack item = new ItemStack( ParkourAddonPlugin.configuration.shops_close_material, 1 );
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName( ParkourAddonPlugin.messages.parkourshopui_close_title );
		item.setItemMeta( meta );
		return item;
	}

	public static float[] intToHsl(int rgbColor) {
		float r = 0xFF & (rgbColor >> 0x10);
		float g = 0xFF & (rgbColor >> 0x8);
		float b = 0xFF & rgbColor;

		r = r / 255.f;
		g = g / 255.f;
		b = b / 255.f;

		float[] hsv = new float[3];
		Color.RGBtoHSB((int)r, (int)g, (int)b, hsv);

		return hsv;
	}

	public static boolean isColorLight(int rgbColor) {
		return intToHsl(rgbColor)[2] > 200;
	}
}
