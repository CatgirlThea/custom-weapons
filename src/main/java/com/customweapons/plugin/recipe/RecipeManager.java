package com.customweapons.plugin.recipe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {

    public static final Component TITLE = Component.text("Assign Recipe");
    public static final int RESULT_SLOT = 25;
    public static final int SAVE_SLOT = 46;
    public static final int CLEAR_SLOT = 52;
    public static final int[] GRID_SLOTS = {
            19, 20, 21,
            28, 29, 30,
            37, 38, 39
    };

    private static final int[] BORDER_SLOTS = buildBorder();

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private int nextId;

    public RecipeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "recipes.yml");
        this.config = new YamlConfiguration();
        if (file.exists()) {
            try {
                config.load(file);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not load recipes.yml: " + e.getMessage());
            }
        }
        this.nextId = config.getInt("next_id", 1);
    }

    public void load() {
        ConfigurationSection section = config.getConfigurationSection("recipes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                ConfigurationSection recipe = section.getConfigurationSection(key);
                if (recipe == null) continue;

                List<String> shape = recipe.getStringList("shape");
                ItemStack result = recipe.getItemStack("result");
                if (result == null || shape.isEmpty()) continue;

                ShapedRecipe shaped = new ShapedRecipe(new NamespacedKey(plugin, key), result);
                shaped.shape(shape.toArray(new String[0]));

                ConfigurationSection ingredients = recipe.getConfigurationSection("ingredients");
                boolean valid = true;
                if (ingredients != null) {
                    for (String ch : ingredients.getKeys(false)) {
                        ItemStack ingredient = ingredients.getItemStack(ch);
                        if (ingredient == null || ch.isEmpty()) {
                            valid = false;
                            continue;
                        }
                        shaped.setIngredient(ch.charAt(0), new RecipeChoice.ExactChoice(ingredient));
                    }
                }
                if (!valid) continue;

                if (Bukkit.addRecipe(shaped)) {
                    plugin.getLogger().info("Registered custom recipe " + key);
                } else {
                    plugin.getLogger().warning("Could not register recipe " + key + " (duplicate?)");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load recipe " + key + ": " + e.getMessage());
            }
        }
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        for (int slot : BORDER_SLOTS) {
            inv.setItem(slot, borderItem());
        }
        inv.setItem(SAVE_SLOT, saveButton());
        inv.setItem(CLEAR_SLOT, clearButton());
        player.openInventory(inv);
    }

    public void onSaveClicked(Player player, Inventory inv) {
        ItemStack result = inv.getItem(RESULT_SLOT);
        if (result == null || result.getType().isAir()) {
            player.sendMessage(Component.text("Place the item you want to craft in the result slot first.", NamedTextColor.RED));
            return;
        }

        char[][] grid = new char[3][3];
        Map<Character, ItemStack> ingredients = new HashMap<>();
        char nextChar = 'A';
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ItemStack item = inv.getItem(GRID_SLOTS[row * 3 + col]);
                if (item == null || item.getType().isAir()) {
                    grid[row][col] = ' ';
                    continue;
                }
                char existing = findIngredientChar(ingredients, item);
                if (existing == 0) {
                    existing = nextChar++;
                    ingredients.put(existing, item.clone());
                }
                grid[row][col] = existing;
            }
        }
        if (ingredients.isEmpty()) {
            player.sendMessage(Component.text("Place at least one ingredient in the crafting grid.", NamedTextColor.RED));
            return;
        }
        if (nextChar > 'Z') {
            player.sendMessage(Component.text("Too many distinct ingredients (max 26).", NamedTextColor.RED));
            return;
        }

        int minRow = 3, maxRow = -1, minCol = 3, maxCol = -1;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (grid[r][c] == ' ') continue;
                minRow = Math.min(minRow, r);
                maxRow = Math.max(maxRow, r);
                minCol = Math.min(minCol, c);
                maxCol = Math.max(maxCol, c);
            }
        }

        String[] shape = new String[maxRow - minRow + 1];
        for (int r = minRow; r <= maxRow; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = minCol; c <= maxCol; c++) {
                sb.append(grid[r][c]);
            }
            shape[r - minRow] = sb.toString();
        }

        String name = "recipe_" + nextId;
        ShapedRecipe shaped = new ShapedRecipe(new NamespacedKey(plugin, name), result.clone());
        shaped.shape(shape);
        for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            shaped.setIngredient(entry.getKey(), new RecipeChoice.ExactChoice(entry.getValue()));
        }

        if (!Bukkit.addRecipe(shaped)) {
            player.sendMessage(Component.text("Failed to register the recipe (key already exists?).", NamedTextColor.RED));
            return;
        }

        nextId++;
        persistRecipe(name, shape, ingredients, result.clone());
        saveFile();

        for (int slot : GRID_SLOTS) inv.setItem(slot, null);
        inv.setItem(RESULT_SLOT, null);

        Component resultName = result.getItemMeta() != null && result.getItemMeta().hasDisplayName()
                ? result.getItemMeta().displayName()
                : Component.text(result.getType().name().toLowerCase().replace('_', ' '));
        player.sendMessage(Component.text("Recipe saved ", NamedTextColor.GREEN)
                .append(Component.text("(" + name + ")", NamedTextColor.GOLD))
                .append(Component.text("! Craft the shape to get ", NamedTextColor.GREEN))
                .append(resultName));
    }

    public void onClearClicked(Inventory inv) {
        for (int slot : GRID_SLOTS) inv.setItem(slot, null);
        inv.setItem(RESULT_SLOT, null);
    }

    public void returnItems(Player player, Inventory inv) {
        for (int slot : GRID_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                inv.setItem(slot, null);
                giveOrDrop(player, item);
            }
        }
        ItemStack result = inv.getItem(RESULT_SLOT);
        if (result != null && !result.getType().isAir()) {
            inv.setItem(RESULT_SLOT, null);
            giveOrDrop(player, result);
        }
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    private void persistRecipe(String name, String[] shape, Map<Character, ItemStack> ingredients, ItemStack result) {
        String path = "recipes." + name;
        config.set(path + ".shape", List.of(shape));
        config.set(path + ".result", result);
        for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            config.set(path + ".ingredients." + entry.getKey(), entry.getValue());
        }
        config.set("next_id", nextId);
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save recipes.yml: " + e.getMessage());
        }
    }

    private static char findIngredientChar(Map<Character, ItemStack> ingredients, ItemStack item) {
        for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            if (entry.getValue().isSimilar(item)) return entry.getKey();
        }
        return 0;
    }

    private static ItemStack borderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack saveButton() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Save Recipe", NamedTextColor.GREEN));
        meta.lore(List.of(
                Component.text("Click to register this pattern", NamedTextColor.GRAY),
                Component.text("as a shaped recipe", NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack clearButton() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Clear Grid", NamedTextColor.RED));
        meta.lore(List.of(
                Component.text("Click to empty the grid", NamedTextColor.GRAY),
                Component.text("and result slot", NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static int[] buildBorder() {
        List<Integer> special = new ArrayList<>();
        for (int s : GRID_SLOTS) special.add(s);
        special.add(RESULT_SLOT);
        special.add(SAVE_SLOT);
        special.add(CLEAR_SLOT);
        List<Integer> border = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (!special.contains(i)) border.add(i);
        }
        return border.stream().mapToInt(Integer::intValue).toArray();
    }
}