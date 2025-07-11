package me.moiz.pakduels.guis;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KitEditorGui {
    private final PakDuelsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private final Inventory inventory;
    
    public KitEditorGui(PakDuelsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
        this.inventory = Bukkit.createInventory(null, 54, "Kit Editor: " + kit.getName());
        
        setupGui();
    }
    
    private void setupGui() {
        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.setDisplayName(" ");
        background.setItemMeta(backgroundMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, background);
        }
        
        // Natural Health Regen
        ItemStack healthRegen = new ItemStack(kit.getRule("natural-health-regen") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta healthRegenMeta = healthRegen.getItemMeta();
        healthRegenMeta.setDisplayName("§c§lNatural Health Regeneration");
        healthRegenMeta.setLore(Arrays.asList(
            "§7Allow natural health regeneration",
            "§7Status: " + (kit.getRule("natural-health-regen") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        healthRegen.setItemMeta(healthRegenMeta);
        inventory.setItem(19, healthRegen);
        
        // Block Break
        ItemStack blockBreak = new ItemStack(kit.getRule("block-break") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta blockBreakMeta = blockBreak.getItemMeta();
        blockBreakMeta.setDisplayName("§6§lBlock Breaking");
        blockBreakMeta.setLore(Arrays.asList(
            "§7Allow players to break blocks",
            "§7Status: " + (kit.getRule("block-break") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        blockBreak.setItemMeta(blockBreakMeta);
        inventory.setItem(21, blockBreak);
        
        // Block Place
        ItemStack blockPlace = new ItemStack(kit.getRule("block-place") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta blockPlaceMeta = blockPlace.getItemMeta();
        blockPlaceMeta.setDisplayName("§e§lBlock Placing");
        blockPlaceMeta.setLore(Arrays.asList(
            "§7Allow players to place blocks",
            "§7Status: " + (kit.getRule("block-place") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        blockPlace.setItemMeta(blockPlaceMeta);
        inventory.setItem(23, blockPlace);
        
        // Health Indicators
        ItemStack healthIndicators = new ItemStack(kit.getRule("health-indicators") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta healthIndicatorsMeta = healthIndicators.getItemMeta();
        healthIndicatorsMeta.setDisplayName("§d§lHealth Indicators");
        healthIndicatorsMeta.setLore(Arrays.asList(
            "§7Show health indicators above players",
            "§7Status: " + (kit.getRule("health-indicators") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        healthIndicators.setItemMeta(healthIndicatorsMeta);
        inventory.setItem(25, healthIndicators);
        
        // Hunger Loss
        ItemStack hungerLoss = new ItemStack(kit.getRule("hunger-loss") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta hungerLossMeta = hungerLoss.getItemMeta();
        hungerLossMeta.setDisplayName("§9§lHunger Loss");
        hungerLossMeta.setLore(Arrays.asList(
            "§7Allow hunger to decrease",
            "§7Status: " + (kit.getRule("hunger-loss") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        hungerLoss.setItemMeta(hungerLossMeta);
        inventory.setItem(28, hungerLoss);
        
        // Item Drop
        ItemStack itemDrop = new ItemStack(kit.getRule("item-drop") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta itemDropMeta = itemDrop.getItemMeta();
        itemDropMeta.setDisplayName("§5§lItem Dropping");
        itemDropMeta.setLore(Arrays.asList(
            "§7Allow players to drop items",
            "§7Status: " + (kit.getRule("item-drop") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        itemDrop.setItemMeta(itemDropMeta);
        inventory.setItem(30, itemDrop);
        
        // Item Pickup
        ItemStack itemPickup = new ItemStack(kit.getRule("item-pickup") ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta itemPickupMeta = itemPickup.getItemMeta();
        itemPickupMeta.setDisplayName("§3§lItem Pickup");
        itemPickupMeta.setLore(Arrays.asList(
            "§7Allow players to pick up items",
            "§7Status: " + (kit.getRule("item-pickup") ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        itemPickup.setItemMeta(itemPickupMeta);
        inventory.setItem(32, itemPickup);
        
        // Save button
        ItemStack save = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName("§a§lSave Kit");
        saveMeta.setLore(Arrays.asList(
            "§7Save all changes to this kit",
            "",
            "§e§lClick to save!"
        ));
        save.setItemMeta(saveMeta);
        inventory.setItem(45, save);
        
        // Delete button
        ItemStack delete = new ItemStack(Material.RED_CONCRETE);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName("§c§lDelete Kit");
        deleteMeta.setLore(Arrays.asList(
            "§7Delete this kit permanently",
            "§c§lWARNING: This cannot be undone!",
            "",
            "§e§lClick to delete!"
        ));
        delete.setItemMeta(deleteMeta);
        inventory.setItem(46, delete);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§7§lBack");
        backMeta.setLore(Arrays.asList(
            "§7Return to kit list",
            "",
            "§e§lClick to go back!"
        ));
        back.setItemMeta(backMeta);
        inventory.setItem(53, back);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void refresh() {
        setupGui();
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public void handleClick(int slot) {
        switch (slot) {
            case 19: // Natural Health Regen
                kit.setRule("natural-health-regen", !kit.getRule("natural-health-regen"));
                MessageUtils.sendMessage(player, "&aHealth regeneration " + (kit.getRule("natural-health-regen") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 21: // Block Break
                kit.setRule("block-break", !kit.getRule("block-break"));
                MessageUtils.sendMessage(player, "&aBlock breaking " + (kit.getRule("block-break") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 23: // Block Place
                kit.setRule("block-place", !kit.getRule("block-place"));
                MessageUtils.sendMessage(player, "&aBlock placing " + (kit.getRule("block-place") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 25: // Health Indicators
                kit.setRule("health-indicators", !kit.getRule("health-indicators"));
                MessageUtils.sendMessage(player, "&aHealth indicators " + (kit.getRule("health-indicators") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 28: // Hunger Loss
                kit.setRule("hunger-loss", !kit.getRule("hunger-loss"));
                MessageUtils.sendMessage(player, "&aHunger loss " + (kit.getRule("hunger-loss") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 30: // Item Drop
                kit.setRule("item-drop", !kit.getRule("item-drop"));
                MessageUtils.sendMessage(player, "&aItem dropping " + (kit.getRule("item-drop") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 32: // Item Pickup
                kit.setRule("item-pickup", !kit.getRule("item-pickup"));
                MessageUtils.sendMessage(player, "&aItem pickup " + (kit.getRule("item-pickup") ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 45: // Save
                plugin.getKitManager().saveKit(kit);
                MessageUtils.sendMessage(player, "&aKit saved successfully!");
                break;
            case 46: // Delete
                plugin.getKitManager().removeKit(kit.getName());
                plugin.getGuiManager().removeKitEditorGui(player);
                MessageUtils.sendMessage(player, "&cKit deleted successfully!");
                player.closeInventory();
                break;
            case 53: // Back
                player.closeInventory();
                break;
        }
    }
}