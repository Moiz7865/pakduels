package me.moiz.pakduels.managers;

import fr.mrmicky.fastboard.FastBoard;
import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private final PakDuelsPlugin plugin;
    private final Map<Player, FastBoard> duelBoards;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public ScoreboardManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.duelBoards = new HashMap<>();
    }
    
    public void setupDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        // Create scoreboards for both players
        FastBoard board1 = new FastBoard(player1);
        FastBoard board2 = new FastBoard(player2);
        
        duelBoards.put(player1, board1);
        duelBoards.put(player2, board2);
        
        updateDuelScoreboard(duel);
    }
    
    public void updateDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = duelBoards.get(player1);
        FastBoard board2 = duelBoards.get(player2);
        
        if (board1 == null || board2 == null) return;
        
        // Get title and lines from config
        String title = translateHexColorCodes(plugin.getConfigManager().getScoreboardTitle());
        List<String> lines = plugin.getConfigManager().getScoreboardLines().stream()
                .map(line -> translateHexColorCodes(replacePlaceholders(line, duel)))
                .collect(Collectors.toList());
        
        // Update both scoreboards
        board1.updateTitle(title);
        board1.updateLines(lines);
        
        board2.updateTitle(title);
        board2.updateLines(lines);
    }
    
    public void removeDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = duelBoards.remove(player1);
        FastBoard board2 = duelBoards.remove(player2);
        
        if (board1 != null) {
            board1.delete();
        }
        if (board2 != null) {
            board2.delete();
        }
    }
    
    private String replacePlaceholders(String line, Duel duel) {
        return line
                .replace("{player1}", duel.getPlayer1().getName())
                .replace("{player2}", duel.getPlayer2().getName())
                .replace("{score1}", String.valueOf(duel.getPlayer1Score()))
                .replace("{score2}", String.valueOf(duel.getPlayer2Score()))
                .replace("{kit}", duel.getKit().getName())
                .replace("{round}", String.valueOf(duel.getCurrentRound()))
                .replace("{maxrounds}", String.valueOf(duel.getMaxRounds()))
                .replace("{state}", getStateDisplay(duel.getState()));
    }
    
    private String getStateDisplay(Duel.DuelState state) {
        switch (state) {
            case WAITING: return "Waiting";
            case STARTING: return "Starting";
            case INVENTORY_COUNTDOWN: return "Preparing";
            case IN_PROGRESS: return "Fighting";
            case ROUND_ENDING: return "Round End";
            case FINISHED: return "Finished";
            default: return "Unknown";
        }
    }
    
    private String translateHexColorCodes(String message) {
        if (message == null) return "";
        
        // Convert hex colors (&#RRGGBB) to Minecraft format
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = "§x§" + hexCode.charAt(0) + "§" + hexCode.charAt(1) + 
                               "§" + hexCode.charAt(2) + "§" + hexCode.charAt(3) + 
                               "§" + hexCode.charAt(4) + "§" + hexCode.charAt(5);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        
        // Convert legacy color codes
        return buffer.toString().replace('&', '§');
    }
}