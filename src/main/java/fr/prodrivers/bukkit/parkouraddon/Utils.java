package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.plugin.EConfiguration;
import fr.prodrivers.bukkit.parkouraddon.plugin.EMessages;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.InputMode;
import org.geysermc.floodgate.util.UiProfile;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Utils {
	public static final String INIT_TABLES_SCRIPT = """
			alter table course add displayName varchar(180);
			alter table course add categoryId integer;
			alter table course add description varchar(255) null;
			alter table course add minimumProtocolVersion integer null;
			alter table course add positionX double null;
			alter table course add positionY double null;
			alter table course add positionZ double null;
			alter table course add positionWorld varchar(24) null;
			create table parkourcategory (
			  categoryId                                        integer auto_increment not null,
			  name                                              varchar(180) not null,
			  baseLevel                                         integer default 0 not null,
			  previousCategoryId                                integer,
			  parkoinsReward                                    integer default 0 not null,
			  requiredCoursesNumberInPreviousCategoryForRankup  integer default 0 not null,
			  material                                          varchar(180) not null,
			  materialData                                      integer default 0 not null,
			  chatColor                                         varchar(180) not null,
			  hexColor                                          integer default 0 not null,
			  price                                             integer default 0 not null,
			  hidden                                            tinyint(1) default 0 not null,
			  constraint pk_parkourcategory primary key (categoryId))
			;

			create table parkourplayercompletion (
			  playeruuid                varbinary(16) not null,
			  courseId                  integer not null)
			;

			alter table parkourcategory add constraint fk_parkourcategory_nextCategory_1 foreign key (nextCategoryId) references parkourcategory (categoryId) on delete restrict on update restrict;
			alter table parkourcategory add constraint fk_parkourcategory_prevCategory_1 foreign key (previousCategoryId) references parkourcategory (categoryId) on delete restrict on update restrict;
			create index ix_parkourcategory_nextCategory_1 on parkourcategory (nextCategoryId);
			create index ix_parkourcategory_previousCategory_1 on parkourcategory (previousCategoryId);
			alter table course add constraint fk_course_category_2 foreign key (categoryId) references parkourcategory (categoryId) on delete restrict on update restrict;
			create index ix_course_category_2 on course (categoryId);
			alter table parkourplayercompletion add constraint fk_parkourplayercompletion_course_3 foreign key (courseId) references course (courseId) on delete restrict on update restrict;
			alter table parkourplayercompletion add constraint pk_parkourplayercompletion primary key (playeruuid, courseId);
			alter table `course` add index `course_idx_categoryid_courseid` (`categoryId`,`courseId`);
			alter table `parkourcategory` add index `parkourcategory_idx_hidden` (`hidden`);
			alter table `time` add index `time_idx_courseid_time` (`courseId`,`time`);
			create view time_ranked as select `time`.`courseId`                      AS `courseId`,
			       `time`.`player`                                AS `player`,
			       `time`.`time`                                  AS `time`,
			       `time`.`deaths`                                AS `deaths`,
			       `time`.`playeruuid`                            AS `playeruuid`,
			       (select count(`b`.`timeId`)
			        from `time` `b`
			        where ((`time`.`time` >= `b`.`time`) and
			               (`time`.`courseId` = `b`.`courseId`))) AS `rank`
			from `time`
			order by `time`.`courseId`, `time`.`time`;
			create view time_sorted as select `time`.`courseId` AS `courseId`,
			       `time`.`player`   AS `player`,
			       `time`.`time`     AS `time`,
			       `time`.`deaths`   AS `deaths`
			from `time`
			order by `time`.`courseId`, `time`.`time`;

			""";

	public static final String GET_PARKOURS_WITH_COMPLETION_QUERY = """
			SELECT course.name AS name, course.displayName AS displayName, course.author AS author, course.description AS description, parkourcategory.chatColor AS chatColor, parkourcategory.material AS material, parkourplayercompletion.playeruuid AS playeruuid
			FROM `parkourcategory`
			         JOIN `course` ON `parkourcategory`.`categoryId` = `course`.`categoryId`
			         LEFT JOIN `parkourplayercompletion`
			                   on `course`.`courseId` = `parkourplayercompletion`.`courseId` AND `parkourplayercompletion`.`playeruuid` = ?
			WHERE `parkourcategory`.`categoryId` = ?
			ORDER BY `course`.`name`""";

	public static final String SET_PLAYER_PARKOINS_QUERY = "UPDATE `players` SET `parkoins` = ? WHERE `playeruuid` = ?";
	public static final String SET_PLAYER_PARKOUR_LEVEL_QUERY = "UPDATE `players` SET `parkourLevel` = ? WHERE `playeruuid` = ?";

	private static final Pattern ARGUMENT_PATTERN = Pattern.compile("\"[^\"]+\"|\\S+");

	public static byte[] getBytesFromUniqueId(UUID uniqueId) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uniqueId.getMostSignificantBits());
		bb.putLong(uniqueId.getLeastSignificantBits());
		return bb.array();
	}

	public static UUID getUniqueIdFromBytes(byte[] uniqueId) {
		return UUID.nameUUIDFromBytes(uniqueId);
	}

	public static int parseColor(String colorString) {
		if(colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong(colorString.substring(1), 16);
			if(colorString.length() != 7) {
				throw new IllegalArgumentException("Unknown color");
			}
			return (int) color;
		}
		throw new IllegalArgumentException("Unknown color");
	}

	public static ItemStack getCloseItem(EConfiguration configuration, EMessages messages) {
		ItemStack item = new ItemStack(configuration.shops_close_material, 1);
		ItemMeta meta = item.getItemMeta();
		if(meta != null) {
			meta.setDisplayName(messages.parkourshopui_close_title);
		}
		item.setItemMeta(meta);
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
		Color.RGBtoHSB((int) r, (int) g, (int) b, hsv);

		return hsv;
	}

	public static boolean isColorLight(int rgbColor) {
		return intToHsl(rgbColor)[2] > 200;
	}

	public static boolean hasBedrockSession(Player player) {
		try {
			// Check presence of FloodgateApi
			Class.forName("org.geysermc.floodgate.api.FloodgateApi");

			FloodgateApi api = FloodgateApi.getInstance();
			return api != null && api.isFloodgatePlayer(player.getUniqueId());
		} catch(ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isPlayerUsingTouchControls(Player player) {
		try {
			// Check presence of FloodgateApi
			Class.forName("org.geysermc.floodgate.api.FloodgateApi");

			FloodgateApi api = FloodgateApi.getInstance();
			FloodgatePlayer floodgatePlayer = api.getPlayer(player.getUniqueId());
			return floodgatePlayer != null && ((floodgatePlayer.getInputMode() == InputMode.TOUCH) || (floodgatePlayer.getUiProfile() == UiProfile.POCKET));
		} catch(ClassNotFoundException e) {
			return false;
		}
	}

	public static String[] parseArgumentsWithQuotes(String message) {
		return ARGUMENT_PATTERN.matcher(message)
				.results()
				.map(MatchResult::group)
				.toArray(String[]::new);
	}
}
