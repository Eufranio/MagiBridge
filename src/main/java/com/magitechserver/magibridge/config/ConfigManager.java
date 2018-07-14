package com.magitechserver.magibridge.config;

import com.google.common.reflect.TypeToken;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;

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
            File file = new File(instance.configDir, "MagiBridge.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(file).build();
            CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(instance.factory).setShouldCopyDefaults(true).setHeader(HEADER));
            root = config.getValue(TypeToken.of(ConfigCategory.class), new ConfigCategory());
            loader.save(config);
            return root;
        } catch (Exception e) {
            MagiBridge.getLogger().error("Could not load config.", e);
            return root;
        }
    }

}
