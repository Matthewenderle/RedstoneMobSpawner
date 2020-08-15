# RedstoneMobSpawner
A simple Minecraft Spigot plugin to enable controlling a Mob Spawner with a Redstone Circuit.

# Intro
RedstoneMobspawner was created for the community per the request of @strinberg at this plugin request thread. I had a great time relearning the Bukkit API to get this plugin to work.

The purpose of this plugin allows a player to place a redstone block inside the mob spawner and toggle it with a redstone circuit. Players can place a lever/button directly on the block or activate it with a redstone circuit.

![GIF of plugin in action](https://i.imgur.com/CwJym8O.png)
![Main Menu](https://i.imgur.com/siNhI4g.png)

# How to Use
* Place or find a Mob spawner, verify a blog is placed under it.
* Right click on the spawner with a Redstone Block or /rsms create
* Profit


# Commands/Permissions
Command |	Description	| Permission
-------- | ----------- | -----------
/rsms mobs | Show the main help screen | rsms.mobs
/rsms list | Displays all Redstone Mobspawners | rsms.list
/rsms create | Changes a regular spawner into a redstone enabled one | rsms.create
/rsms info | Provides information on the Mob Spawner in view | rsms.info
/rsms remove | Removes the Redstone from the spawner in view | rsms.remove
/rsms remove <UUID> | Removes the Redstone from the spawner identified by the UUID | rsms.removeremote
/rsms setmob <MOB_NAME> | Forces the plugin to get new data from the config file | rsms.setmob


Players have the ability tp change the mob type using the /rsms setmob <mobtype> but they need the permissions for that mob. For example to change it to a Wandering Traveler they need rsms.setmob.wandering_traveler.

# Features
* Control Mob Spawners with Redstone
* Change Mob type
* Permissions for each type of mob. ex: rsms.setmob.pig
* Customizable cost system for Redstone Blocks

# Future Features
* Nothing planned

# Caveats
* None found

# Changelog
## Version 1.0 (Current)
* Built on Spigot-1.16.1
* Fixed falling Armor Stands if the block below the spawner was removed or not present
* Fixed the issue with accidentally placing armor or items on the stand and not being able to remove them.
* Fixed bug where Redstone Block is left on the spawner
* Added 1.16 Mobs
* Added ability to charge players a set amount of Redstone block(s), and also a refund when deactivated.
* Removed manually set permissions. Permissions per mob type now follow rsms.setmob.<MOBNAME> under Mobs in the config file.

## Version 0.0.1
* Built on CraftBukkit-1.12.2
* Release
