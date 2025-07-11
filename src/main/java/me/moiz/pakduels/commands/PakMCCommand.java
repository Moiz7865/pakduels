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
            MessageUtils.sendMessage(player, "&cUsage: /pakmc <subcommand>");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreateCommand(player, args);
            case "arena":
                return handleArenaCommand(player, args);
            default:
                MessageUtils.sendMessage(player, "&cUnknown subcommand: " + subCommand);
                return true;
        }
    }
    
    private boolean handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("pakmc.kit.create")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to create kits!");
            return true;
        }
        
        if (args.length != 2) {
            MessageUtils.sendMessage(player, "&cUsage: /pakmc create <kitname>");
            return true;
        }
        
        String kitName = args[1];
        
        // Check if kit already exists
        if (plugin.getKitManager().getKit(kitName) != null) {
            MessageUtils.sendMessage(player, "&cA kit with that name already exists!");
            return true;
        }
        
        // Create kit from player's inventory
        Kit kit = new Kit(kitName, 
                         player.getInventory().getContents().clone(),
                         player.getInventory().getArmorContents().clone(),
                         player.getInventory().getItemInOffHand().clone());
        
        plugin.getKitManager().addKit(kit);
        MessageUtils.sendMessage(player, "&aKit &f" + kitName + " &acreated successfully!");
        
        return true;
    }
    
    private boolean handleArenaCommand(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendMessage(player, "&cUsage: /pakmc arena <create|editor>");
            return true;
        }
        
        String arenaSubCommand = args[1].toLowerCase();
        
        switch (arenaSubCommand) {
            case "create":
                return handleArenaCreateCommand(player, args);
            case "editor":
                return handleArenaEditorCommand(player, args);
            default:
                MessageUtils.sendMessage(player, "&cUnknown arena subcommand: " + arenaSubCommand);
                return true;
        }
    }
    
    private boolean handleArenaCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("pakmc.arena.create")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to create arenas!");
            return true;
        }
        
        if (args.length != 3) {
            MessageUtils.sendMessage(player, "&cUsage: /pakmc arena create <name>");
            return true;
        }
        
        String arenaName = args[2];
        
        // Check if arena already exists
        if (plugin.getArenaManager().getArena(arenaName) != null) {
            MessageUtils.sendMessage(player, "&cAn arena with that name already exists!");
            return true;
        }
        
        // Create arena
        Arena arena = new Arena(arenaName);
        plugin.getArenaManager().addArena(arena);
        MessageUtils.sendMessage(player, "&aArena &f" + arenaName + " &acreated successfully!");
        MessageUtils.sendMessage(player, "&aUse &f/pakmc arena editor &ato configure it.");
        
        return true;
    }
    
    private boolean handleArenaEditorCommand(Player player, String[] args) {
        if (!player.hasPermission("pakmc.arena.edit")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to edit arenas!");
            return true;
        }
        
        // Open arena list GUI
        plugin.getGuiManager().openArenaListGUI(player);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("create", "arena").stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arena")) {
                String partial = args[1].toLowerCase();
                return Arrays.asList("create", "editor").stream()
                        .filter(cmd -> cmd.startsWith(partial))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}