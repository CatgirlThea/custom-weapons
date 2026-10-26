package com.customweapons.plugin.weapons;

import com.customweapons.plugin.cooldown.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BlackKnifeAbility {

    public static final String ABILITY_NAME = "Wraith Slash";
    public static final long COOLDOWN_MS = 360_000;
    public static final double DAMAGE = 10.0; // 5 hearts, true damage (ignores armor & protection)
    public static final double RANGE = 5.5;

    private static final Set<UUID> TRUE_DAMAGE_VICTIMS = new HashSet<>();

    public static boolean isMarked(Entity entity) {
        return TRUE_DAMAGE_VICTIMS.contains(entity.getUniqueId());
    }

    public static void consumeTrueDamage(Entity entity) {
        TRUE_DAMAGE_VICTIMS.remove(entity.getUniqueId());
    }

    public static void applyTrueDamage(LivingEntity target, double amount, Player source) {
        TRUE_DAMAGE_VICTIMS.add(target.getUniqueId());
        target.damage(amount, source);
    }

    public static boolean activate(Player player, CooldownManager cooldownManager) {
        if (cooldownManager.hasCooldown(player, ABILITY_NAME)) {
            long seconds = cooldownManager.getRemainingCooldownSeconds(player, ABILITY_NAME);
            player.sendActionBar(
                    Component.text("Black Knife", NamedTextColor.DARK_GRAY)
                            .append(Component.text(": Wraith Slash on cooldown: ", NamedTextColor.GRAY))
                            .append(Component.text(seconds + "s", NamedTextColor.RED)));
            return false;
        }

        cooldownManager.setCooldown(player, ABILITY_NAME, COOLDOWN_MS);
        BlackKnifeEffect.spawnSlash(player, true);

        Vector look = player.getEyeLocation().getDirection().normalize();
        List<LivingEntity> targets = player.getNearbyEntities(RANGE, 4.5, RANGE).stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(e -> !e.getUniqueId().equals(player.getUniqueId()))
                .filter(e -> {
                    Vector to = e.getEyeLocation().subtract(player.getEyeLocation()).toVector().normalize();
                    return look.dot(to) >= 0.6;
                })
                .toList();

        for (LivingEntity target : targets) {
            applyTrueDamage(target, DAMAGE, player);
        }

        player.sendActionBar(
                Component.text("WRAITH SLASH!", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                        .append(Component.text(" " + (targets.size()) + " enemy", NamedTextColor.GRAY))
                        .append(targets.size() == 1 ? Component.text(" hit!", NamedTextColor.GRAY)
                                : Component.text("s hit!", NamedTextColor.GRAY)));
        return true;
    }
}