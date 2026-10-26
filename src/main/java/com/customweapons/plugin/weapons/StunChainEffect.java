package com.customweapons.plugin.weapons;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StunChainEffect {

    private static final int CHAIN_COUNT = 6;
    private static final double RADIUS = 1.15;
    private static final org.bukkit.NamespacedKey CHAIN_MARKER =
            new org.bukkit.NamespacedKey("undertalesouls", "stun_chain");

    private static final Map<UUID, List<ItemDisplay>> chains = new ConcurrentHashMap<>();
    private static BukkitTask tickTask;

    public static void start(com.customweapons.plugin.WeaponsPlugin plugin) {
        if (tickTask != null) return;
        cleanupOrphans(plugin);
        tickTask = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                tick++;
                double angle = tick * 0.25;

                chains.entrySet().removeIf(entry -> {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) {
                        despawn(entry.getValue());
                        return true;
                    }
                    return false;
                });

                for (Map.Entry<UUID, List<ItemDisplay>> entry : chains.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) continue;

                    List<ItemDisplay> displays = entry.getValue();
                    Location base = player.getLocation();
                    for (int i = 0; i < displays.size(); i++) {
                        ItemDisplay display = displays.get(i);
                        if (!display.isValid()) continue;

                        double a = angle + (2 * Math.PI * i / displays.size());
                        double x = Math.cos(a) * RADIUS;
                        double z = Math.sin(a) * RADIUS;
                        double y = 0.6 + 0.35 * Math.sin(2 * a + tick * 0.2);

                        display.teleport(base.clone().add(x, y, z));
                        display.setTransformation(new Transformation(
                                new Vector3f(0, 0, 0),
                                new Quaternionf().rotateY(tick * 0.3f).rotateX((float) Math.sin(a) * 0.5f),
                                new Vector3f(0.7f),
                                new Quaternionf()));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public static void spawn(Player player) {
        remove(player);

        List<ItemDisplay> displays = new ArrayList<>();
        World world = player.getWorld();
        Location center = player.getLocation();

        for (int i = 0; i < CHAIN_COUNT; i++) {
            double a = 2 * Math.PI * i / CHAIN_COUNT;
            Location loc = center.clone().add(Math.cos(a) * RADIUS, 0.9, Math.sin(a) * RADIUS);

            ItemDisplay display = world.spawn(loc, ItemDisplay.class, e -> {
                e.setItemStack(new ItemStack(Material.IRON_CHAIN));
                e.setGlowing(true);
                e.setGlowColorOverride(Color.YELLOW);
                e.setGravity(false);
                e.setInvulnerable(true);
                e.setVisibleByDefault(true);
                e.getPersistentDataContainer().set(CHAIN_MARKER,
                        org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            });

            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(0.7f),
                    new Quaternionf()));

            displays.add(display);
        }

        chains.put(player.getUniqueId(), displays);
    }

    public static void remove(Player player) {
        if (player == null) return;
        List<ItemDisplay> displays = chains.remove(player.getUniqueId());
        despawn(displays);
    }

    private static void despawn(List<ItemDisplay> displays) {
        if (displays == null) return;
        for (ItemDisplay display : displays) {
            if (display.isValid()) {
                display.remove();
            }
        }
    }

    public static void stopAll() {
        chains.forEach((uuid, displays) -> despawn(displays));
        chains.clear();
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private static void cleanupOrphans(com.customweapons.plugin.WeaponsPlugin plugin) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ItemDisplay display &&
                        display.getPersistentDataContainer().has(CHAIN_MARKER,
                                org.bukkit.persistence.PersistentDataType.BYTE)) {
                    display.remove();
                }
            }
        }
    }
}