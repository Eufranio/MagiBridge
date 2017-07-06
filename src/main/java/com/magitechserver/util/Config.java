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
               System.out.println("Houve um erro ao carregar a config, abortando!");
                return;
            }
        }

        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        ConfigurationNode rootNode;

        try {
            rootNode = loader.load();
        } catch (IOException e) {
            System.out.println("A config nao pode ser carregada!");
            return;
        }

        ConfigurationNode bot = rootNode.getNode("bot");
        BOT_TOKEN = bot.getNode("token").getString("your-token-here");

        ConfigurationNode channel = rootNode.getNode("channel");
        DISCORD_MAIN_CHANNEL = channel.getNode("discord-main-channel").getString("Main channel ID of your discord server");
        DISCORD_STAFF_CHANNEL = channel.getNode("discord-staff-channel").getString("Channel ID of your Staff/Admin channel");

        ConfigurationNode messages = rootNode.getNode("messages");
        DISCORD_TO_SERVER_FORMAT = messages.getNode("discord-to-server-global-format").getString("Format of the messsage sent from Discord to the server. Supports %user% and %msg%");

        System.out.println("Config carregada!");

    }

    public static String BOT_TOKEN;

    public static String DISCORD_MAIN_CHANNEL;

    public static String DISCORD_TO_SERVER_FORMAT;

    public static String DISCORD_STAFF_CHANNEL;

}
