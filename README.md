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
/rsms list | Displays all Redstone Mobspawners. | rsms.list
/rsms create | Changes a regular spawner into a redstone enabled one. | rsms.create
/rsms info | Provides information on the Mob Spawner in view. | rsms.info
/rsms remove | Forces to player to teleport to the region's warp point. | rsms.remove
/rsms setmob <MOB_NAME> | Forces the plugin to get new data from the config file. | rsms.setmob
 
# Features
* Control Mob Spawners with Redstone
* Change Mob type
* Permissions for each type of mob. ex: rsms.setmob.pig

# Future Features
* Nothing planned

# Caveats
* None found

# Changelog
* Version 0.0.1 (Current)
* Built on CraftBukkit-1.12.2
* Release
