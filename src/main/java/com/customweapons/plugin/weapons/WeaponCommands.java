package com.customweapons.plugin.weapons;

import com.customweapons.plugin.WeaponsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponCommands implements CommandExecutor, TabCompleter {

    private final WeaponsPlugin plugin;
    private final WeaponItemManager weaponManager;

    public WeaponCommands(WeaponsPlugin plugin, WeaponItemManager weaponManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("weapongive")) return false;

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /weapongive <player> <weapon>", NamedTextColor.RED));
            sender.sendMessage(Component.text("Weapons: " +
                    Arrays.stream(WeaponType.values()).map(WeaponType::getDisplayName).collect(Collectors.joining(", ")), NamedTextColor.GRAY));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        WeaponType weaponType = WeaponType.fromString(args[1]);
        if (weaponType == null) {
            sender.sendMessage(Component.text("Invalid weapon! Valid: " +
                    Arrays.stream(WeaponType.values()).map(WeaponType::name).collect(Collectors.joining(", ")), NamedTextColor.RED));
            return true;
        }

        ItemStack weapon = weaponManager.createWeapon(weaponType);
        target.getInventory().addItem(weapon);

        sender.sendMessage(Component.text("Gave " + weaponType.getDisplayName() + " to " + target.getName(), NamedTextColor.GREEN));
        target.sendMessage(weaponType.getDisplayNameComponent()
                .append(Component.text(" has been given to you!", NamedTextColor.GRAY)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.stream(WeaponType.values())
                    .map(WeaponType::getDisplayName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}