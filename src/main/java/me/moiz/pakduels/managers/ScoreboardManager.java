package me.moiz.pakduels.managers;

import fr.mrmicky.fastboard.FastBoard;
import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private final PakDuelsPlugin plugin;
    private final Map<Player, FastBoard> boards;
    
    public ScoreboardManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.boards = new HashMap<>();
    }
    
    public void setupDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = new FastBoard(player1);
        FastBoard board2 = new FastBoard(player2);
        
        boards.put(player1, board1);
        boards.put(player2, board2);
        
        updateDuelScoreboard(duel);
    }
    
    public void updateDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = boards.get(player1);
        FastBoard board2 = boards.get(player2);
        
        if (board1 != null && board2 != null) {
            String title = plugin.getConfigManager().getScoreboardTitle();
            List<String> lines = plugin.getConfigManager().getScoreboardLines();
            
            // Process placeholders for player1's board
            List<String> processedLines1 = lines.stream()
                .map(line -> processPlaceholders(line, duel, player1))
                .collect(Collectors.toList());
            
            // Process placeholders for player2's board
            List<String> processedLines2 = lines.stream()
                .map(line -> processPlaceholders(line, duel, player2))
                .collect(Collectors.toList());
            
            board1.updateTitle(colorize(title));
            board1.updateLines(processedLines1.stream().map(this::colorize).collect(Collectors.toList()));
            
            board2.updateTitle(colorize(title));
            board2.updateLines(processedLines2.stream().map(this::colorize).collect(Collectors.toList()));
        }
    }
    
    private String processPlaceholders(String line, Duel duel, Player viewer) {
        Player opponent = duel.getOpponent(viewer);
        int viewerScore = viewer.equals(duel.getPlayer1()) ? duel.getPlayer1Score() : duel.getPlayer2Score();
        int opponentScore = viewer.equals(duel.getPlayer1()) ? duel.getPlayer2Score() : duel.getPlayer1Score();
        
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
            case INVENTORY_COUNTDOWN: return "Organizing";
            case IN_PROGRESS: return "Fighting";
            case ROUND_ENDING: return "Round End";
            case FINISHED: return "Finished";
            default: return "Unknown";
        }
    }
    
    private String colorize(String text) {
        return text.replace("&", "§");
    }
    
    public void removeDuelScoreboard(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        FastBoard board1 = boards.remove(player1);
        FastBoard board2 = boards.remove(player2);
        
        if (board1 != null) {
            board1.delete();
        }
        if (board2 != null) {
            board2.delete();
        }
    }
    
    public void cleanup() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
    }
}