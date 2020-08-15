package io.enderle.rsms;

import java.util.Map;
import java.util.Objects;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

class RMSListener implements Listener {
    public static Main plugin;

    public RMSListener(Main instance) {
        plugin = instance;
    }

    @EventHandler
    public void interactSpawner(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        // Test if Right click action is on a SPAWNER
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType().equals(Material.SPAWNER)) {
            Boolean canRegister = false;
            String plural = "";
            if (this.plugin.redstoneblockcost > 1) plural = "s";

            // Test if user has permission or is OP
            if (PermissionChecker.CheckPermission(p, "rsms.create")) {
                canRegister = true;
            }

            // Test if redstone block is required
            if (PermissionChecker.CheckPermission(p, "rsms.requireBlock")) {
                // If holding Redstone Block
                if (p.getInventory().getItemInMainHand().getType().equals(Material.REDSTONE_BLOCK)) {
                    canRegister = true;
                } else {
                    canRegister = false;
                    //p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you must use " +
                            //this.plugin.redstoneblockcost + " Redstone Block" + plural + " to power this spawner.");
                }
            } else canRegister = false;


            if (canRegister) {
                Location loc = e.getClickedBlock().getLocation();
                e.setCancelled(true);
                if (!plugin.isRedstoneSpawner(loc)) {
                    plugin.createRedstoneSpawner(loc, p);
                } else plugin.removeRedstoneSpawner(loc, p);
            }
        }
    }

    @EventHandler
    public void redstoneListener(final BlockRedstoneEvent e) {
        Bukkit.getScheduler().runTaskLater((Plugin)plugin, new Runnable() {
            public void run() {
                Block eBlock = e.getBlock();
                for (BlockFace b : BlockFace.values()) {
                    Block ms = eBlock.getRelative(b);
                    if (ms.getType().equals(Material.SPAWNER) &&
                            RMSListener.plugin.asLocations.containsValue(ms.getLocation()))
                        for (Map.Entry<String, Location> asLoc : RMSListener.plugin.asLocations.entrySet()) {
                            if (ms.getLocation().toVector().equals(((Location)asLoc.getValue()).toVector()))
                                RMSListener.plugin.toggleSpawner(ms.getLocation());
                        }
                }
            }
        },  20L);
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER &&
                plugin.isNearDisabledRsSpawner(e.getLocation()))
            e.setCancelled(true);
    }
}
