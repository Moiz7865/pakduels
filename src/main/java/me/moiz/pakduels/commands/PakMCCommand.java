package me.moiz.pakduels.commands;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PakMCCommand implements CommandExecutor, TabCompleter {
    private final PakDuelsPlugin plugin;
    
    public PakMCCommand(PakDuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length != 2) {
                    MessageUtils.sendMessage(player, "&cUsage: /pakmc create <kitname>");
                    return true;
                }
                createKit(player, args[1]);
                break;
                
            case "setspawn":
                if (!player.hasPermission("pakmc.admin")) {
                    MessageUtils.sendMessage(player, "&cYou don't have permission to use this command!");
                    return true;
                }
                setSpawn(player);
                break;
                
            case "reload":
                if (!player.hasPermission("pakmc.admin")) {
                    MessageUtils.sendMessage(player, "no-permission");
                    return true;
                }
                reloadConfigs();
                MessageUtils.sendMessage(player, "configs-reloaded");
                break;
                
            case "arena":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "&cUsage: /pakmc arena <create|editor> [name]");
                    return true;
                }
                handleArenaCommand(player, args);
                break;
                
            case "kit":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "&cUsage: /pakmc kit <editor> <kitname>");
                    return true;
                }
                handleKitCommand(player, args);
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void createKit(Player player, String kitName) {
        if (!player.hasPermission("pakmc.kit.create")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to create kits!");
            return;
        }
        
        if (plugin.getKitManager().hasKit(kitName)) {
            MessageUtils.sendMessage(player, "&cA kit with that name already exists!");
            return;
        }
        
        Kit kit = new Kit(kitName, 
                player.getInventory().getContents().clone(),
                player.getInventory().getArmorContents().clone(),
                player.getInventory().getItemInOffHand().clone());
        
        plugin.getKitManager().addKit(kit);
        MessageUtils.sendMessage(player, "&aKit &f" + kitName + " &acreated successfully!");
    }
    
    private void handleArenaCommand(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "create":
                if (args.length != 3) {
                    MessageUtils.sendMessage(player, "&cUsage: /pakmc arena create <name>");
                    return;
                }
                createArena(player, args[2]);
                break;
                
            case "editor":
                if (!player.hasPermission("pakmc.arena.edit")) {
                    MessageUtils.sendMessage(player, "&cYou don't have permission to edit arenas!");
                    return;
                }
                plugin.getGuiManager().openArenaListGUI(player);
                break;
                
            default:
                MessageUtils.sendMessage(player, "&cUsage: /pakmc arena <create|editor> [name]");
                break;
        }
    }
    
    private void handleKitCommand(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "editor":
                if (args.length != 3) {
                    MessageUtils.sendMessage(player, "&cUsage: /pakmc kit editor <kitname>");
                    return;
                }
                openKitEditor(player, args[2]);
                break;
                
            default:
                MessageUtils.sendMessage(player, "&cUsage: /pakmc kit <editor> <kitname>");
                break;
        }
    }
    
    private void createArena(Player player, String arenaName) {
        if (!player.hasPermission("pakmc.arena.create")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to create arenas!");
            return;
        }
        
        if (plugin.getArenaManager().hasArena(arenaName)) {
            MessageUtils.sendMessage(player, "&cAn arena with that name already exists!");
            return;
        }
        
        Arena arena = new Arena(arenaName);
        plugin.getArenaManager().addArena(arena);
        MessageUtils.sendMessage(player, "&aArena &f" + arenaName + " &acreated successfully!");
        MessageUtils.sendMessage(player, "&eUse &f/pakmc arena editor &eto configure it.");
    }
    
    private void openKitEditor(Player player, String kitName) {
        if (!player.hasPermission("pakmc.kit.create")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to edit kits!");
            return;
        }
        
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "&cKit not found!");
            return;
        }
        
        plugin.getGuiManager().openKitEditorGUI(player, kit);
    }
    
    private void setSpawn(Player player) {
        plugin.getConfigManager().setSpawn(
            player.getWorld().getName(),
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ(),
            player.getLocation().getYaw(),
            player.getLocation().getPitch()
        );
        MessageUtils.sendMessage(player, "&aSpawn location set successfully!");
    }
    
    private void reloadConfigs() {
        plugin.getConfigManager().reloadConfig();
        plugin.getKitManager().loadKits();
        plugin.getArenaManager().loadArenas();
    }
    
    private void sendHelp(Player player) {
        MessageUtils.sendMessage(player, "&6&l=== PakDuels Commands ===");
        MessageUtils.sendMessage(player, "&e/pakmc create <kitname> &7- Create a kit from your inventory");
        MessageUtils.sendMessage(player, "&e/pakmc arena create <name> &7- Create a new arena");
        MessageUtils.sendMessage(player, "&e/pakmc arena editor &7- Open arena management GUI");
        MessageUtils.sendMessage(player, "&e/pakmc kit editor <kitname> &7- Edit kit rules");
        MessageUtils.sendMessage(player, "&e/pakmc setspawn &7- Set lobby spawn location");
        MessageUtils.sendMessage(player, "&e/pakmc reload &7- Reload configuration");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            return Arrays.asList("create", "arena", "kit", "setspawn", "reload").stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arena")) {
                return Arrays.asList("create", "editor").stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("kit")) {
                return Arrays.asList("editor").stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("editor")) {
                return plugin.getKitManager().getKitNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}