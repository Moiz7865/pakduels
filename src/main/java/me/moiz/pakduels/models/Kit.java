package me.moiz.pakduels.models;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Kit {
    private final String name;
    private final ItemStack[] contents;
    private final ItemStack[] armorContents;
    private final ItemStack offHand;
    private final Map<String, Boolean> rules;
    
    public Kit(String name, ItemStack[] contents, ItemStack[] armorContents, ItemStack offHand) {
        this.name = name;
        this.contents = contents != null ? contents : new ItemStack[36];
        this.armorContents = armorContents != null ? armorContents : new ItemStack[4];
        this.offHand = offHand;
        this.rules = new HashMap<>();
        
        // Default rules
        this.rules.put("natural-health-regen", true);
        this.rules.put("block-break", false);
        this.rules.put("block-place", false);
        this.rules.put("health-indicators", true);
        this.rules.put("hunger-loss", false);
        this.rules.put("item-drop", false);
        this.rules.put("item-pickup", false);
    }
    
    public Kit(String name, ItemStack[] contents, ItemStack[] armorContents, ItemStack offHand, Map<String, Boolean> rules) {
        this.name = name;
        this.contents = contents != null ? contents : new ItemStack[36];
        this.armorContents = armorContents != null ? armorContents : new ItemStack[4];
        this.offHand = offHand;
        this.rules = rules != null ? rules : new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public ItemStack[] getContents() {
        return contents;
    }
    
    public ItemStack[] getArmorContents() {
        return armorContents;
    }
    
    public ItemStack getOffHand() {
        return offHand;
    }
    
    public Map<String, Boolean> getRules() {
        return rules;
    }
    
    public boolean getRule(String rule) {
        return rules.getOrDefault(rule, false);
    }
    
    public void setRule(String rule, boolean value) {
        rules.put(rule, value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return Objects.equals(name, kit.name) && 
               Arrays.equals(contents, kit.contents) && 
               Arrays.equals(armorContents, kit.armorContents) && 
               Objects.equals(offHand, kit.offHand);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(name, offHand);
        result = 31 * result + Arrays.hashCode(contents);
        result = 31 * result + Arrays.hashCode(armorContents);
        return result;
    }
    
    @Override
    public String toString() {
        return "Kit{" +
                "name='" + name + '\'' +
                ", contents=" + Arrays.toString(contents) +
                ", armorContents=" + Arrays.toString(armorContents) +
                ", offHand=" + offHand +
                ", rules=" + rules +
                '}';
    }
}