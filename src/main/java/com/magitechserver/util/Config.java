package com.magitechserver.util;


import com.magitechserver.MagiShat;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

/**
 * Created by Frani on 04/07/2017.
 */
public class Config {

    private final MagiShat instance;
    private final Path configFile;
    private final Path configDir;

    public Config(MagiShat instance, Path configFile, Path configDir) {
        this.instance = instance;
        this.configFile = configFile;
        this.configDir = configDir;
    }

    public void load() {
        if(!configFile.toFile().exists()) {
            try {
                Sponge.getAssetManager().getAsset(instance, "MagiShat.conf").get().copyToFile(configFile);
            } catch (IOException | NoSuchElementException e) {
               MagiShat.getInstance().logger.error("Could not create the default config! Report it in the plugin issue tracker, and include this stacktrace: ");
               e.printStackTrace();
                return;
            }
        }

        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        ConfigurationNode rootNode;

        try {
            rootNode = loader.load();
        } catch (IOException e) {
            MagiShat.getInstance().logger.error("Could not load the default config! Report it in the plugin issue tracker, and include this stacktrace: ");
            e.printStackTrace();
            return;
        }

        ConfigurationNode bot = rootNode.getNode("bot");
        BOT_TOKEN = bot.getNode("token").getString("your-token-here");

        ConfigurationNode channel = rootNode.getNode("channel");
        DISCORD_MAIN_CHANNEL = channel.getNode("discord-main-channel").getString("Main channel ID of your discord server");
        DISCORD_STAFF_CHANNEL = channel.getNode("discord-staff-channel").getString("Channel ID of your Staff/Admin channel");

        ConfigurationNode messages = rootNode.getNode("messages");
        DISCORD_TO_SERVER_FORMAT = messages.getNode("discord-to-server-global-format").getString("Format of the messsage sent from Discord to the server. Supports %user% and %msg%");

        MagiShat.getInstance().logger.info("Config loaded successfully!");

    }

    public static String BOT_TOKEN;

    public static String DISCORD_MAIN_CHANNEL;

    public static String DISCORD_TO_SERVER_FORMAT;

    public static String DISCORD_STAFF_CHANNEL;

}
