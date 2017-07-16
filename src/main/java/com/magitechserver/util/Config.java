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
    private ConfigurationNode rootNode;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private TypeToken<Map<String, String>> typeToken;

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

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            rootNode = loader.load();
        } catch (IOException e) {
            MagiBridge.logger.error("Could not load the default config! Report it in the plugin issue tracker, and include this stacktrace: ");
            e.printStackTrace();
            return;
        }

        typeToken = new TypeToken<Map<String, String>>() {};

        MagiBridge.logger.info("Config loaded successfully!");

        /* try {
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        } */
    }

    public Boolean getBool(Object... key) {
        return rootNode.getNode(key).getBoolean(false);
    }

    public String getString(Object... key) {
        return rootNode.getNode(key).getString();
    }

    public Map<String, String> getMap(Object... key) {
        try {
            return rootNode.getNode(key).getValue(typeToken);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean useWebhooks() {
        return MagiBridge.getConfig().getBool("channel", "use-webhooks");
    }
}
