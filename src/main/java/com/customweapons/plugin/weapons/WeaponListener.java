package com.customweapons.plugin.weapons;

import com.customweapons.plugin.WeaponsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.UUID;

public class WeaponListener implements Listener {

    private final WeaponsPlugin plugin;
    private final WeaponItemManager weaponManager;
    private final StunManager stunManager;
    private final NamespacedKey keyStunShooter;

    public WeaponListener(WeaponsPlugin plugin, WeaponItemManager weaponManager, StunManager stunManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.stunManager = stunManager;
        this.keyStunShooter = new NamespacedKey(plugin, "stun_bow_shooter");
    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (stunManager.isStunned(player)) {
            event.setCancelled(true);
            player.sendActionBar(Component.text("You are stunned and cannot attack!", NamedTextColor.RED));
            return;
        }

        ItemStack bow = event.getBow();
        if (bow == null) return;
        if (weaponManager.getWeaponType(bow) != WeaponType.STUN_CROSSBOW) return;

        event.getProjectile().getPersistentDataContainer().set(
                keyStunShooter, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        String shooterId = projectile.getPersistentDataContainer().get(keyStunShooter, PersistentDataType.STRING);
        if (shooterId == null) return;

        Entity hitEntity = event.getHitEntity();
        if (!(hitEntity instanceof LivingEntity target)) return;

        Player shooter = Bukkit.getPlayer(UUID.fromString(shooterId));
        if (shooter == null || !shooter.isOnline()) return;

        ItemStack crossbow = weaponManager.findStunCrossbow(shooter);
        if (crossbow == null) return;

        int threshold = WeaponType.STUN_CROSSBOW.getShotsToStun();
        int shots = weaponManager.getStunShots(crossbow) + 1;

        if (shots >= threshold) {
            weaponManager.setStunShots(crossbow, 0);
            stunManager.stun(target, WeaponType.STUN_CROSSBOW.getStunDurationMillis());

            shooter.sendActionBar(
                    Component.text("STUN CROSSBOW ", NamedTextColor.YELLOW, TextDecoration.BOLD)
                            .append(Component.text(threshold + "/" + threshold + " ", NamedTextColor.WHITE))
                            .append(Component.text("- ", NamedTextColor.GRAY))
                            .append(Component.text(target.getName() + " stunned for " +
                                    (WeaponType.STUN_CROSSBOW.getStunDurationMillis() / 1000) + "s!", NamedTextColor.GOLD)));
        } else {
            weaponManager.setStunShots(crossbow, shots);
            shooter.sendActionBar(
                    Component.text("Stun Crossbow", NamedTextColor.AQUA)
                            .append(Component.text(": " + shots + "/" + threshold + " hits", NamedTextColor.GRAY)));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof LivingEntity living && stunManager.isStunned(living)) {
            event.setCancelled(true);
            return;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof LivingEntity living && stunManager.isStunned(living)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!stunManager.isStunned(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setCancelled(true);
            player.setVelocity(new Vector(0, 0, 0));
        }
    }
}