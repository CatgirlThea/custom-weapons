package com.customweapons.plugin.weapons;

import com.customweapons.plugin.WeaponsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SiphonTridentListener implements Listener {

    private final WeaponsPlugin plugin;
    private final WeaponItemManager weaponManager;
    private final Map<UUID, Long> lastSiphonMessage = new HashMap<>();

    private static final float SIPHON_MIN_CHARGE = 0.7F;

    public SiphonTridentListener(WeaponsPlugin plugin, WeaponItemManager weaponManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
    }

    private boolean isSiphonTrident(ItemStack item) {
        return weaponManager.getWeaponType(item) == WeaponType.SIPHON_TRIDENT;
    }

    private boolean holdsSiphonTrident(Player player) {
        return isSiphonTrident(player.getInventory().getItemInMainHand())
                || isSiphonTrident(player.getInventory().getItemInOffHand());
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player damaged = event.getPlayer();
        int lost = event.getDamage();
        if (lost <= 0) return;

        for (Player holder : plugin.getServer().getOnlinePlayers()) {
            if (holder.equals(damaged)) continue;
            if (!holdsSiphonTrident(holder)) continue;
            if (holder.getCooledAttackStrength(0.0F) < SIPHON_MIN_CHARGE) continue;

            int repaired = repairArmor(holder, lost);
            if (repaired <= 0) continue;

            long now = System.currentTimeMillis();
            Long last = lastSiphonMessage.get(holder.getUniqueId());
            if (last == null || now - last > 1500) {
                lastSiphonMessage.put(holder.getUniqueId(), now);
                holder.sendActionBar(Component.text("Siphon Trident", NamedTextColor.DARK_AQUA)
                        .append(Component.text(": siphoned " + repaired + " durability to your armor", NamedTextColor.GRAY)));
            }
        }
    }

    private int repairArmor(Player holder, int amount) {
        ItemStack[] slots = {
                holder.getInventory().getHelmet(),
                holder.getInventory().getChestplate(),
                holder.getInventory().getLeggings(),
                holder.getInventory().getBoots()
        };
        int[] damage = new int[4];
        List<Integer> repairable = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            ItemStack item = slots[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType().getMaxDurability() <= 0) continue;
            damage[i] = ((Damageable) item.getItemMeta()).getDamage();
            repairable.add(i);
        }
        repairable.sort((a, b) -> Integer.compare(damage[b], damage[a]));

        int remaining = amount;
        int repaired = 0;
        for (int index : repairable) {
            if (remaining <= 0) break;
            ItemStack item = slots[index];
            Damageable damageable = (Damageable) item.getItemMeta();
            int dmg = Math.max(0, damageable.getDamage());
            if (dmg <= 0) continue;
            int give = Math.min(dmg, remaining);
            damageable.setDamage(dmg - give);
            item.setItemMeta(damageable);
            remaining -= give;
            repaired += give;
        }
        return repaired;
    }

    @EventHandler
    public void onLaunchTrident(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident)) return;
        ProjectileSource source = event.getEntity().getShooter();
        if (!(source instanceof Player player)) return;
        if (!isSiphonTrident(player.getInventory().getItemInMainHand())) return;
        event.setCancelled(true);
        player.sendActionBar(Component.text("Siphon Trident cannot be thrown!", NamedTextColor.RED));
    }

    @EventHandler
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isSiphonTrident(player.getInventory().getItemInMainHand())) return;
        event.setDamage(WeaponType.SIPHON_TRIDENT.getMeleeDamage() + AttackUtil.strengthBonus(player));
    }
}