package com.pseudosmp.PlayerBiomes;

import com.jeff_media.jefflib.*;
import com.jeff_media.jefflib.pluginhooks.PlaceholderAPIUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bstats.bukkit.Metrics;


public class PlayerBiomes extends JavaPlugin {

    public static String getBiomeFormatted(OfflinePlayer player) {
        NamespacedKey namespacedKey = BiomeUtils.getBiomeNamespacedKey(player.getPlayer().getLocation());
        String biomeNamespace = namespacedKey.getNamespace();
        String biomeKey = namespacedKey.getKey();

        String biome = biomeKey.replaceAll("[_.]", " ");
        StringBuilder formattedBiome = new StringBuilder();

        int findSlash = biome.lastIndexOf("/");
        biome = biome.substring(findSlash + 1);

        String[] words = biome.split("\\s");
        for (String w : words) {
            formattedBiome.append(w.substring(0, 1).toUpperCase()).append(w.substring(1)).append(" ");
        }
        formattedBiome.insert(0, biomeNamespace.substring(0, 1).toUpperCase() + biomeNamespace.substring(1) + ": ");
        return formattedBiome.toString().trim();
    }

    @Override
    public void onEnable() {

        // bstats, enabled by default 
        if (getConfig().getBoolean("bstats_consent", true)) {
            int pluginId = 17782;
            Metrics metrics = new Metrics(this, pluginId);
            getLogger().info("bstats for PlayerBiomes has been enabled. You can opt-out by disabling bstats in the plugin config.");
        }

        // placeholders
        JeffLib.init(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIUtils.register("biome_raw", player -> {
                if (player.isOnline()) {
                    return BiomeUtils.getBiomeNamespacedKey(player.getPlayer().getLocation()).getNamespace() + ":" + BiomeUtils.getBiomeNamespacedKey(player.getPlayer().getLocation()).getKey();
                } else {
                    return null;
                }
            });

            PlaceholderAPIUtils.register("biome_namespace", player -> {
                if (player.isOnline()) {
                    String ns = BiomeUtils.getBiomeNamespacedKey(player.getPlayer().getLocation()).getNamespace();
                    return ns.substring(0, 1).toUpperCase() + ns.substring(1);
                } else {
                    return null;
                }
            });

            PlaceholderAPIUtils.register("biome_name", player -> {
                if (player.isOnline()) {
                    String formattedBiome = getBiomeFormatted(player);
                    int nameIndex = formattedBiome.indexOf(":") + 2;
                    return formattedBiome.substring(nameIndex);
                } else {
                    return null;
                }
            });

            PlaceholderAPIUtils.register("biome_formatted", player -> {
                if (player.isOnline()) {
                    return getBiomeFormatted(player);
                } else {
                    return null;
                }
            });
        } else {
            getLogger().warning("PlaceholderAPI is not available and thus placeholders will not be registered");
        }
        // Save the default config if it doesn't exist
        this.saveDefaultConfig();

        // Register the /whereami command
        this.getCommand("whereami").setExecutor(new WhereAmICommand(this));
        getLogger().info("/whereami is now a valid question! (PlayerBiomes has been enabled)");
    }
    public class WhereAmICommand implements CommandExecutor {
        private final JavaPlugin plugin;
        public WhereAmICommand(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                String message = plugin.getConfig().getString("messages.console_whereami");
                if (message == null) {
                    getLogger().warning("The console message for /whereami is blank. Check your config!");
                    message = "This command can only be executed by a player.";
                }
                sender.sendMessage(message);
                return true;
            }

            OfflinePlayer player = (Player) sender;
            String formattedBiome = getBiomeFormatted(player);

            // Get the message from the config and send
            String message = plugin.getConfig().getString("messages.user_whereami");
            if (message == null) {
                getLogger().warning("The user message for /whereami is blank. Check your config!");
                message = "You are currently in the %s biome.";
            } else if (!message.contains("%s")) {
                getLogger().warning("Format specifier '%s' not available in the player message. Please read instructions in the config properly!");
                getLogger().warning("The player who executed the command will be shown the default message.");
                message = "You are currently in the %s biome.";
            }
            player.getPlayer().sendMessage(String.format(message, formattedBiome));
            return true;
        }
    }
}