package me.moiz.pakduels.models;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Arena {
    private final String name;
    private Location position1;
    private Location position2;
    private Location spawnPoint1;
    private Location spawnPoint2;
    private Location center;
    private List<String> allowedKits;
    private boolean regenerationEnabled;
    private boolean reserved;
    
    public Arena(String name) {
        this.name = name;
        this.allowedKits = new ArrayList<>();
        this.regenerationEnabled = false;
        this.reserved = false;
    }
    
    public Arena(String name, Location position1, Location position2, Location spawnPoint1, Location spawnPoint2) {
        this.name = name;
        this.position1 = position1;
        this.position2 = position2;
        this.spawnPoint1 = spawnPoint1;
        this.spawnPoint2 = spawnPoint2;
        this.allowedKits = new ArrayList<>();
        this.regenerationEnabled = false;
        this.reserved = false;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getPosition1() {
        return position1;
    }
    
    public void setPosition1(Location position1) {
        this.position1 = position1;
    }
    
    public Location getPosition2() {
        return position2;
    }
    
    public void setPosition2(Location position2) {
        this.position2 = position2;
    }
    
    public Location getSpawnPoint1() {
        return spawnPoint1;
    }
    
    public void setSpawnPoint1(Location spawnPoint1) {
        this.spawnPoint1 = spawnPoint1;
    }
    
    public Location getSpawnPoint2() {
        return spawnPoint2;
    }
    
    public void setSpawnPoint2(Location spawnPoint2) {
        this.spawnPoint2 = spawnPoint2;
    }
    
    public Location getCenter() {
        return center;
    }
    
    public void setCenter(Location center) {
        this.center = center;
    }
    
    public List<String> getAllowedKits() {
        return allowedKits;
    }
    
    public void setAllowedKits(List<String> allowedKits) {
        this.allowedKits = allowedKits != null ? allowedKits : new ArrayList<>();
    }
    
    public void addAllowedKit(String kitName) {
        if (!allowedKits.contains(kitName)) {
            allowedKits.add(kitName);
        }
    }
    
    public void removeAllowedKit(String kitName) {
        allowedKits.remove(kitName);
    }
    
    public boolean isKitAllowed(String kitName) {
        return allowedKits.isEmpty() || allowedKits.contains(kitName);
    }
    
    public boolean isRegenerationEnabled() {
        return regenerationEnabled;
    }
    
    public void setRegenerationEnabled(boolean regenerationEnabled) {
        this.regenerationEnabled = regenerationEnabled;
    }
    
    public boolean isReserved() {
        return reserved;
    }
    
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
    
    public boolean isComplete() {
        return position1 != null && position2 != null && spawnPoint1 != null && spawnPoint2 != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return Objects.equals(name, arena.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return "Arena{" +
                "name='" + name + '\'' +
                ", position1=" + position1 +
                ", position2=" + position2 +
                ", spawnPoint1=" + spawnPoint1 +
                ", spawnPoint2=" + spawnPoint2 +
                ", allowedKits=" + allowedKits +
                ", regenerationEnabled=" + regenerationEnabled +
                ", reserved=" + reserved +
                '}';
    }
}