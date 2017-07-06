package com.magitechserver;

import com.google.inject.Inject;
import com.magitechserver.discord.MessageListener;
import com.magitechserver.util.Config;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;

/**
 * Created by Frani on 22/06/2017.
 */

@Plugin(id = MagiShat.ID,
        name = MagiShat.NAME,
        version = MagiShat.VERSION,
        description = MagiShat.DESCRIPTION,
        authors = { MagiShat.AUTHOR },
        dependencies = @Dependency(id = "ultimatechat", optional = true))

public class MagiShat {

    private static MagiShat instance = null;

    public static final String ID = "magishat";
    public static final String NAME = "MagiShat";
    public static final String VERSION = "1.0.0";
    public static final String DESCRIPTION = "A utility Discord <-> Minecraft chat relay plugin";
    public static final String AUTHOR = "Eufranio";

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configFile;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    public static Logger logger;

    private Config config;

    public static Path getConfigFile() {
        return instance.configFile;
    }

    public static Path getConfigDir() {
        return instance.configDir;
    }

    public static Config getConfig() {
        return instance.config;
    }

    public static MagiShat getInstance() { return instance; }

    public static JDA jda;

    @Listener
    public void gameConstruct(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        initStuff(false);
        DiscordHandler.sendMessageToChannel(Config.DISCORD_MAIN_CHANNEL, Config.SERVER_STARTING_MESSAGE);
    }

    @Listener
    public void stop(GameStoppingEvent event) throws Exception {
        stopStuff(false);
        DiscordHandler.sendMessageToChannel(Config.DISCORD_MAIN_CHANNEL, Config.SERVER_STOPPING_MESSAGE);
    }

    @Listener
    public void reload(GameReloadEvent event) throws Exception {
        stopStuff(true);
        initStuff(true);
        logger.info("Plugin reloaded successfully!");
    }

    public void initStuff(Boolean fake) {
        try {
            logger.info("MagiShat is starting!");

            config = new Config(this, configFile, configDir);
            config.load();

            if(!initJDA()) return;

            // Registering listeners
            Sponge.getEventManager().registerListeners(this, new ChatListener());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopStuff(Boolean fake) {
        config = null;

        logger.info("Disconnecting from Discord...");
        jda.shutdown(false);

        // Unregistering listeners
        Sponge.getEventManager().unregisterListeners(new ChatListener());

        logger.info("Plugin stopped successfully!");
    }

    private boolean initJDA() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(Config.BOT_TOKEN).buildBlocking();
            jda.addEventListener(new MessageListener());
        } catch (LoginException e) {
            logger.error("ERROR STARTING THE PLUGIN:");
            logger.error("THE TOKEN IN THE CONFIG IS INVALID!");
            logger.error("You probably didn't set the token yet, edit your config!");
            return false;
        } catch (Exception e) {
            logger.error("Error connecting to discord. This is NOT a plugin error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
