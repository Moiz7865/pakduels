package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.guis.ArenaEditorGui;
import me.moiz.pakduels.guis.ArenaListGui;
import me.moiz.pakduels.models.Arena;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {
    private final PakDuelsPlugin plugin;
    private final Map<UUID, ArenaEditorGui> arenaEditorGuis;
    
    public GuiManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.arenaEditorGuis = new ConcurrentHashMap<>();
    }
    
    public void openArenaListGUI(Player player) {
        ArenaListGui gui = new ArenaListGui(plugin, player);
        gui.open();
    }
    
    public void openArenaEditorGUI(Player player, Arena arena) {
        ArenaEditorGui gui = new ArenaEditorGui(plugin, player, arena);
        arenaEditorGuis.put(player.getUniqueId(), gui);
        gui.open();
    }
    
    public ArenaEditorGui getArenaEditorGui(Player player) {
        return arenaEditorGuis.get(player.getUniqueId());
    }
    
    public void removeArenaEditorGui(Player player) {
        arenaEditorGuis.remove(player.getUniqueId());
    }
}