package me.moiz.pakduels.commands;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.DuelRequest;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {
    private final PakDuelsPlugin plugin;
    
    public DuelCommand(PakDuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }
        
        if (args.length != 3) {
            MessageUtils.sendMessage(player, "&cUsage: /duel <player> <kit> <rounds>");
            return true;
        }
        
        // Check if player is already in a duel
        if (plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou are already in a duel!");
            return true;
        }
        
        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtils.sendMessage(player, "&cPlayer not found!");
            return true;
        }
        
        if (target.equals(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot duel yourself!");
            return true;
        }
        
        // Check if target is already in a duel
        if (plugin.getDuelManager().isInDuel(target)) {
            MessageUtils.sendMessage(player, "&cThat player is already in a duel!");
            return true;
        }
        
        // Get kit
        Kit kit = plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            MessageUtils.sendMessage(player, "&cKit not found!");
            return true;
        }
        
        // Parse rounds
        int rounds;
        try {
            rounds = Integer.parseInt(args[2]);
            if (rounds <= 0 || rounds > 10) {
                MessageUtils.sendMessage(player, "&cRounds must be between 1 and 10!");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "&cInvalid number of rounds!");
            return true;
        }
        
        // Create duel request
        DuelRequest request = new DuelRequest(player, target, kit, rounds);
        plugin.getDuelManager().addDuelRequest(request);
        
        MessageUtils.sendMessage(player, "&aDuel request sent to &f" + target.getName() + "&a!");
        MessageUtils.sendMessage(target, "&6" + player.getName() + " &ahas challenged you to a duel!");
        MessageUtils.sendMessage(target, "&aKit: &f" + kit.getName() + " &a| Rounds: &f" + rounds);
        MessageUtils.sendMessage(target, "&aType &f/duel accept &aor &f/duel deny &ato respond.");
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete online players
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Tab complete kit names
            String partial = args[1].toLowerCase();
            return plugin.getKitManager().getKitNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Tab complete round numbers
            String partial = args[2].toLowerCase();
            return Arrays.asList("1", "3", "5").stream()
                    .filter(rounds -> rounds.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}