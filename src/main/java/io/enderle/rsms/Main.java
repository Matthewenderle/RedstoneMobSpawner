package io.enderle.rsms;

import java.util.*;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main plugin = null;

    Logger logger = this.getLogger();

    public boolean debugging = true;

    public static Configuration config;

    public boolean useTags;

    public boolean usePermissions = true;

    public Integer redstoneblockcost;
    public Integer redstoneblockrefund;
    public Set<String> asUUIDs;
    public HashMap<String, Location> asLocations = null;
    public HashMap<Location, Boolean> spawnerStates = null;
    public HashMap<Location, Boolean> spawnerMobs = null;
    public HashMap<String, Boolean> allowedMobs = null;
    public HashMap<String, String> mobPermissions = null;

    public static void registerEvents(Plugin plugin, Listener... listeners) {
        Listener[] arrayOfListener;
        int j = (arrayOfListener = listeners).length;
        for (int i = 0; i < j; i++) {
            Listener listener = arrayOfListener[i];
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public void onEnable() {

        new UpdateChecker(this, 82789).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                logger.info("There is not a new update available.");
            } else {
                logger.info("There is a new update available.");
            }
        });

        getConfig().options().copyDefaults(true);
        saveConfig();
        plugin = this;

        this.useTags = getConfig().getBoolean("Config.use_tags");
        this.redstoneblockcost = getConfig().getInt("Config.redstoneblockcost");
        this.redstoneblockrefund = getConfig().getInt("Config.redstoneblockrefund");
        this.usePermissions = getConfig().getBoolean("Config.use_permissions");
        Bukkit.getServer().getPluginCommand("redstonems").setExecutor(new RSMSCommandExecutor());
        if (getUUIDsFromConfig()) {
            getArmorStandLocations();
            getSpawnerStates();
            setSpawnerStatesFromConfig();
            //getMobSpawners();
        }

        getAllowedMobs();
        Bukkit.getPluginManager().registerEvents(new RMSListener(this), (Plugin)this);
    }

    public void onDisable() {
        plugin = null;
    }

    public String Name() {
        return getDescription().getName();
    }
    public String Header() {
        return ChatColor.RED + "---------------------- " + ChatColor.GRAY + Name() + ChatColor.RED + " ----------------------";
    }
    public String preHeader() {
        return ChatColor.RED + "---------------------- " + ChatColor.GRAY + Name();
    }

    public Location str2loc(String asUUID) {
        Integer xLoc = (Integer) getConfig().get("RedstoneMobSpawners." + asUUID + ".x");
        Integer yLoc = (Integer) getConfig().get("RedstoneMobSpawners." + asUUID + ".y");
        Integer zLoc = (Integer) getConfig().get("RedstoneMobSpawners." + asUUID + ".z");
        String w = (String) getConfig().get("RedstoneMobSpawners." + asUUID + ".w");
        Location loc = new Location(getServer().getWorld(w), xLoc, yLoc, zLoc);
        return loc;
    }

    private void getArmorStandLocations() {
        this.asLocations = new HashMap<>();
        for (String aStandUUID : this.asUUIDs)
            this.asLocations.put(aStandUUID, str2loc(aStandUUID));
        getLogger().info("RsMS loaded " + this.asLocations.size() + " spawner (Armor Stands) Locations");
    }

    private void getAllowedMobs() {
        this.allowedMobs = new HashMap<>();
        if (getConfig().getConfigurationSection("Mobs") != null)
            for (String s : getConfig().getConfigurationSection("Mobs").getKeys(false))
                this.allowedMobs.put(s, Boolean.valueOf(getConfig().getBoolean("Mobs." + s)));
    }

    private boolean getUUIDsFromConfig() {
        if (getConfig().getConfigurationSection("RedstoneMobSpawners") == null) return false;
        if (!getConfig().getConfigurationSection("RedstoneMobSpawners").getKeys(false).isEmpty()) {
            this.asUUIDs = getConfig().getConfigurationSection("RedstoneMobSpawners").getKeys(false);
            return true;
        }
        return false;
    }

    /**
     * Gets the spawner states from the config to memory.
     */
    private void getSpawnerStates() {
        this.spawnerStates = new HashMap<>();
        for (Map.Entry<String, Location> l : this.asLocations.entrySet()) {
            //getLogger().warning("  " + (String) l.getKey() + " : " + l.getValue());
            if (getConfig().getBoolean("RedstoneMobSpawners." + (String)l.getKey() + ".enabled".toString())) {
                this.spawnerStates.put(l.getValue(), true);
                continue;
            }
            this.spawnerStates.put(l.getValue(), false);
        }
    }

    /**
     * Creates the Armor Stand in the Mob Spawner
     * @param loc Location of Mob Spawner.
     * @return String UUID of the new Armor Stand.
     */
    public String createArmorStand(Location loc) {
        Entity entity = loc.getWorld().spawnEntity(new Location(loc
                .getWorld(), loc.getX() + 0.5D, loc.getY(), loc.getZ() + 0.5D), EntityType.ARMOR_STAND);
        ArmorStand stand = (ArmorStand)entity;
        stand.setSilent(true);
        stand.setAI(true);
        stand.setCustomName("RS Enabled");
        stand.setCustomNameVisible(true);
        stand.setSmall(true);
        stand.setCanPickupItems(false);
        stand.setGravity(false);
        stand.setVisible(false);
        //getLogger().info("stand spawned : " + stand.getEntityId());
        return stand.getUniqueId().toString();
    }

    /**
     * Writes the data to memory and the config file
     * @param loc Location of Mob Spawner.
     * @param p Player
     * @return boolean
     */
    public boolean createRedstoneSpawner(Location loc, Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.create")) {
            Block b = loc.getBlock();
            if (b.getType().equals(Material.SPAWNER)) {
                if (!(this.asLocations == null) && !this.asLocations.isEmpty() && this.asLocations.containsValue(loc)) {
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.DARK_RED + "[FAIL] " + ChatColor.GRAY +
                            "This is already a redstone enabled spawner.");
                } else {
                    if (PermissionChecker.CheckPermission(p, "rsms.requireBlock")) {
                        ItemStack pi = p.getInventory().getItemInMainHand();
                        if (!pi.getType().equals(Material.REDSTONE_BLOCK) || pi.getAmount() < this.redstoneblockcost) {
                            String plural = "";
                            if (this.redstoneblockcost > 1) plural = "s";
                            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you must be holding " +
                                    this.redstoneblockcost.toString() + " Redstone block" + plural + ".");
                            return false;
                        }

                        // remove the Redstone block(s) from the inventory
                        pi.setAmount(pi.getAmount() - this.redstoneblockcost);
                    }
                    String ArmorStandUUID = createArmorStand(loc);
                    CreatureSpawner cs = (CreatureSpawner)loc.getBlock().getState();
                    getConfig().set("RedstoneMobSpawners." + ArmorStandUUID + ".x", Integer.valueOf(loc.getBlockX()));
                    getConfig().set("RedstoneMobSpawners." + ArmorStandUUID + ".y", Integer.valueOf(loc.getBlockY()));
                    getConfig().set("RedstoneMobSpawners." + ArmorStandUUID + ".z", Integer.valueOf(loc.getBlockZ()));
                    getConfig().set("RedstoneMobSpawners." + ArmorStandUUID + ".w", loc.getWorld().getName());
                    getConfig().set("RedstoneMobSpawners." + ArmorStandUUID + ".mob", cs.getCreatureTypeName());
                    getConfig().set("RedstoneMobSpawners." + ArmorStandUUID + ".enabled", Boolean.valueOf(true));
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GREEN + "[SUCCESS] " + ChatColor.GRAY + "You created a Redstone Enabled Spawner!");
                    p.sendMessage(ChatColor.RED + "       " + ChatColor.GRAY + "Attach a redstone lever/button to toggle it.");
                    p.sendMessage("       " + ChatColor.GOLD + "Mob: " + ChatColor.RESET + cs.getCreatureTypeName());
                    saveConfig();
                    getUUIDsFromConfig();
                    getArmorStandLocations();
                    getSpawnerStates();
                    getLogger().info("RSMS: stand spawned at " + loc.toString());
                    return true;
                }
            } else {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you need to be looking at a mobspawner within 3 blocks");
                return false;
            }
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
            return false;
        }
        return false;
    }

    private void setSpawnerStatesFromConfig() {
        for (Location loc : this.spawnerStates.keySet())
            setSpawnerState(loc, this.spawnerStates.get(loc));
    }

    public void listSpawners(Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.list")) {
            if (plugin.spawnerStates == null) {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, we could not find any Redstone enabled spawners.");
            } else {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Listing Information on Redstone Spawners");

                for (Map.Entry<String, Location> asUUID : this.asLocations.entrySet()) {
                    Block b = ((Location)asUUID.getValue()).getBlock();
                    String spawnerMob = getConfig().getString("RedstoneMobSpawners." + (String)asUUID.getKey() + ".mob".toString());
                    Boolean spawnerState = this.spawnerStates.get(b);

                    p.sendMessage(ChatColor.GOLD + "Spawner UUID: " + ChatColor.RESET + (String)asUUID.getKey());
                    p.sendMessage(ChatColor.GOLD + "  Block x,y,z: " + ChatColor.RESET + b.getX() + ", " + b.getY() + ", " + b
                            .getZ());
                    p.sendMessage(ChatColor.GOLD + "  Mob: " + ChatColor.RESET + spawnerMob);
                    p.sendMessage(ChatColor.GOLD + "  Powered: " + ChatColor.RESET + spawnerState);
                    p.sendMessage("");
                }
            }
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
        }
    }

    public boolean changeSpawnerMob(Location loc, Player p, EntityType e) {
        if (PermissionChecker.CheckPermission(p, "rsms.setmob")) {
            if (!loc.getBlock().getType().equals(Material.SPAWNER)) {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "You must be looking directly at a Spawner.");
                return false;
            }
            if (PermissionChecker.CheckPermission(p, "rsms.setmob." + (String)this.mobPermissions.get(e.toString()))) {
                String asUUID = getLocationKey(loc);
                CreatureSpawner cs = (CreatureSpawner)loc.getBlock().getState();
                cs.setSpawnedType(e);
                cs.update();
                getConfig().set("RedstoneMobSpawners." + asUUID + ".mob", e.toString());
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "The spawner will now spawn " + ChatColor.GOLD + e
                        .toString() + "s");
                return true;
            } else {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
        }
        return false;
    }

    public boolean isRedstoneSpawner(Location loc) {
        Location asLoc = loc.getBlock().getLocation().add(0.5D, 0.0D, 0.5D);
        //getLogger().info(asLoc.toString());
        for (Entity entities : asLoc.getWorld().getEntities()) {
            if (entities.getType() == EntityType.ARMOR_STAND) {
                //getLogger().info(entities.getUniqueId().toString());
                if (this.asUUIDs.contains(entities.getUniqueId().toString())) {
                    //getLogger().info(entities.getUniqueId().toString());
                    return true;
                }
            }
        }
        return false;
    }

    public void removeRedstoneSpawnerUUID(String UUID, Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.removeremote")) {
            removeRedstoneSpawner(this.asLocations.get(UUID),p);
        }
    }

    public void removeRedstoneSpawner(Location loc, Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.remove")) {
            if (loc.getBlock().getType().equals(Material.SPAWNER)) {
                String asUUID = getLocationKey(loc);
                for (Entity entities : loc.getWorld().getEntities()) {
                    if (entities.getUniqueId().toString().equals(asUUID)) {
                        entities.remove();
                        getConfig().set("RedstoneMobSpawners." + asUUID, null);
                        saveConfig();
                        this.asLocations.remove(loc);
                        this.asUUIDs.remove(asUUID);
                        getUUIDsFromConfig();
                        getArmorStandLocations();
                        getSpawnerStates();
                        if (PermissionChecker.CheckPermission(p, "rsms.requireBlock")) {
                            ItemStack itemStack = new ItemStack(Material.REDSTONE_BLOCK, this.redstoneblockrefund);
                            p.getInventory().addItem(itemStack);
                        }
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you need to be looking at a mobspawner within 3 blocks");
            }
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
        }
    }

    public void purgeRedstoneSpawners(Location loc, Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.purge")) {
            Location asLoc = loc.getBlock().getLocation().add(0.5D, 0.0D, 0.5D);
            for (Entity entities : asLoc.getWorld().getEntities()) {
                if (entities.getType() == EntityType.ARMOR_STAND &&
                        this.asUUIDs.contains(entities.getUniqueId().toString())) {
                    entities.remove();
                    getConfig().set("RedstoneMobSpawners." + entities.getUniqueId().toString(), null);
                    saveConfig();

                    this.asLocations.remove(loc);
                    this.asUUIDs.remove(entities.getUniqueId().toString());
                    //getUUIDsFromConfig();
                    //getArmorStandLocations();
                    getSpawnerStates();
                }
            }
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
        }
    }

    public boolean isNearDisabledRsSpawner(Location loc) {
        for (Location asLoc : this.asLocations.values()) {
            if (loc.distance(asLoc) <= 9.0D &&
                    !((Boolean)this.spawnerStates.get(asLoc)).booleanValue())
                return true;
        }
        return false;
    }

    public void toggleSpawner(Location loc) {
        String asUUID = getLocationKey(loc);
        for (Entity e : loc.getWorld().getEntities()) {
            if (e.getUniqueId().toString().equals(asUUID)) {
                Boolean newstate = Boolean.valueOf(!((Boolean)this.spawnerStates.get(loc)).booleanValue());
                this.spawnerStates.put(loc, newstate);
                if (newstate) e.setCustomName("RS Enabled"); else e.setCustomName("RS Disabled");
                getConfig().set("RedstoneMobSpawners." + asUUID + ".enabled", newstate);
                saveConfig();
            }
        }
    }

    public void setSpawnerState(Location loc, Boolean state) {
        String asUUID = getLocationKey(loc);
        for (Entity e : loc.getWorld().getEntities()) {
            if (e.getUniqueId().toString().equals(asUUID)) {
                if (!state.booleanValue()) {
                    e.setCustomName("RS Disabled");
                    //getConfig().set("RedstoneMobSpawners." + asUUID + ".enabled", false);
                    continue;
                }
                e.setCustomName("RS Enabled");
                //getConfig().set("RedstoneMobSpawners." + asUUID + ".enabled", true);
            }
            saveConfig();
        }
    }

    public String getLocationKey(Location loc) {
        for (Map.Entry<String, Location> e : this.asLocations.entrySet()) {
            if (loc.equals(e.getValue()))
                return e.getKey();
        }
        return null;
    }

    public void spawnerInfo(Location loc, Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.info")) {
            Block b = loc.getBlock();
            String spawnerUUID = getLocationKey(loc);
            String spawnerMob = getConfig().getString("RedstoneMobSpawners." + spawnerUUID + ".mob".toString());
            Boolean spawnerState = this.spawnerStates.get(loc);
            if (b.getType().equals(Material.SPAWNER)) {
                if (!this.asLocations.containsValue(loc)) {
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, this spawner is not redstone enabled.");
                } else if (plugin.spawnerStates.isEmpty()) {
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, we could not find any Redstone enabled spawners.");
                } else {
                    p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Info for Redstone Enabled Spawner!");
                    p.sendMessage(ChatColor.GOLD + "Spawner UUID: " + ChatColor.RESET + spawnerUUID);
                    p.sendMessage(ChatColor.GOLD + "  Block x,y,z: " + ChatColor.RESET + b.getX() + ", " + b.getY() + ", " + b
                            .getZ());
                    p.sendMessage(ChatColor.GOLD + "  Mob: " + ChatColor.RESET + spawnerMob);
                    p.sendMessage(ChatColor.GOLD + "  Spawning: " + ChatColor.RESET + spawnerState);
                    p.sendMessage(ChatColor.GOLD + "Actions:");
                    sendClickableCommand(p,ChatColor.RESET + "    Remove","rsms remove " + spawnerUUID);
                }
            } else {
                p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, we could not find any Redstone enabled spawners.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
        }
    }

    public void sendClickableCommand(Player player, String message, String command) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        player.spigot().sendMessage(component);
    }

    public void reloadConfig(Player p) {
        if (PermissionChecker.CheckPermission(p, "rsms.reload")) {
            getConfig().options().copyDefaults(true);
            saveConfig();
            getUUIDsFromConfig();
            getArmorStandLocations();
            getSpawnerStates();
            getAllowedMobs();
            setSpawnerStatesFromConfig();
        } else {
            p.sendMessage(ChatColor.RED + "RSMS " + ChatColor.GRAY + "Sorry, you don't have the required permission.");
        }
    }
}
