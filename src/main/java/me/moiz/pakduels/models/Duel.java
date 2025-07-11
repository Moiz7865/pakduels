package me.moiz.pakduels.models;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Duel {
    private final UUID id;
    private final Player player1;
    private final Player player2;
    private final Kit kit;
    private final Arena arena;
    private final int maxRounds;
    private int currentRound;
    private int player1Score;
    private int player2Score;
    private DuelState state;
    private final Map<Player, ItemStack[]> savedInventories;
    private final Map<Player, ItemStack[]> savedArmor;
    private final Map<Player, ItemStack> savedOffHand;
    
    public enum DuelState {
        WAITING,
        STARTING,
        INVENTORY_COUNTDOWN,
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
        this.currentRound = 1;
        this.player1Score = 0;
        this.player2Score = 0;
        this.state = DuelState.WAITING;
        this.savedInventories = new HashMap<>();
        this.savedArmor = new HashMap<>();
        this.savedOffHand = new HashMap<>();
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
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
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
    
    public DuelState getState() {
        return state;
    }
    
    public void setState(DuelState state) {
        this.state = state;
    }
    
    public Player getOpponent(Player player) {
        return player.equals(player1) ? player2 : player1;
    }
    
    public boolean hasPlayer(Player player) {
        return player.equals(player1) || player.equals(player2);
    }
    
    public void incrementScore(Player winner) {
        if (winner.equals(player1)) {
            player1Score++;
        } else if (winner.equals(player2)) {
            player2Score++;
        }
    }
    
    public Player getWinner() {
        if (player1Score > maxRounds / 2) {
            return player1;
        } else if (player2Score > maxRounds / 2) {
            return player2;
        }
        return null;
    }
    
    public boolean isFinished() {
        return getWinner() != null;
    }
    
    public void savePlayerInventory(Player player) {
        savedInventories.put(player, player.getInventory().getContents().clone());
        savedArmor.put(player, player.getInventory().getArmorContents().clone());
        savedOffHand.put(player, player.getInventory().getItemInOffHand().clone());
    }
    
    public void restorePlayerInventory(Player player) {
        ItemStack[] inventory = savedInventories.get(player);
        ItemStack[] armor = savedArmor.get(player);
        ItemStack offHand = savedOffHand.get(player);
        
        if (inventory != null) {
            player.getInventory().setContents(inventory);
        }
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
        if (offHand != null) {
            player.getInventory().setItemInOffHand(offHand);
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
                ", currentRound=" + currentRound +
                ", maxRounds=" + maxRounds +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                ", state=" + state +
                '}';
    }
}