package com.heregravity.heregravity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class HereGravity extends JavaPlugin implements Listener {

    // Using EnumMap to store the Material and its specific breaks-on-fall setting
    private final Map<Material, Boolean> gravityBlocks = new EnumMap<>(Material.class);
    private final Set<String> scheduledFalls = new HashSet<>();
    private final Set<String> scheduledDecays = new HashSet<>();

    private String getBlockKey(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    @Override
    public void onEnable() {
        // Saves the default config.yml from your resources folder if it doesn't exist
        saveDefaultConfig();
        loadGravityConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("HereGravity enabled! Loaded " + gravityBlocks.size() + " gravity blocks.");
    }

    private void loadGravityConfig() {
        gravityBlocks.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("gravity-list");
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            boolean breaksOnFall = section.getBoolean(key + ".breaks-on-fall", false);

            // Check if the config key is a Minecraft Tag (e.g., #logs)
            if (key.startsWith("#")) {
                String tagName = key.substring(1).toLowerCase();
                Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tagName),
                        Material.class);
                if (tag != null) {
                    for (Material mat : tag.getValues()) {
                        gravityBlocks.put(mat, breaksOnFall);
                    }
                } else {
                    getLogger().warning("Could not find Minecraft tag: " + key);
                }
            }
            // Otherwise, parse it as a standard block material
            else {
                Material mat = Material.matchMaterial(key.toUpperCase());
                if (mat != null) {
                    gravityBlocks.put(mat, breaksOnFall);
                } else {
                    getLogger().warning("Invalid material in config: " + key);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        // 1. The "Shatter" Canopy System (Fast Leaf Decay)
        if (Tag.LEAVES.isTagged(type)) {
            if (block.getBlockData() instanceof Leaves leaves) {
                if (leaves.getDistance() > 6 && !leaves.isPersistent()) {
                    String key = getBlockKey(block);
                    if (scheduledDecays.add(key)) {
                        Bukkit.getScheduler().runTask(this, () -> {
                            try {
                                if (block.getType() == type) {
                                    block.breakNaturally();
                                }
                            } finally {
                                scheduledDecays.remove(key);
                            }
                        });
                    }
                }
            }
            return;
        }

        // 2. The Configured Gravity System
        if (gravityBlocks.containsKey(type)) {
            Block below = block.getRelative(BlockFace.DOWN);

            if (below.isEmpty() || below.isLiquid()) {
                String key = getBlockKey(block);
                if (scheduledFalls.add(key)) {
                    Bukkit.getScheduler().runTask(this, () -> {
                        try {
                            if (block.getType() == type && (block.getRelative(BlockFace.DOWN).isEmpty()
                                    || block.getRelative(BlockFace.DOWN).isLiquid())) {
                                BlockData data = block.getBlockData();
                                block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), data);
                                block.setType(Material.AIR);
                            }
                        } finally {
                            scheduledFalls.remove(key);
                        }
                    });
                }
            }
        }
    }

    // 3. The "Breaks-on-Fall" Landing Interceptor
    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        // Check if the entity changing the block is a falling block landing on the
        // ground
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            Material landingMaterial = event.getTo();

            // If this block is in our config and breaks-on-fall is true
            if (gravityBlocks.getOrDefault(landingMaterial, false)) {
                // Cancel the block placement
                event.setCancelled(true);

                // Drop it as a collectible item at the exact location it landed
                event.getEntity().getWorld().dropItemNaturally(
                        event.getEntity().getLocation(),
                        new ItemStack(landingMaterial));
            }
        }
    }
}