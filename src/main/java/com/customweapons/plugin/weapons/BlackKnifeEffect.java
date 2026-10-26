package com.customweapons.plugin.weapons;

import com.customweapons.plugin.WeaponsPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class BlackKnifeEffect {

    private static final Color BLACK = Color.fromRGB(12, 12, 12);
    private static final double RING_RADIUS = 1.1;
    private static final int RING_PARTICLES = 8;

    private static BukkitTask particleTask;

    public static void start(WeaponsPlugin plugin, WeaponItemManager weaponManager) {
        if (particleTask != null) return;
        particleTask = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                tick++;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!holdingBlackKnife(player, weaponManager)) continue;
                    spawnAura(player, tick);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static boolean holdingBlackKnife(Player player, WeaponItemManager weaponManager) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (weaponManager.getWeaponType(main) == WeaponType.BLACK_KNIFE) return true;
        ItemStack off = player.getInventory().getItemInOffHand();
        return weaponManager.getWeaponType(off) == WeaponType.BLACK_KNIFE;
    }

    private static void spawnAura(Player player, int tick) {
        World world = player.getWorld();
        Location center = player.getLocation();
        double baseY = center.getY() + 1.05;
        double angleBase = tick * 0.22;
        double bob = Math.sin(tick * 0.12) * 0.15;

        for (int i = 0; i < RING_PARTICLES; i++) {
            double a = angleBase + (2 * Math.PI * i / RING_PARTICLES);
            double y = baseY + bob + Math.sin(a * 2 + tick * 0.3) * 0.2;
            double x = center.getX() + Math.cos(a) * RING_RADIUS;
            double z = center.getZ() + Math.sin(a) * RING_RADIUS;
            world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, new Particle.DustOptions(BLACK, 1.0f));
        }

        if (tick % 4 == 0) {
            world.spawnParticle(Particle.SMOKE, center.getX(), center.getY() + 0.4, center.getZ(),
                    1, 0.1, 0.02, 0.1, 0.01);
        }
    }

    public static void spawnSlash(Player player, boolean big) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Vector up = new Vector(0, 1, 0);
        Vector right = dir.getCrossProduct(up);
        if (right.lengthSquared() < 1e-4) {
            right = new Vector(1, 0, 0);
        }
        right.normalize();
        Vector forward = right.getCrossProduct(up).normalize();

        World world = player.getWorld();
        final Location base = eye.clone();
        final Vector fwd = forward.clone();
        final Vector rgt = right.clone();
        final int frames = big ? 12 : 7;
        final double maxRadius = big ? 6.0 : 4.0;
        final double sweep = big ? 85.0 : 60.0;
        final float particleSize = big ? 2.2f : 1.4f;

        new BukkitRunnable() {
            private int frame = 0;

            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    return;
                }
                frame++;
                double radius = maxRadius * (frame / (double) frames);

                for (double angleDeg = -sweep; angleDeg <= sweep; angleDeg += 10) {
                    double a = Math.toRadians(angleDeg);
                    double cos = Math.cos(a);
                    double sin = Math.sin(a);

                    double x = base.getX() + fwd.getX() * radius * cos + rgt.getX() * radius * sin * 0.75;
                    double y = base.getY() - 0.55 + Math.abs(sin) * radius * (big ? 0.5 : 0.3);
                    double z = base.getZ() + fwd.getZ() * radius * cos + rgt.getZ() * radius * sin * 0.75;

                    world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(BLACK, particleSize));
                }

                if (big && frame % 3 == 0) {
                    double tipX = base.getX() + fwd.getX() * radius;
                    double tipY = base.getY() - 0.55;
                    double tipZ = base.getZ() + fwd.getZ() * radius;
                    world.spawnParticle(Particle.LARGE_SMOKE, tipX, tipY, tipZ, 2, 0.2, 0.2, 0.2, 0);
                }
            }
        }.runTaskTimer(WeaponsPlugin.getInstance(), 0L, 1L);
    }
}
