package me.moiz.pakduels.managers;

import fr.mrmicky.fastboard.FastBoard;
import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {
    private final PakDuelsPlugin plugin;
    private final Map<Player, FastBoard> boards;
    
    public ScoreboardManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.boards = new ConcurrentHashMap<>();
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
        
        if (board1 != null) {
            updateBoardForPlayer(board1, duel, player1);
        }
        if (board2 != null) {
            updateBoardForPlayer(board2, duel, player2);
        }
    }
    
    private void updateBoardForPlayer(FastBoard board, Duel duel, Player viewer) {
        Player opponent = duel.getOpponent(viewer);
        
        String title = "§6§l⚔ DUEL ⚔";
        board.updateTitle(title);
        
        String stateColor = getStateColor(duel.getState());
        String viewerScore = String.valueOf(viewer.equals(duel.getPlayer1()) ? duel.getPlayer1Score() : duel.getPlayer2Score());
        String opponentScore = String.valueOf(opponent.equals(duel.getPlayer1()) ? duel.getPlayer1Score() : duel.getPlayer2Score());
        
        board.updateLines(Arrays.asList(
            "§7━━━━━━━━━━━━━━━━━━━━",
            "§f" + viewer.getName() + " §7vs §f" + opponent.getName(),
            "",
            "§a§lYour Score: §f" + viewerScore,
            "§c§lOpponent Score: §f" + opponentScore,
            "",
            "§b§lKit: §f" + duel.getKit().getName(),
            "§e§lRound: §f" + duel.getCurrentRound() + "§7/§f" + duel.getMaxRounds(),
            "",
            "§7Status: " + stateColor + getStateDisplay(duel.getState()),
            "§7Arena: §f" + duel.getArena().getName(),
            "§7━━━━━━━━━━━━━━━━━━━━"
        ));
    }
    
    private String getStateColor(Duel.DuelState state) {
        return switch (state) {
            case WAITING -> "§7";
            case STARTING -> "§e";
            case IN_PROGRESS -> "§a";
            case ROUND_ENDING -> "§6";
            case FINISHED -> "§c";
        };
    }
    
    private String getStateDisplay(Duel.DuelState state) {
        return switch (state) {
            case WAITING -> "Waiting";
            case STARTING -> "Starting";
            case IN_PROGRESS -> "Fighting";
            case ROUND_ENDING -> "Round Ending";
            case FINISHED -> "Finished";
        };
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
        boards.values().forEach(FastBoard::delete);
        boards.clear();
    }
}