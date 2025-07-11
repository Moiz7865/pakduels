package me.moiz.pakduels.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SerializationUtils {
    
    public static String serializeItemStack(ItemStack item) {
        if (item == null) return "";
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeObject(item);
            dataOutput.close();
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            
            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<String> serializeItemStackArray(ItemStack[] items) {
        List<String> result = new ArrayList<>();
        
        for (ItemStack item : items) {
            result.add(serializeItemStack(item));
        }
        
        return result;
    }
    
    public static ItemStack[] deserializeItemStackArray(List<String> data) {
        if (data == null) return new ItemStack[0];
        
        ItemStack[] items = new ItemStack[data.size()];
        
        for (int i = 0; i < data.size(); i++) {
            items[i] = deserializeItemStack(data.get(i));
        }
        
        return items;
    }
    
    public static String serializeLocation(Location location) {
        if (location == null) return "";
        
        return location.getWorld().getName() + "," + 
               location.getX() + "," + 
               location.getY() + "," + 
               location.getZ() + "," + 
               location.getYaw() + "," + 
               location.getPitch();
    }
    
    public static Location deserializeLocation(String data) {
        if (data == null || data.isEmpty()) return null;
        
        String[] parts = data.split(",");
        if (parts.length != 6) return null;
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}