package com.customweapons.plugin.recipe;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class RecipeGuiListener implements Listener {

    private final RecipeManager manager;

    public RecipeGuiListener(RecipeManager manager) {
        this.manager = manager;
    }

    private boolean isOurView(InventoryView view) {
        return view.getTitle().equals(RecipeManager.TITLE);
    }

    private static boolean isEditableTopSlot(int raw) {
        if (raw == RecipeManager.RESULT_SLOT) return true;
        for (int s : RecipeManager.GRID_SLOTS) {
            if (s == raw) return true;
        }
        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isOurView(event.getView())) return;

        Inventory top = event.getView().getTopInventory();

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR
                || event.getAction() == InventoryAction.CLONE_STACK) {
            event.setCancelled(true);
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null || !clicked.equals(top)) return;

        int raw = event.getRawSlot();
        if (raw == RecipeManager.SAVE_SLOT) {
            event.setCancelled(true);
            manager.onSaveClicked(player, top);
        } else if (raw == RecipeManager.CLEAR_SLOT) {
            event.setCancelled(true);
            manager.onClearClicked(top);
        } else if (!isEditableTopSlot(raw)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isOurView(event.getView())) return;
        int topSize = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize && !isEditableTopSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!isOurView(event.getView())) return;
        manager.returnItems(player, event.getInventory());
    }
}