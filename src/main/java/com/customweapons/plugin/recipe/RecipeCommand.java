package com.customweapons.plugin.recipe;

import com.customweapons.plugin.WeaponsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecipeCommand implements CommandExecutor {

    private final WeaponsPlugin plugin;
    private final RecipeManager manager;

    public RecipeCommand(WeaponsPlugin plugin, RecipeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(Component.text("Usage: /" + label + " [player]", NamedTextColor.RED));
            return true;
        }

        Player target;
        if (args.length == 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(Component.text("You must specify a player to open the GUI for.", NamedTextColor.RED));
            return true;
        }

        manager.open(target);
        sender.sendMessage(Component.text("Opened the recipe assignment GUI for " + target.getName(), NamedTextColor.GREEN));
        return true;
    }
}