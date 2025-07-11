package me.moiz.pakduels.models;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class DuelRequest {
    private final UUID id;
    private final Player challenger;
    private final Player challenged;
    private final Kit kit;
    private final int rounds;
    private final long createdAt;
    
    public DuelRequest(Player challenger, Player challenged, Kit kit, int rounds) {
        this.id = UUID.randomUUID();
        this.challenger = challenger;
        this.challenged = challenged;
        this.kit = kit;
        this.rounds = rounds;
        this.createdAt = System.currentTimeMillis();
    }
    
    public UUID getId() {
        return id;
    }
    
    public Player getChallenger() {
        return challenger;
    }
    
    public Player getChallenged() {
        return challenged;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public int getRounds() {
        return rounds;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > 30000; // 30 seconds
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuelRequest that = (DuelRequest) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DuelRequest{" +
                "id=" + id +
                ", challenger=" + challenger.getName() +
                ", challenged=" + challenged.getName() +
                ", kit=" + kit.getName() +
                ", rounds=" + rounds +
                ", createdAt=" + createdAt +
                '}';
    }
}