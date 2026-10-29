package com.customweapons.plugin.weapons;

import com.customweapons.plugin.WeaponsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlackKnifeListener implements Listener {

    private static final Set<Material> INTERACTIVE_MATERIALS = new HashSet<>(Arrays.asList(
            Material.CRAFTING_TABLE,
            Material.ENCHANTING_TABLE,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.GRINDSTONE,
            Material.LECTERN,
            Material.LOOM,
            Material.STONECUTTER,
            Material.CARTOGRAPHY_TABLE,
            Material.SMITHING_TABLE,
            Material.BEACON,
            Material.CAKE,
            Material.REPEATER,
            Material.COMPARATOR,
            Material.LEVER,
            Material.NOTE_BLOCK,
            Material.CAULDRON,
            Material.WATER_CAULDRON,
            Material.LAVA_CAULDRON,
            Material.POWDER_SNOW_CAULDRON,
            Material.DECORATED_POT,
            Material.DRAGON_EGG,
            Material.RESPAWN_ANCHOR,
            Material.END_PORTAL_FRAME,
            Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.MANGROVE_DOOR, Material.CHERRY_DOOR,
            Material.BAMBOO_DOOR, Material.CRIMSON_DOOR, Material.WARPED_DOOR,
            Material.IRON_DOOR,
            Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
            Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.CHERRY_TRAPDOOR,
            Material.BAMBOO_TRAPDOOR, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR, Material.IRON_TRAPDOOR,
            Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
            Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.MANGROVE_FENCE_GATE,
            Material.CHERRY_FENCE_GATE, Material.BAMBOO_FENCE_GATE, Material.CRIMSON_FENCE_GATE,
            Material.WARPED_FENCE_GATE,
            Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON,
            Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.MANGROVE_BUTTON, Material.CHERRY_BUTTON,
            Material.BAMBOO_BUTTON, Material.CRIMSON_BUTTON, Material.WARPED_BUTTON,
            Material.STONE_BUTTON,
            Material.FLOWER_POT
    ));

    private final WeaponsPlugin plugin;
    private final WeaponItemManager weaponManager;

    public BlackKnifeListener(WeaponsPlugin plugin, WeaponItemManager weaponManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
    }

    private boolean isBlackKnife(ItemStack item) {
        return weaponManager.getWeaponType(item) == WeaponType.BLACK_KNIFE;
    }

    @EventHandler
    public void onSlash(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player player = event.getPlayer();
        if (!isBlackKnife(player.getInventory().getItemInMainHand())) return;
        BlackKnifeEffect.spawnSlash(player, false);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;
        ItemStack item = event.getItem();
        if (item == null || !isBlackKnife(item)) return;

        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                && isInteractableBlock(event.getClickedBlock())) {
            return;
        }

        event.setCancelled(true);
        BlackKnifeAbility.activate(event.getPlayer(), plugin.getCooldownManager());
    }

    private static boolean isInteractableBlock(org.bukkit.block.Block block) {
        if (block == null) return false;
        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder) return true;
        return INTERACTIVE_MATERIALS.contains(block.getType());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTrueDamage(EntityDamageEvent event) {
        if (!BlackKnifeAbility.isMarked(event.getEntity())) return;
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            if (modifier == EntityDamageEvent.DamageModifier.BASE) continue;
            if (modifier == EntityDamageEvent.DamageModifier.ABSORPTION) continue;
            if (modifier == EntityDamageEvent.DamageModifier.BLOCKING) continue;
            if (!event.isApplicable(modifier)) continue;
            event.setDamage(modifier, 0);
        }
    }

    @EventHandler
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isBlackKnife(player.getInventory().getItemInMainHand())) return;

        if (BlackKnifeAbility.isMarked(event.getEntity())) {
            BlackKnifeAbility.consumeTrueDamage(event.getEntity());
            return;
        }

        if (player.getCooledAttackStrength(0.0F) < 1.0F) return;

        double base = WeaponType.BLACK_KNIFE.getMeleeDamage() + AttackUtil.strengthBonus(player);
        // Vanilla has already applied any critical-hit multiplier to this value,
        // so keep the larger of our base and the server-computed damage.
        event.setDamage(Math.max(base, event.getDamage()));
    }

    @EventHandler
    public void onEnchantTable(EnchantItemEvent event) {
        if (!isBlackKnife(event.getItem())) return;
        event.setCancelled(true);
        event.getEnchanter().sendMessage(Component.text("The Black Knife cannot be enchanted!", NamedTextColor.RED));
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        if (isBlackKnife(first) || isBlackKnife(second)) {
            event.setResult(ItemStack.empty());
        }
    }

    @EventHandler
    public void onEnchantCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().trim();
        if (!message.toLowerCase().startsWith("/enchant")) return;

        String[] parts = message.split("\\s+");
        Player target = event.getPlayer();
        if (parts.length > 1) {
            Player named = Bukkit.getPlayerExact(parts[1]);
            if (named != null) {
                target = named;
            }
        }

        if (isBlackKnife(target.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("The Black Knife cannot be enchanted!", NamedTextColor.RED));
        }
    }
}