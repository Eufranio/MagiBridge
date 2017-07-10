package com.magitechserver.util;


import com.google.common.reflect.TypeToken;
import com.magitechserver.MagiBridge;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by Frani on 04/07/2017.
 */
public class Config {

    private final MagiBridge instance;
    private final Path configFile;
    private final Path configDir;

    public Config(MagiBridge instance, Path configFile, Path configDir) {
        this.instance = instance;
        this.configFile = configFile;
        this.configDir = configDir;
    }

    public void load() {
        if(!configFile.toFile().exists()) {
            try {
                Sponge.getAssetManager().getAsset(instance, "MagiBridge.conf").get().copyToFile(configFile);
            } catch (IOException | NoSuchElementException e) {
               MagiBridge.logger.error("Could not create the default config! Report it in the plugin issue tracker, and include this stacktrace: ");
               e.printStackTrace();
                return;
            }
        }

        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        ConfigurationNode rootNode;

        try {
            rootNode = loader.load();
            MagiBridge.logger.error(configDir.toString());
        } catch (IOException e) {
            MagiBridge.logger.error("Could not load the default config! Report it in the plugin issue tracker, and include this stacktrace: ");
            e.printStackTrace();
            return;
        }

        TypeToken<Map<String, String>> typeToken = new TypeToken<Map<String, String>>() {};

        ConfigurationNode bot = rootNode.getNode("bot");
        BOT_TOKEN = bot.getNode("token").getString("your-token-here");

        ConfigurationNode channel = rootNode.getNode("channel");
        MAIN_DISCORD_CHANNEL = channel.getNode("main-discord-channel").getString();
        USE_NUCLEUS = channel.getNode("use-nucleus").getBoolean();
        USE_UCHAT = channel.getNode("use-ultimatechat").getBoolean();

        ConfigurationNode ids = channel.getNode("ultimatechat");
        try {
            CHANNELS = ids.getValue(typeToken);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        ConfigurationNode nucleus = channel.getNode("nucleus");
        NUCLEUS_GLOBAL_DISCORD_CHANNEL = nucleus.getNode("global-discord-channel").getString();
        NUCLEUS_STAFF_DISCORD_CHANNEL = nucleus.getNode("staff-discord-channel").getString();

        ConfigurationNode messages = rootNode.getNode("messages");
        DISCORD_TO_SERVER_FORMAT = messages.getNode("discord-to-server-global-format").getString("&7[&b&lDiscord&7] &f%user%&7: &7%msg%");
        DISCORD_TO_SERVER_STAFF_FORMAT = messages.getNode("discord-to-server-staff-format").getString("&7[&c&lDiscord&7] &f%user%&7: &7%msg%");
        SERVER_STARTING_MESSAGE = messages.getNode("server-starting-message").getString("**The server is starting!**");
        SERVER_STOPPING_MESSAGE = messages.getNode("server-stopping-message").getString("**The server is stopping!**");

        MagiBridge.logger.info("Config loaded successfully!");

        /* try {
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        } */
    }

    public static String BOT_TOKEN;

    public static String DISCORD_TO_SERVER_FORMAT;

    public static String DISCORD_TO_SERVER_STAFF_FORMAT;

    public static String SERVER_STARTING_MESSAGE;

    public static String SERVER_STOPPING_MESSAGE;

    public static String MAIN_DISCORD_CHANNEL;

    public static Map<String, String> CHANNELS;

    public static Boolean USE_NUCLEUS;

    public static Boolean USE_UCHAT;

    public static String NUCLEUS_GLOBAL_DISCORD_CHANNEL;

    public static String NUCLEUS_STAFF_DISCORD_CHANNEL;

}
