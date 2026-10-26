package com.customweapons.plugin.weapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum WeaponType {

    STUN_CROSSBOW("Stun Crossbow", Material.CROSSBOW, 15, 4000),
    BLACK_KNIFE("Black Knife", Material.NETHERITE_SWORD, 14.0, 10.0, 360_000),
    SIPHON_TRIDENT("Siphon Trident", Material.TRIDENT, 13.0, 0.0, 0);

    private final String displayName;
    private final Material material;
    private final double meleeDamage;
    private final double abilityDamage;
    private final long abilityCooldownMillis;
    private final int shotsToStun;
    private final long stunDurationMillis;

    WeaponType(String displayName, Material material, int shotsToStun, long stunDurationMillis) {
        this(displayName, material, 0.0, 0.0, 0, shotsToStun, stunDurationMillis);
    }

    WeaponType(String displayName, Material material, double meleeDamage, double abilityDamage, long abilityCooldownMillis) {
        this(displayName, material, meleeDamage, abilityDamage, abilityCooldownMillis, 0, 0);
    }

    private WeaponType(String displayName, Material material, double meleeDamage, double abilityDamage,
                       long abilityCooldownMillis, int shotsToStun, long stunDurationMillis) {
        this.displayName = displayName;
        this.material = material;
        this.meleeDamage = meleeDamage;
        this.abilityDamage = abilityDamage;
        this.abilityCooldownMillis = abilityCooldownMillis;
        this.shotsToStun = shotsToStun;
        this.stunDurationMillis = stunDurationMillis;
    }

    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public double getMeleeDamage() { return meleeDamage; }
    public double getAbilityDamage() { return abilityDamage; }
    public long getAbilityCooldownMillis() { return abilityCooldownMillis; }
    public int getShotsToStun() { return shotsToStun; }
    public long getStunDurationMillis() { return stunDurationMillis; }

    public Component getDisplayNameComponent() {
        return Component.text(displayName, NamedTextColor.AQUA);
    }

    public static WeaponType fromString(String name) {
        for (WeaponType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}