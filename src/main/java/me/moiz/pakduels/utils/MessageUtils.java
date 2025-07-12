package me.moiz.pakduels.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import me.moiz.pakduels.PakDuelsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    private static PakDuelsPlugin plugin;
    
    public static void setPlugin(PakDuelsPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    public static void sendMessage(CommandSender sender, String messageKey) {
        if (plugin != null && plugin.getConfigManager().isMessageEnabled(messageKey)) {
            String message = plugin.getConfigManager().getMessageText(messageKey);
            String prefix = plugin.getConfigManager().getMessageText("prefix");
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message);
            sender.sendMessage(component);
        }
    }
    
    public static void sendMessage(CommandSender sender, String messageKey, String placeholder, String value) {
        if (plugin != null && plugin.getConfigManager().isMessageEnabled(messageKey)) {
            String message = plugin.getConfigManager().getMessageText(messageKey);
            message = message.replace("{" + placeholder + "}", value);
            String prefix = plugin.getConfigManager().getMessageText("prefix");
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message);
            sender.sendMessage(component);
        }
    }
    
    public static void sendRawMessage(CommandSender sender, String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        sender.sendMessage(component);
    }
    
    public static void sendRawMessage(Player player, String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        player.sendMessage(component);
    }
    
    public static Component colorize(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
    
    public static Component createComponent(String text, TextColor color) {
        return Component.text(text).color(color);
    }
}