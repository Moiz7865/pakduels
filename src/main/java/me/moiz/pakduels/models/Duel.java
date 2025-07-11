package me.moiz.pakduels.models;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class Duel {
    private final UUID id;
    private final Player player1;
    private final Player player2;
    private final Kit kit;
    private final Arena arena;
    private final int maxRounds;
    private int player1Score;
    private int player2Score;
    private int currentRound;
    private DuelState state;
    private long startTime;
    
    public enum DuelState {
        WAITING,
        STARTING,
        IN_PROGRESS,
        ROUND_ENDING,
        FINISHED
    }
    
    public Duel(Player player1, Player player2, Kit kit, Arena arena, int maxRounds) {
        this.id = UUID.randomUUID();
        this.player1 = player1;
        this.player2 = player2;
        this.kit = kit;
        this.arena = arena;
        this.maxRounds = maxRounds;
        this.player1Score = 0;
        this.player2Score = 0;
        this.currentRound = 1;
        this.state = DuelState.WAITING;
        this.startTime = System.currentTimeMillis();
    }
    
    public UUID getId() {
        return id;
    }
    
    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public int getMaxRounds() {
        return maxRounds;
    }
    
    public int getPlayer1Score() {
        return player1Score;
    }
    
    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }
    
    public int getPlayer2Score() {
        return player2Score;
    }
    
    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }
    
    public DuelState getState() {
        return state;
    }
    
    public void setState(DuelState state) {
        this.state = state;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public Player getOpponent(Player player) {
        return player.equals(player1) ? player2 : player1;
    }
    
    public boolean isPlayerInDuel(Player player) {
        return player.equals(player1) || player.equals(player2);
    }
    
    public boolean isFinished() {
        return player1Score >= maxRounds || player2Score >= maxRounds;
    }
    
    public Player getWinner() {
        if (player1Score >= maxRounds) return player1;
        if (player2Score >= maxRounds) return player2;
        return null;
    }
    
    public void incrementScore(Player player) {
        if (player.equals(player1)) {
            player1Score++;
        } else if (player.equals(player2)) {
            player2Score++;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Duel duel = (Duel) o;
        return Objects.equals(id, duel.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Duel{" +
                "id=" + id +
                ", player1=" + player1.getName() +
                ", player2=" + player2.getName() +
                ", kit=" + kit.getName() +
                ", arena=" + arena.getName() +
                ", maxRounds=" + maxRounds +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                ", currentRound=" + currentRound +
                ", state=" + state +
                '}';
    }
}