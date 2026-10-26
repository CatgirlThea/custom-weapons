package com.customweapons.plugin.weapons;

import com.customweapons.plugin.WeaponsPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StunManager {

    private final Map<UUID, Long> stunExpiry = new HashMap<>();

    public boolean isStunned(LivingEntity entity) {
        Long expiry = stunExpiry.get(entity.getUniqueId());
        if (expiry == null) return false;
        if (expiry <= System.currentTimeMillis()) {
            stunExpiry.remove(entity.getUniqueId());
            return false;
        }
        return true;
    }

    public long getRemainingMillis(LivingEntity entity) {
        Long expiry = stunExpiry.get(entity.getUniqueId());
        if (expiry == null) return 0;
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    public void stun(LivingEntity entity, long durationMillis) {
        long expiry = System.currentTimeMillis() + durationMillis;
        stunExpiry.put(entity.getUniqueId(), expiry);

        if (entity instanceof Player player) {
            StunChainEffect.spawn(player);
        } else {
            entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    (int) (durationMillis / 50),
                    255,
                    false, false, true));
        }

        WeaponsPlugin.getInstance().getServer().getScheduler().runTaskLater(
                WeaponsPlugin.getInstance(), () -> {
                    if (stunExpiry.getOrDefault(entity.getUniqueId(), 0L) == expiry) {
                        unstun(entity);
                    }
                }, durationMillis / 50);
    }

    public void unstun(LivingEntity entity) {
        if (!stunExpiry.containsKey(entity.getUniqueId())) return;
        stunExpiry.remove(entity.getUniqueId());
        if (entity instanceof Player player) {
            StunChainEffect.remove(player);
        }
    }

    public void clear() {
        stunExpiry.clear();
    }
}