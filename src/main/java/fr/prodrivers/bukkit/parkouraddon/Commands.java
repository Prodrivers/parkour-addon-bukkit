package fr.prodrivers.bukkit.parkouraddon;

import fr.prodrivers.bukkit.parkouraddon.adaptation.Parkoins;
import fr.prodrivers.bukkit.parkouraddon.adaptation.ParkourLevel;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCategory;
import fr.prodrivers.bukkit.parkouraddon.models.ParkourCourse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Commands implements CommandExecutor {
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if( label.equalsIgnoreCase( "paddon" ) ) {
			if( args.length > 0 ) {
				switch( args[ 0 ] ) {
					case "shop":
						return openShop( sender );
					case "open":
						return openSelection( sender, args );
					case "listcategories":
						return listCategories( sender );
					case "addcategory":
						return addCategory( sender, args );
					case "setnextcategory":
						return setNextCategory( sender, args );
					case "setpreviouscategory":
						return setPreviousCategory( sender, args );
					case "setparkoinsreward":
						return setParkoinsReward( sender, args );
					case "sethidden":
						return setHidden( sender, args );
					case "setparkourcategory":
						return setParkourCategory( sender, args );
					case "setparkourdisplayname":
						return setParkourDisplayName( sender, args );
					case "setparkourdescription":
						return setParkourDescription( sender, args );
					case "setlevel":
						return setLevel( sender, args );
					case "addparkoins":
						return addParkoins( sender, args );
					case "removeparkoins":
						return removeParkoins( sender, args );
					case "reload":
						return reload( sender );
					case "help":
						return help( sender );
				}
			} else {
				if( sender.hasPermission( "parkouraddon.help" ) ) {
					ParkourAddonPlugin.chat.error( sender, "Unknown command, use /paddon help" );
					return true;
				}
			}
		}

		return false;
	}

	private boolean help( CommandSender sender ) {
		if( sender.hasPermission( "parkouraddon.help" ) ) {
			ParkourAddonPlugin.chat.send( sender, "=== USAGE ===" );
			ParkourAddonPlugin.chat.send( sender, "--- General ---" );
			ParkourAddonPlugin.chat.send( sender, "/paddon shop" );
			ParkourAddonPlugin.chat.send( sender, "/paddon reload" );
			ParkourAddonPlugin.chat.send( sender, "--- Categories ---" );
			ParkourAddonPlugin.chat.send( sender, "/paddon listcategories" );
			ParkourAddonPlugin.chat.send( sender, "/paddon addcategory <name> <baseLevel> <material> <materialDataValue> <chatColor> <hexColor>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setnextcategory <categoryId> <nextCategoryId>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setpreviouscategory <categoryId> <previousCategoryId>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setparkoinsreward <categoryId> <parkoinsReward>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon sethidden <categoryId> <isHidden>" );
			ParkourAddonPlugin.chat.send( sender, "--- Parkours ---" );
			ParkourAddonPlugin.chat.send( sender, "/paddon open <categoryId>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setparkourcategory <courseName> <categoryId>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setparkourdisplayname <courseName> <displayName>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setparkourdescription <courseName> <line 1> [line 2] ..." );
			ParkourAddonPlugin.chat.send( sender, "--- Levels ---" );
			ParkourAddonPlugin.chat.send( sender, "/paddon setlevel <playerName> <level>" );
			ParkourAddonPlugin.chat.send( sender, "--- Parkoins ---" );
			ParkourAddonPlugin.chat.send( sender, "/paddon addparkoins <playerName> <parkoins>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon removeparkoins <playerName> <parkoins>" );
		} else {
			ParkourAddonPlugin.chat.send( sender, "Usage:" );
			ParkourAddonPlugin.chat.send( sender, "/paddon open <category>" );
			ParkourAddonPlugin.chat.send( sender, "/paddon shop" );
		}
		return true;
	}

	private boolean reload( CommandSender sender ) {
		if( sender.hasPermission( "parkouraddon.reload" ) ) {
			ParkourAddonPlugin.configuration.reload();
			Categories.reload();
			ParkourSelectionUI.reload();
			ParkourShopUI.getInstance().reload();
			ParkourShopRankUI.getInstance().reload();
			ParkourShopConverterUI.getInstance().reload();
			ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.configurationreloaded );
			return true;
		}
		return false;
	}

	private boolean openShop( CommandSender sender ) {
		if( sender instanceof Player ) {
			try {
				ParkourShopUI.getInstance().open( (Player) sender );
			} catch( Exception e ) {
				ParkourAddonPlugin.chat.internalError( sender );
				Log.severe( "Error while trying to open Parkour Shop UI.", e );
			}
		} else {
			ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notaplayer );
		}
		return true;
	}

	private boolean openSelection( CommandSender sender, String[] args ) {
		if( sender instanceof Player ) {
			if( args.length > 1 ) {
				try {
					ParkourCategory cat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 1 ] ) );
					if( cat != null ) {
						ParkourSelectionUI.open( (Player) sender, cat );
					} else {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
					}
				} catch( NumberFormatException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
				} catch( IllegalArgumentException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidargument );
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
					Log.severe( "Error while trying to open Parkour Selection UI.", e );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
		} else {
			ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notaplayer );
		}
		return true;
	}

	private boolean listCategories( CommandSender sender ) {
		if( sender.hasPermission( "parkouraddon.category.list" ) ) {
			List<ParkourCategory> categories = ParkourAddonPlugin.database.find( ParkourCategory.class ).select( "*" ).findList();

			if( categories.size() > 0 ) {
				ParkourAddonPlugin.chat.send( sender, ParkourAddonPlugin.messages.categoryentryheader );
				for( ParkourCategory cat : categories ) {
					ParkourAddonPlugin.chat.send( sender,
							ParkourAddonPlugin.messages.categoryentry
									.replace( "%ID%", String.valueOf( cat.getCategoryId() ) )
									.replace( "%NAME%", cat.getName() )
									.replace( "%BASELEVEL%", String.valueOf( cat.getBaseLevel() ) )
									.replace( "%PREVID%", String.valueOf( ( cat.getPreviousCategory() != null ? cat.getPreviousCategory().getCategoryId() : "None" ) ) )
									.replace( "%NEXTID%", String.valueOf( ( cat.getNextCategory() != null ? cat.getNextCategory().getCategoryId() : "None" ) ) )
									.replace( "%HEXCOLOR%", "#" + Integer.toHexString( cat.getHexColor() ) )
									.replace( "%CHATCOLOR%", cat.getChatColor() )
									.replace( "%MATERIAL%", cat.getMaterial() )
					);
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.nocategoryentries );
			}
			return true;
		}
		return false;
	}

	private boolean addCategory( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.category.add" ) ) {
			if( args.length > 6 ) {
				try {
					ParkourCategory cat = new ParkourCategory();
					cat.setName( args[ 1 ] );
					cat.setBaseLevel( Integer.valueOf( args[ 2 ] ) );
					cat.setMaterial( Material.valueOf( args[ 3 ] ).toString() );
					cat.setChatColor( ChatColor.valueOf( args[ 5 ] ).toString() );
					cat.setHexColor( Utils.parseColor( args[ 6 ] ) );
					ParkourAddonPlugin.database.save( cat );
					ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.categoryadded.replace( "%CATID%", String.valueOf( cat.getCategoryId() ) ) );
				} catch( NumberFormatException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
				} catch( IllegalArgumentException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidargument );
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setNextCategory( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.category.setnext" ) ) {
			if( args.length > 2 ) {
				try {
					ParkourCategory cat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 1 ] ) );
					if( cat != null ) {
						if( args[ 2 ].equalsIgnoreCase( "null" ) ) {
							cat.setNextCategory( null );
							ParkourAddonPlugin.database.save( cat );
							ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.nextcategoryset.replace( "%CAT%", cat.getName() ).replace( "%NEXTCAT%", "None" ) );
						} else {
							ParkourCategory nextcat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 2 ] ) );
							if( nextcat != null ) {
								cat.setNextCategory( nextcat );
								ParkourAddonPlugin.database.save( cat );
								ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.nextcategoryset.replace( "%CAT%", cat.getName() ).replace( "%NEXTCAT%", nextcat.getName() ) );
							} else {
								ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
							}
						}
					} else {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
					}
				} catch( NumberFormatException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setPreviousCategory( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.category.setprevious" ) ) {
			if( args.length > 2 ) {
				try {
					ParkourCategory cat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 1 ] ) );
					if( cat != null ) {
						if( args[ 2 ].equalsIgnoreCase( "null" ) ) {
							cat.setPreviousCategory( null );
							ParkourAddonPlugin.database.save( cat );
							ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.prevcategoryset.replace( "%CAT%", cat.getName() ).replace( "%PREVCAT%", "None" ) );
						} else {
							ParkourCategory prevcat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 2 ] ) );
							if( prevcat != null ) {
								cat.setPreviousCategory( prevcat );
								ParkourAddonPlugin.database.save( cat );
								ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.prevcategoryset.replace( "%CAT%", cat.getName() ).replace( "%PREVCAT%", prevcat.getName() ) );
							} else {
								ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
							}
						}
					} else {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
					}
				} catch( NumberFormatException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setParkoinsReward( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.category.setparkoinsreward" ) ) {
			if( args.length > 2 ) {
				try {
					ParkourCategory cat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 1 ] ) );
					if( cat != null ) {
						cat.setParkoinsReward( Integer.valueOf( args[ 2 ] ) );
						ParkourAddonPlugin.database.save( cat );
						ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkoinsrewardcategoryset.replace( "%CAT%", cat.getName() ).replace( "%REWARD%", args[ 2 ] ) );
					} else {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
					}
				} catch( NumberFormatException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setHidden( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.category.sethidden" ) ) {
			if( args.length > 2 ) {
				try {
					ParkourCategory cat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 1 ] ) );
					if( cat != null ) {
						cat.setHidden( Boolean.parseBoolean( args[ 2 ] ) );
						ParkourAddonPlugin.database.save( cat );
						ParkourAddonPlugin.chat.success( sender, ( cat.isHidden() ? ParkourAddonPlugin.messages.categorysettohidden : ParkourAddonPlugin.messages.categorysettoshown ).replace( "%CAT%", cat.getName() ) );
					} else {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
					}
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
					Log.severe( "Cannot set category hidden state.", e );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setParkourCategory( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.parkour.setcategory" ) ) {
			if( args.length > 2 ) {
				try {
					ParkourCourse course = ParkourCourse.retrieveFromName( ParkourAddonPlugin.database, args[ 1 ] );
					if( course != null ) {
						if( args[ 2 ].equalsIgnoreCase( "null" ) ) {
							Courses.setCategory( course, null );
							ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkourcategoryset.replace( "%COURSENAME%", course.getName() ).replace( "%CAT%", "None" ) );
						} else {
							ParkourCategory cat = ParkourAddonPlugin.database.find( ParkourCategory.class, Integer.valueOf( args[ 2 ] ) );
							if( cat != null ) {
								Courses.setCategory( course, cat );
								ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkourcategoryset.replace( "%COURSENAME%", course.getName() ).replace( "%CAT%", cat.getName() ) );
							} else {
								ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcategory );
							}
						}
					} else {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcourse );
					}
				} catch( NumberFormatException e ) {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
				} catch( Exception e ) {
					ParkourAddonPlugin.chat.internalError( sender );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setParkourDisplayName( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.parkour.setdisplayname" ) ) {
			if( args.length > 2 ) {
				ParkourCourse course = ParkourCourse.retrieveFromName( ParkourAddonPlugin.database, args[ 1 ] );
				if( course != null ) {
					course.setDisplayName( args[ 2 ] );
					ParkourAddonPlugin.database.save( course );
					ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkourdisplaynameset.replace( "%COURSENAME%", course.getName() ).replace( "%DISPLAYNAME%", course.getDisplayName() ) );
				} else {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcourse );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setParkourDescription( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.parkour.setdescription" ) ) {
			if( args.length > 2 ) {
				ParkourCourse course = ParkourCourse.retrieveFromName( ParkourAddonPlugin.database, args[ 1 ] );
				if( course != null ) {
					String description = Stream.of( args )
							.skip( 2 )
							.collect( Collectors.joining( "\n" ) );
					course.setDescription( description );
					ParkourAddonPlugin.database.save( course );
					ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkourdisplaynameset.replace( "%COURSENAME%", course.getName() ).replace( "%DISPLAYNAME%", course.getDisplayName() ) );
				} else {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidcourse );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean setLevel( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.setlevel" ) ) {
			if( args.length > 2 ) {
				Player player = Bukkit.getPlayer( args[ 1 ] );
				if( player != null ) {
					try {
						ParkourLevel.setLevel( player, Integer.valueOf( args[ 2 ] ) );
						ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkourlevelset.replace( "%PLAYER%", player.getName() ).replace( "%LEVEL%", args[ 2 ] ) );
					} catch( NumberFormatException e ) {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
					} catch( Exception e ) {
						ParkourAddonPlugin.chat.internalError( sender );
					}
				} else {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidplayer );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean addParkoins( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.parkoins.add" ) ) {
			if( args.length > 2 ) {
				Player player = Bukkit.getPlayer( args[ 1 ] );
				if( player != null ) {
					try {
						Parkoins.add( player, Integer.valueOf( args[ 2 ] ) );
						Log.info( "Player " + sender.getName() + " added " + args[ 2 ] + " parkoins to player " + player.getName() );
						ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkoinsadd.replace( "%PLAYER%", player.getName() ).replace( "%PARKOINS%", args[ 2 ] ) );
						Bukkit.dispatchCommand( Bukkit.getConsoleSender(), "mail send Colasix " + sender.getName() + " a ajouté " + args[ 2 ] + " parkoins à " + player.getName() );
					} catch( NumberFormatException e ) {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
					} catch( Exception e ) {
						ParkourAddonPlugin.chat.internalError( sender );
					}
				} else {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidplayer );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}

	private boolean removeParkoins( CommandSender sender, String[] args ) {
		if( sender.hasPermission( "parkouraddon.parkoins.deduct" ) ) {
			if( args.length > 2 ) {
				Player player = Bukkit.getPlayer( args[ 1 ] );
				if( player != null ) {
					try {
						Parkoins.remove( player, Integer.valueOf( args[ 2 ] ) );
						Log.info( "Player " + sender.getName() + " deducted " + args[ 2 ] + " parkoins to player " + player.getName() );
						ParkourAddonPlugin.chat.success( sender, ParkourAddonPlugin.messages.parkoinsremove.replace( "%PLAYER%", player.getName() ).replace( "%PARKOINS%", args[ 2 ] ) );
						Bukkit.dispatchCommand( Bukkit.getConsoleSender(), "mail send Colasix " + sender.getName() + " a retiré " + args[ 2 ] + " parkoins à " + player.getName() );
					} catch( NumberFormatException e ) {
						ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidnumber );
					} catch( Exception e ) {
						ParkourAddonPlugin.chat.internalError( sender );
					}
				} else {
					ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.invalidplayer );
				}
			} else {
				ParkourAddonPlugin.chat.error( sender, ParkourAddonPlugin.messages.notenougharguments );
			}
			return true;
		}
		return false;
	}
}
