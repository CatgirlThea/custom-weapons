package com.customweapons.plugin.weapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WeaponItemManager {

    private static final UUID BLACK_KNIFE_DAMAGE_UUID =
            UUID.fromString("9f3a1c54-bb2d-4c6e-8a1f-6e7d0b2a8c33");
    private static final UUID SIPHON_TRIDENT_DAMAGE_UUID =
            UUID.fromString("2b7e9d31-c4f5-4a20-9b8e-0f8c1a2d3e44");
    private static final UUID SIPHON_TRIDENT_SPEED_UUID =
            UUID.fromString("4d9c0b72-e8a1-4b3f-9c4d-2f6a7b8c9e55");

    private final Plugin plugin;
    private final NamespacedKey keyIsWeapon;
    private final NamespacedKey keyWeaponType;
    private final NamespacedKey keyStunShots;

    public WeaponItemManager(Plugin plugin) {
        this.plugin = plugin;
        this.keyIsWeapon = new NamespacedKey(plugin, "is_weapon");
        this.keyWeaponType = new NamespacedKey(plugin, "weapon_type");
        this.keyStunShots = new NamespacedKey(plugin, "stun_shots");
    }

    public NamespacedKey getKeyIsWeapon() { return keyIsWeapon; }
    public NamespacedKey getKeyWeaponType() { return keyWeaponType; }
    public NamespacedKey getKeyStunShots() { return keyStunShots; }

    public ItemStack createWeapon(WeaponType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(type.getDisplayNameComponent().decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        if (type == WeaponType.STUN_CROSSBOW) {
            lore.add(Component.empty());
            lore.add(Component.text("Charges on every hit shot.", NamedTextColor.GRAY));
            lore.add(Component.text("After " + type.getShotsToStun() + " hits the target is", NamedTextColor.GRAY));
            lore.add(Component.text("stunned for " + (type.getStunDurationMillis() / 1000) + " seconds!", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text(type.getShotsToStun() + " charges to stun", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Right-click to fire", NamedTextColor.DARK_GRAY));
        } else if (type == WeaponType.BLACK_KNIFE) {
            lore.add(Component.empty());
            lore.add(Component.text("A blade forged from pure darkness.", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Deals " + (int) type.getMeleeDamage() + " damage on hit", NamedTextColor.GRAY));
            lore.add(Component.text("Full damage only on fully charged swings", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Cannot be enchanted", NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("§8Right-click: Wraith Slash", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Deals " + ((int) type.getAbilityDamage() / 2) + " hearts of true damage", NamedTextColor.RED));
            lore.add(Component.text("Ignores armor & protection", NamedTextColor.DARK_RED));
            lore.add(Component.text("Cooldown: " + (type.getAbilityCooldownMillis() / 60000) + " minutes", NamedTextColor.RED));
        } else if (type == WeaponType.SIPHON_TRIDENT) {
            lore.add(Component.empty());
            lore.add(Component.text("A trident that drinks in broken gear.", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Deals " + (int) type.getMeleeDamage() + " damage on hit", NamedTextColor.GRAY));
            lore.add(Component.text("Sword attack speed", NamedTextColor.GRAY));
            lore.add(Component.text("Cannot be thrown", NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Durability lost by other players", NamedTextColor.DARK_AQUA));
            lore.add(Component.text("repairs your armor while held", NamedTextColor.DARK_AQUA));
            lore.add(Component.text("Requires 70% swing charge", NamedTextColor.DARK_GRAY));
        }
        meta.lore(lore);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(keyIsWeapon, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(keyWeaponType, PersistentDataType.STRING, type.name());
        if (type == WeaponType.STUN_CROSSBOW) {
            meta.getPersistentDataContainer().set(keyStunShots, PersistentDataType.INTEGER, 0);
        } else if (type == WeaponType.BLACK_KNIFE) {
            meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                    BLACK_KNIFE_DAMAGE_UUID, "black_knife_damage", type.getMeleeDamage() - 1.0,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        } else if (type == WeaponType.SIPHON_TRIDENT) {
            meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
            meta.removeAttributeModifier(Attribute.ATTACK_SPEED);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                    SIPHON_TRIDENT_DAMAGE_UUID, "siphon_trident_damage", type.getMeleeDamage() - 1.0,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                    SIPHON_TRIDENT_SPEED_UUID, "siphon_trident_speed", -2.4,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        }

        item.setItemMeta(meta);
        return item;
    }

    public boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyIsWeapon, PersistentDataType.BYTE);
    }

    public WeaponType getWeaponType(ItemStack item) {
        if (!isWeapon(item)) return null;
        ItemMeta meta = item.getItemMeta();
        String typeName = meta.getPersistentDataContainer().get(keyWeaponType, PersistentDataType.STRING);
        if (typeName == null) return null;
        try {
            return WeaponType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getStunShots(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        Integer shots = meta.getPersistentDataContainer().get(keyStunShots, PersistentDataType.INTEGER);
        return shots != null ? shots : 0;
    }

    public void setStunShots(ItemStack item, int shots) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyStunShots, PersistentDataType.INTEGER, Math.max(0, shots));
        item.setItemMeta(meta);
    }

    public ItemStack findStunCrossbow(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            if (getWeaponType(item) == WeaponType.STUN_CROSSBOW) {
                return item;
            }
        }
        return null;
    }
}