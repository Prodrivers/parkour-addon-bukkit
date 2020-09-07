package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.bedrockbridger.session.BedrockSession;
import fr.prodrivers.bukkit.commons.storage.SQLProvider;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import me.eddie.inventoryguiapi.gui.contents.UnlimitedGUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.AbstractGUIElement;
import me.eddie.inventoryguiapi.gui.elements.FormImage;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.view.BedrockGUIPresenter;
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
import java.util.stream.Stream;

class ParkourSelectionUI {
	private static Map<UUID, Map<Integer, InventoryGUI>> uis = new HashMap<>();

	static void reload() {
		uis.clear();
	}

	public static void reload(Player player) {
		uis.remove( player.getUniqueId() );
	}

	public static void reload(Player player, Integer categoryId) {
		Map<Integer, InventoryGUI> playerUis = uis.computeIfAbsent( player.getUniqueId(), k -> new HashMap<>() );
		playerUis.remove( categoryId );
	}

	static void open( Player player, ParkourCategory category ) {
		try {
			if( !category.isHidden() || category.isHidden() && player.hasPermission( "Parkour.Admin.Bypass" ) ) {
				if( ParkourLevel.getLevel( player ) >= category.getBaseLevel() || player.hasPermission( "Parkour.Admin.Bypass" ) ) {
					Map<Integer, InventoryGUI> playerUis = uis.computeIfAbsent( player.getUniqueId(), k -> new HashMap<>() );
					if( !playerUis.containsKey( category.getCategoryId() ) ) {
						playerUis.put( category.getCategoryId(), generate( player, category ) );
					}
					playerUis.get( category.getCategoryId() ).open( player );
				} else {
					ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.notenoughlevel );
				}
			} else {
				ParkourAddonPlugin.chat.error( player, ParkourAddonPlugin.messages.invalidcategory );
			}
		} catch( NullPointerException e ) {
			ParkourAddonPlugin.chat.internalError( player );
			Log.severe( "Cannot show selection UI to player " + player.getName() + " .", e );
		}
	}

	private static InventoryGUI generate( Player player, ParkourCategory category ) throws NullPointerException {
		String title = ParkourAddonPlugin.messages.parkourselectionui_title_normal
				.replace( "%CAT%", category.getName() )
				.replace( "%CATCOLOR%", ChatColor.valueOf( category.getChatColor() ).toString() )
				+ ChatColor.RESET;

		if( BedrockSession.hasSession( player ) ) {
			return new GUIBuilder()
					.guiStateBehaviour( GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION )
					.inventoryType( InventoryType.CHEST )
					.dynamicallyResizeToWrapContent( true )
					.size( 54 )
					.presenter( new BedrockGUIPresenter() )
					.populator( new UnlimitedGUIPopulator() )
					.contents(
							title,
							genContent( true, player, category ),
							false,
							false,
							false
					)
					.build();
		}

		if( category.getName().length() > 8 ) {
			title = ParkourAddonPlugin.messages.parkourselectionui_title_reduced
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
						genContent( false, player, category ),
						true,
						true,
						true
				)
				.build();
	}

	private static List<GUIElement> genContent( boolean isBedrockContent, Player player, ParkourCategory category ) throws NullPointerException {
		List<String> lores = ParkourAddonPlugin.messages.parkourselectionui_item_lore_normal;
		List<String> lores_completed = ParkourAddonPlugin.messages.parkourselectionui_item_lore_completed;
		List<GUIElement> contents = new ArrayList<>();

		if( isBedrockContent ) {
			lores = ParkourAddonPlugin.messages.parkourselectionui_item_lore_bedrock;
		}

		PreparedStatement query;
		try {
			query = SQLProvider.getConnection().prepareStatement( Utils.GET_PARKOURS_WITH_COMPLETION_QUERY );
			query.setBytes( 1, Utils.getBytesFromUniqueId( player.getUniqueId() ) );
			query.setInt( 2, category.getCategoryId() );
			ResultSet results = query.executeQuery();

			while( results.next() ) {
				String internalName = results.getString( "course.name" );
				String author = results.getString( "course.author" );
				String description = results.getString( "course.description" );
				final String finalDescription = ( description == null ? "" : description );
				String name = ChatColor.valueOf( results.getString( "parkourcategory.chatColor" ) ) + results.getString( "course.displayName" );
				boolean completed = results.getBytes( "playeruuid" ) != null;
				Material material = Material.valueOf( results.getString( "parkourcategory.material" ) );
				GUIElement element;

				Stream<String> loreStream = completed ? Stream.concat( lores.stream(), lores_completed.stream() ) : lores.stream();

				String[] formattedLore = loreStream
						.skip(1)
						.map( lore -> lore
								.replace( "%NAME%", name)
								.replace( "%AUTHOR%", author)
								.replace( "%DESCRIPTION%", finalDescription )
								.split( "\n" )
						)
						.flatMap( Arrays::stream )
						.filter( lore -> !ChatColor.stripColor( lore ).isEmpty() )
						.toArray(String[]::new);
				element = createJoinParkourElement(
						completed,
						internalName,
						lores.get(0).replace( "%NAME%", name )
								.replace( "%AUTHOR%", author ),
						material,
						formattedLore
				);
				contents.add( element );
			}
		} catch( SQLException e ) {
			Log.severe( "Cannot get courses to show selection UI.", e );
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
				AbstractGUIElement.NO_DESIRED_SLOT,
				GUIElementFactory.formatItem(
						item,
						displayName,
						lore
				),
				(Callback<Player>) player -> Bukkit.getScheduler().runTaskLater( InventoryGUIAPI.getInstance(),
						() -> {
							player.closeInventory();
							Players.joinParkour( player, name );
						}, 1L ),
				completed ? ParkourAddonPlugin.configuration.selection_image_check : FormImage.NONE
		);
	}
}