package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import me.moiz.pakduels.utils.MessageUtils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private final PakDuelsPlugin plugin;
    private final Map<Player, FastBoard> playerBoards;
    
    public ScoreboardManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.playerBoards = new HashMap<>();
    }
    
    public void setupDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        // Create scoreboards for both players
        FastBoard board1 = new FastBoard(player1);
        FastBoard board2 = new FastBoard(player2);
        
        // Set title
        String title = MessageUtils.colorize(plugin.getConfigManager().getScoreboardTitle()).toString();
        board1.updateTitle(title);
        board2.updateTitle(title);
        
        // Store boards
        playerBoards.put(player1, board1);
        playerBoards.put(player2, board2);
        
        // Update initial content
        updateDuelScoreboard(duel);
    }
    
    public void updateDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = playerBoards.get(player1);
        FastBoard board2 = playerBoards.get(player2);
        
        if (board1 == null || board2 == null) return;
        
        // Get scoreboard lines from config
        List<String> lines = plugin.getConfigManager().getScoreboardLines();
        
        // Replace placeholders
        List<String> processedLines = lines.stream()
                .map(line -> replacePlaceholders(line, duel))
                .map(line -> MessageUtils.colorize(line).toString())
                .collect(Collectors.toList());
        
        // Update boards
        board1.updateLines(processedLines);
        board2.updateLines(processedLines);
    }
    
    public void removeDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = playerBoards.remove(player1);
        FastBoard board2 = playerBoards.remove(player2);
        
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
            case WAITING:
                return "Waiting";
            case STARTING:
                return "Starting";
            case INVENTORY_COUNTDOWN:
                return "Preparing";
            case IN_PROGRESS:
                return "Fighting";
            case ROUND_ENDING:
                return "Round End";
            case FINISHED:
                return "Finished";
            default:
                return "Unknown";
        }
    }
    
    public void cleanup() {
        for (FastBoard board : playerBoards.values()) {
            board.delete();
        }
        playerBoards.clear();
    }
}