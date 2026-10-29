package com.customweapons.plugin.weapons;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Shared combat math for custom weapons.
 */
public final class AttackUtil {

    private static final double STRENGTH_DAMAGE_PER_LEVEL = 3.0;

    private AttackUtil() {
    }

    /**
     * Flat attack-damage bonus granted by the vanilla Strength effect (+3 per level).
     */
    public static double strengthBonus(Player player) {
        PotionEffect effect = player.getPotionEffect(PotionEffectType.STRENGTH);
        if (effect == null) return 0.0;
        return STRENGTH_DAMAGE_PER_LEVEL * (effect.getAmplifier() + 1);
    }
}
