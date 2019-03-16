package com.magitechserver.magibridge.config;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Frani on 27/09/2017.
 */
public class ConfigManager {
    public ConfigCategory root;
    private String HEADER = " MagiBridge, by Eufranio\n" +
            "\n" +
            " You can use the following placeholders on the DISCORD -> MC formats:\n" +
            "     %user% -> user who sent the message in the Discord channel\n" +
            "     %message% -> message that was sent to the channel\n" +
            "     %toprole% -> name of the highest role/rank of the user in the Discord server\n" +
            "     %toprolecolor% -> color of the user's role, according to the replacer at the bottom of this config\n" +
            "\n" +
            " You can use the following placeholder on the MC -> DISCORD formats:\n" +
            "     %player% -> player who sent the message in the chat channel\n" +
            "     %prefix% -> prefix of the player that sent the message. Usually set via permission plugins\n" +
            "     %topgroup% -> name of the highest group of the player who sent the message\n" +
            "     %nick% -> nickname of the player. If no nick is assigned to the player, his name will be used instead\n" +
            "     %message% -> message that the player sent";
    private MagiBridge instance;

    public ConfigManager(MagiBridge instance) {
        this.instance = instance;
        if (!instance.configDir.exists()) {
            instance.configDir.mkdirs();
        }
    }

    public ConfigCategory get() {
        return root;
    }

    public ConfigCategory loadConfig() {
        try {
            File configFile = new File(MagiBridge.instance.configDir, "MagiBridge.conf");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(configFile).build();
            CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(instance.factory).setShouldCopyDefaults(true).setHeader(HEADER));
            root = config.getValue(TypeToken.of(ConfigCategory.class), new ConfigCategory());

            Map<String, String> UCHAT_CHANNELS = Maps.newHashMap();
            for (Map.Entry<String, String> values : root.CHANNELS.UCHAT.UCHAT_CHANNELS.entrySet()) {
                UCHAT_CHANNELS.put(values.getKey().replace("#", ""), values.getValue());
            }

            try {
                config.setValue(TypeToken.of(ConfigCategory.class), root);
                loader.save(config);
                root.CHANNELS.UCHAT.UCHAT_CHANNELS = UCHAT_CHANNELS;
            } catch (IOException | ObjectMappingException e) {
                MagiBridge.getLogger().error("Could not save config.", e);
                return root;
            }

            return root;
        } catch (Exception e) {
            MagiBridge.getLogger().error("Could not load config.", e);
            return root;
        }
    }

}
