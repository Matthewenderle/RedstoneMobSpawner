package io.enderle.rsms;


import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class RSMSCommandExecutor implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redstonems") &&
                sender instanceof Player) {
            Player p = (Player)sender;
            if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
                if (args.length > 1 && Main.plugin.asUUIDs.contains(args[1])) {
                    Main.plugin.removeRedstoneSpawnerUUID(args[1],p);
                } else Main.plugin.removeRedstoneSpawner(p.getTargetBlock(null, 3).getLocation(), p);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("create")) {
                Main.plugin.createRedstoneSpawner(p.getTargetBlock(null, 3).getLocation(), p);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
                Block b = p.getTargetBlock(null, 3);
                Main.plugin.spawnerInfo(b.getLocation(), p);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
                Main.plugin.listSpawners(p);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                Main.plugin.reloadConfig(p);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("purge")) {
                Main.plugin.purgeRedstoneSpawners(p.getLocation(), p);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("mobs")) {
                String mobs = "";
                for (Map.Entry<String, Boolean> mob : Main.plugin.allowedMobs.entrySet()) {
                    if (PermissionChecker.CheckPermission(p, mob.getKey())) {
                        //p.sendMessage(ChatColor.GREEN + mob.getKey());
                        mobs = mobs + ChatColor.GREEN + mob.getKey() + ChatColor.RESET +", ";
                    } else mobs = mobs + ChatColor.DARK_RED + mob.getKey() + ChatColor.RESET +", ";
                }
                p.sendMessage(mobs.substring(0,mobs.length()-2));
            } else if (args.length > 0 && args[0].equalsIgnoreCase("setmob")) {
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.DARK_RED + "[FAIL] " + ChatColor.GRAY + "You must specify a mob type.");

                    for (Map.Entry<String, String> key: Main.plugin.mobPermissions.entrySet()) {
                        p.sendMessage(ChatColor.GREEN + key.getKey());
                    }

                    for (Map.Entry<String, String> mob : Main.plugin.mobPermissions.entrySet()) {
                        if (PermissionChecker.CheckPermission(p, mob.getValue()))
                            p.sendMessage(ChatColor.GREEN + mob.getKey());
                    }
                } else if (Main.plugin.mobPermissions.containsKey(args[1].toUpperCase())) {
                    Main.plugin.changeSpawnerMob(p.getTargetBlock(null, 3).getLocation(), p, EntityType.valueOf(args[1].toUpperCase()));
                } else {
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.DARK_RED + "[FAIL] " + ChatColor.GRAY + "You must specify a mob name.");
                    for (Map.Entry<String, String> mob : Main.plugin.mobPermissions.entrySet()) {
                        if (PermissionChecker.CheckPermission(p, mob.getValue()))
                            p.sendMessage(ChatColor.GREEN + mob.getKey());
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GOLD + "[HELP] " + ChatColor.GRAY + "You must specify a subcommand.");
                if (PermissionChecker.CheckPermission(p, "rsms.list")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "list " + ChatColor.RESET + "Displays all Redstone Mobspawners in the config");
                }
                if (PermissionChecker.CheckPermission(p, "rsms.create")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "create " + ChatColor.RESET + "Converts regular spawner to a RS one");
                }
                if (PermissionChecker.CheckPermission(p, "rsms.info")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "info " + ChatColor.RESET + "Displays information about a RS spawner");
                }
                //p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "remove " + ChatColor.RESET + "Converts a RS spawner back to a regular one");
                if (PermissionChecker.CheckPermission(p, "rsms.remove")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "remove " + ChatColor.RED + "<UUID> " + ChatColor.RESET + "Converts a spawner back");
                }
                if (PermissionChecker.CheckPermission(p, "rsms.mobs")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "mobs " + ChatColor.RESET + "Lists mobs you can change it to");
                }
                if (PermissionChecker.CheckPermission(p, "rsms.setmob")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.GOLD + "setmob " + ChatColor.RED + "<MOB_NAME> " + ChatColor.RESET + "Changes the spawning mob");
                }
                if (PermissionChecker.CheckPermission(p, "rsms.purge")) {
                    p.sendMessage(ChatColor.GRAY + "/rsms " + ChatColor.RED + "purge " + ChatColor.RESET + "Purges ALL spawners in the server");
                }            }
        }
        return false;
    }

    public void spawnerInfo(Location loc, Player p) {
        Block b = loc.getBlock();
        if (Main.plugin.spawnerStates.size() == 0) {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, we could not find any Redstone enabled spawners.");
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Info for Redstone Enabled Spawner!");
            p.sendMessage(ChatColor.GOLD + "Block x,y,z: " + ChatColor.RESET + b
                    .getX() + ", " + b.getY() + ", " + b.getZ());
            if (!Main.plugin.spawnerStates.get(b.getLocation())) {
                p.sendMessage(ChatColor.GOLD + "is Spawning: " + ChatColor.RESET + Character.MIN_VALUE);
            } else {
                p.sendMessage(ChatColor.GOLD + "is Spawning: " + ChatColor.RESET + Main.plugin.spawnerStates
                        .get(b.getLocation()).toString());
            }
        }
        for (String s : Main.plugin.asLocations.keySet()) {
            if (Main.plugin.asLocations.get(s) == b.getLocation().add(0.5D, 0.0D, 0.5D))
                p.sendMessage(ChatColor.GOLD + "AS UUID: " + ChatColor.RESET + s);
        }
    }
}
