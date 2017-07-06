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
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

/**
 * Created by Frani on 22/06/2017.
 */

@Plugin(id = MagiShat.ID, name = MagiShat.NAME, version = MagiShat.VERSION, description = MagiShat.DESCRIPTION, authors = { MagiShat.AUTHOR })
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
    public Logger logger;

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

    public JDA jda;

    @Listener
    public void gameConstruct(GameConstructionEvent event) {
        instance = this;
    }

    public JDA getJDA() {
        return jda;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        try {
            logger.info("MagiShat is starting!");

            config = new Config(this, configFile, configDir);
            config.load();

            if(Config.BOT_TOKEN.contains(" ") || Config.BOT_TOKEN.contains("-")) {
                logger.error("ERROR STARTING THE PLUGIN:");
                logger.error("THE TOKEN IN THE CONFIG IS INVALID!");
                logger.error("You probably didn't set the token yet, edit your config!");
                return;
            }

            initJDA();
            // Registering listeners
            Sponge.getEventManager().registerListeners(this, new ChatListener());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void reload(GameReloadEvent event) throws Exception {
        Cause cause = Cause.source(this).build();

        GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
        stop(gameStoppingEvent);

        GameInitializationEvent gameInitializationEvent = SpongeEventFactory.createGameInitializationEvent(cause);
        init(gameInitializationEvent);

        logger.info("Plugin reloaded successfully!");
    }

    @Listener
    public void stop(GameStoppingEvent event) throws Exception {

        config = null;

        if(jda != null ) {
            logger.info("Disconnecting from Discord...");
            jda.shutdown(false);
        }
        // Unregistering listeners
        Sponge.getEventManager().unregisterListeners(new ChatListener());

        logger.info("Plugin stopped successfully!");

    }

    private void initJDA() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(Config.BOT_TOKEN).buildBlocking();
            if (jda != null) {
                jda.addEventListener(new MessageListener());
            } else {
                logger.error("ERROR LOGGING TO DISCORD!");
                logger.error("Stacktrace: ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
