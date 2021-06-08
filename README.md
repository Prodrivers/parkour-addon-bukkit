# ParkourAddon

Integration and additional functionalities for Parkour plugin :
* Integration with ProdriversCommons section management
* Parkour categories with progression tree :
	* Each parkour is sorted in categories of increasing difficulty and/or specific themes
	* Each category has a minimum parkour level
	* Once a certain threshold of completed parkour in a category is attained, the player gains access to the following one by being upgraded to the next category's parkour level
	* Multiple categories can be unlocked at once if they have the same minimum parkour level
	* Categories unlocking integrates with Minecraft advancements if they exists
* Category UI showing all parkours of a category and the player's progression in it
* Shop UI to buy access to categories and exchange Parkoins with the server's economy through VaultAPI
* ViaVersion support to limit access of certain courses to certain versions
* BlueMap integration
* Additional information in database, such as parkoins, parkour level and course position

This plugin only supports servers with 1.16+ API.

## Overview

The following video explains the plugin's main functionalities (in French).
[![Overview video link](https://img.youtube.com/vi/XT3lxEB3zVw/0.jpg)](https://youtu.be/XT3lxEB3zVw)


## License

ProdriversCommons API and its reference implementation is distributed under the LGPL version 3 license. A copy of the license is provided in LICENSE.md.
