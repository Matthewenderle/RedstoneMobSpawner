package io.enderle.rsms;


import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class Spawner extends Main {
    public void toggleSpawner(Location loc) {
        this.spawnerStates.put(loc, !(Boolean) this.spawnerStates.get(loc));
        getLogger().info("1");
        for (Entity entities : loc.getWorld().getEntities()) {
            getLogger().info("2");
            for (Map.Entry<String, Location> e : this.asLocations.entrySet()) {
                getLogger().info("3a: " + loc);
                getLogger().info("3b: " + e.getValue());
                getLogger().info("3c: " + e.getValue().add(0.5D, 0.0D, 0.5D));
                if (loc == e.getValue()) {
                    getLogger().info("4");
                    if (this.spawnerStates.get(loc)) {
                        entities.setCustomName("RS Enabled");
                        continue;
                    }
                    entities.setCustomName("RS Disabled");
                }
            }
        }
        for (Map.Entry<String, Location> e : this.asLocations.entrySet()) {
            if (loc.toString().equals(e.getValue().toString())) {
                getConfig().set("RedstoneMobSpawner." + e.getKey() + ".enabled", this.spawnerStates.get(loc));
                saveConfig();
            }
        }
    }
}
