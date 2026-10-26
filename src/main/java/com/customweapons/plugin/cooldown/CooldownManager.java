package com.customweapons.plugin.cooldown;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(Player player, String abilityName, long durationMillis) {
        UUID uuid = player.getUniqueId();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(abilityName, System.currentTimeMillis() + durationMillis);
    }

    public boolean hasCooldown(Player player, String abilityName) {
        return getRemainingCooldown(player, abilityName) > 0;
    }

    public long getRemainingCooldown(Player player, String abilityName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long expiry = playerCooldowns.get(abilityName);
        if (expiry == null) return 0;

        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            playerCooldowns.remove(abilityName);
            return 0;
        }
        return remaining;
    }

    public long getRemainingCooldownSeconds(Player player, String abilityName) {
        return (getRemainingCooldown(player, abilityName) + 999) / 1000;
    }

    public void removeCooldown(Player player, String abilityName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns != null) {
            playerCooldowns.remove(abilityName);
        }
    }

    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public void clearAllCooldowns() {
        cooldowns.clear();
    }
}
