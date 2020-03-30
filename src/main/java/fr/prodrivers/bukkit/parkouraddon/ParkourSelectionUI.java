package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.commons.exceptions.NotPartyOwnerException;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.commons.storage.SQLProvider;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

class ParkourSelectionUI {
	private static Map<UUID, Map<Integer, InventoryGUI>> uis = new HashMap<>();

	static void reload() {
		uis.clear();
	}

	public static void reload(Player player, Integer categoryId) {
		Map<Integer, InventoryGUI> playerUis = uis.computeIfAbsent( player.getUniqueId(), k -> new HashMap<>() );
		playerUis.remove( categoryId );
	}

	static void open( Player player, ParkourCategory category ) {
		try {
			if( ParkourLevel.getLevel( player ) >= category.getBaseLevel() || player.hasPermission( "Parkour.Admin.Bypass" ) ) {
				Map<Integer, InventoryGUI> playerUis = uis.computeIfAbsent( player.getUniqueId(), k -> new HashMap<>() );
				if( !playerUis.containsKey( category.getCategoryId() ) ) {
					playerUis.put( category.getCategoryId(), generate( player, category ) );
				}
				playerUis.get( category.getCategoryId() ).open( player );
			} else {
				ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.notenoughlevel );
			}
		} catch( NullPointerException e ) {
			ParkourAddonPlugin.chat.internalError( player );
			ParkourAddonPlugin.logger.severe( "Error while trying to show parkour ui: " + e.getLocalizedMessage() );
			e.printStackTrace();
		}
	}

	private static InventoryGUI generate( Player player, ParkourCategory category ) throws NullPointerException {
		String title = ParkourAddonPlugin.messages.parkourselectionuititle
				.replace( "%CAT%", category.getName() )
				.replace( "%CATCOLOR%", ChatColor.valueOf( category.getChatColor() ).toString() )
				+ ChatColor.RESET;
		if( category.getName().length() > 8 ) {
			title = ParkourAddonPlugin.messages.parkourselectionuititlereduced
					.replace( "%CAT%", category.getName() )
					.replace( "%CATCOLOR%", ChatColor.valueOf( category.getChatColor() ).toString() )
					+ ChatColor.RESET;
		}

		return new GUIBuilder()
				.guiStateBehaviour( GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION )
				.inventoryType( InventoryType.CHEST )
				.dynamicallyResizeToWrapContent( true )
				.size( 54 )
				.contents(
						title,
						genContent( player, category ),
						true,
						true,
						true
				)
				.build();
	}

	private static List<GUIElement> genContent( Player player, ParkourCategory category ) throws NullPointerException {
		String lore = ParkourAddonPlugin.messages.parkourselectionuilore;
		String lore_completed = ParkourAddonPlugin.messages.parkourselectionuilorecompleted;
		List<GUIElement> contents = new ArrayList<>();

		/*ParkourCategory completeCoursesCategory = ParkourAddonPlugin.database.find( ParkourCategory.class )
				.select( "categoryId" )
				.fetch( "courses" )
				.orderBy( "courses.name" )
				.where()
				.eq( "categoryId", category.getCategoryId() )
				.findOne();

		try {
			for( ParkourCourse course : completeCoursesCategory.getCourses() ) {
				String name = ChatColor.valueOf( category.getChatColor() ) + course.getDisplayName();
				contents.add( createJoinParkourElement(
						course.getName(),
						name,
						Material.valueOf( category.getMaterial() ),
						category.getMaterialData(),
						lore.replace( "%COURSE%", name )
				) );
			}
		} catch( NullPointerException ex ) {
			ParkourAddonPlugin.logger.severe( "[ParkourAddon] Error while getting courses: " + ex.getLocalizedMessage() );
			ex.printStackTrace();
		}
		return contents;*/

		PreparedStatement query = null;
		try {
			query = SQLProvider.getConnection().prepareStatement( Utils.GET_PARKOURS_WITH_COMPLETION_QUERY );
			query.setBytes( 1, Utils.getBytesFromUniqueId( player.getUniqueId() ) );
			query.setInt( 2, category.getCategoryId() );
			ResultSet results = query.executeQuery();

			while( results.next() ) {
				String name = ChatColor.valueOf( results.getString( "chatColor" ) ) + results.getString( "displayName" );
				boolean completed = results.getBytes( "playeruuid" ) != null;
				GUIElement element;
				if(completed) {
					element = createJoinParkourElement(
							completed,
							results.getString( "course.name" ),
							name,
							Material.valueOf( results.getString( "material" ) ),
							lore.replace( "%COURSE%", name ),
							lore_completed
					);
				} else {
					element = createJoinParkourElement(
							completed,
							results.getString( "course.name" ),
							name,
							Material.valueOf( results.getString( "material" ) ),
							lore.replace( "%COURSE%", name )
					);
				}
				contents.add( element );
			}
		} catch( SQLException ex ) {
			ParkourAddonPlugin.logger.severe( "[ParkourAddon] Error while getting courses: " + ex.getLocalizedMessage() );
			ex.printStackTrace();
		}

		return contents;
	}

	private static GUIElement createJoinParkourElement( boolean completed, final String name, String displayName, Material material, String... lore ) {
		ItemStack item = new ItemStack( material, 1 );
		if( completed ) {
			ItemMeta meta = item.getItemMeta();
			meta.addEnchant( Enchantment.DAMAGE_ALL, 1, true );
			meta.addItemFlags( ItemFlag.HIDE_ENCHANTS );
			item.setItemMeta( meta );
		}

		return GUIElementFactory.createActionItem(
				GUIElementFactory.formatItem(
						item,
						displayName,
						lore
				),
				(Callback<Player>) player -> Bukkit.getScheduler().runTaskLater( InventoryGUIAPI.getInstance(),
						() -> {
							player.closeInventory();
							Players.joinParkour( player, name );
						}, 1L )
		);
	}
}