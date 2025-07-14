package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.guis.ArenaEditorGui;
import me.moiz.pakduels.guis.ArenaListGui;
import me.moiz.pakduels.guis.KitEditorGui;
import me.moiz.pakduels.guis.KitSelectorGui;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.models.Kit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GuiManager {
    private final PakDuelsPlugin plugin;
    private final Map<Player, ArenaEditorGui> arenaEditorGuis;
    private final Map<Player, KitEditorGui> kitEditorGuis;
    private final Map<Player, KitSelectorGui> kitSelectorGuis;
    
    public GuiManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.arenaEditorGuis = new HashMap<>();
        this.kitEditorGuis = new HashMap<>();
        this.kitSelectorGuis = new HashMap<>();
    }
    
    public void openArenaListGUI(Player player) {
        ArenaListGui gui = new ArenaListGui(plugin, player);
        gui.open();
    }
    
    public void openArenaEditorGUI(Player player, Arena arena) {
        ArenaEditorGui gui = new ArenaEditorGui(plugin, player, arena);
        arenaEditorGuis.put(player, gui);
        gui.open();
    }
    
    public void openKitEditorGUI(Player player, Kit kit) {
        KitEditorGui gui = new KitEditorGui(plugin, player, kit);
        kitEditorGuis.put(player, gui);
        gui.open();
    }
    
    public void openKitSelectorGUI(Player player, Arena arena) {
        KitSelectorGui gui = new KitSelectorGui(plugin, player, arena);
        kitSelectorGuis.put(player, gui);
        gui.open();
    }
    
    public ArenaEditorGui getArenaEditorGui(Player player) {
        return arenaEditorGuis.get(player);
    }
    
    public KitEditorGui getKitEditorGui(Player player) {
        return kitEditorGuis.get(player);
    }
    
    public KitSelectorGui getKitSelectorGui(Player player) {
        return kitSelectorGuis.get(player);
    }
    
    public void removeArenaEditorGui(Player player) {
        arenaEditorGuis.remove(player);
    }
    
    public void removeKitEditorGui(Player player) {
        kitEditorGuis.remove(player);
    }
    
    public void removeKitSelectorGui(Player player) {
        kitSelectorGuis.remove(player);
    }
    
    public void cleanup() {
        arenaEditorGuis.clear();
        kitEditorGuis.clear();
        kitSelectorGuis.clear();
    }
}