package com.customweapons.plugin;

import com.customweapons.plugin.cooldown.CooldownManager;
import com.customweapons.plugin.recipe.RecipeCommand;
import com.customweapons.plugin.recipe.RecipeGuiListener;
import com.customweapons.plugin.recipe.RecipeManager;
import com.customweapons.plugin.weapons.BlackKnifeEffect;
import com.customweapons.plugin.weapons.BlackKnifeListener;
import com.customweapons.plugin.weapons.SiphonTridentListener;
import com.customweapons.plugin.weapons.StunChainEffect;
import com.customweapons.plugin.weapons.StunManager;
import com.customweapons.plugin.weapons.WeaponCommands;
import com.customweapons.plugin.weapons.WeaponItemManager;
import com.customweapons.plugin.weapons.WeaponListener;
import com.customweapons.plugin.weapons.WeaponType;
import org.bukkit.plugin.java.JavaPlugin;

public class WeaponsPlugin extends JavaPlugin {

    private static WeaponsPlugin instance;

    private WeaponItemManager weaponItemManager;
    private CooldownManager cooldownManager;
    private StunManager stunManager;
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        instance = this;

        weaponItemManager = new WeaponItemManager(this);
        cooldownManager = new CooldownManager();
        stunManager = new StunManager();

        getServer().getPluginManager().registerEvents(new WeaponListener(this, weaponItemManager, stunManager), this);
        getServer().getPluginManager().registerEvents(new BlackKnifeListener(this, weaponItemManager), this);
        getServer().getPluginManager().registerEvents(new SiphonTridentListener(this, weaponItemManager), this);

        StunChainEffect.start(this);
        BlackKnifeEffect.start(this, weaponItemManager);

        WeaponCommands weaponCommands = new WeaponCommands(this, weaponItemManager);
        getCommand("weapongive").setExecutor(weaponCommands);
        getCommand("weapongive").setTabCompleter(weaponCommands);

        recipeManager = new RecipeManager(this);
        recipeManager.load();
        getServer().getPluginManager().registerEvents(new RecipeGuiListener(recipeManager), this);
        getCommand("recipegui").setExecutor(new RecipeCommand(this, recipeManager));

        getLogger().info("CustomWeapons enabled! " + WeaponType.values().length + " weapons loaded.");
    }

    @Override
    public void onDisable() {
        if (stunManager != null) stunManager.clear();
        StunChainEffect.stopAll();
        if (cooldownManager != null) cooldownManager.clearAllCooldowns();
    }

    public static WeaponsPlugin getInstance() { return instance; }
    public WeaponItemManager getWeaponItemManager() { return weaponItemManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public StunManager getStunManager() { return stunManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
}