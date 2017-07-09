package com.magitechserver;

import com.google.inject.Inject;
import com.magitechserver.discord.MessageListener;
import com.magitechserver.listeners.ChatListener;
import com.magitechserver.listeners.SpongeChatListener;
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
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;

/**
 * Created by Frani on 22/06/2017.
 */

@Plugin(id = MagiBridge.ID,
        name = MagiBridge.NAME,
        description = MagiBridge.DESCRIPTION,
        authors = { MagiBridge.AUTHOR },
        dependencies = {@Dependency(id = "ultimatechat", optional = true),
                        @Dependency(id = "nucleus", optional = true)})

public class MagiBridge {

    private static MagiBridge instance = null;

    public static final String ID = "magibridge";
    public static final String NAME = "MagiBridge";
    public static final String DESCRIPTION = "A utility Discord <-> Minecraft chat relay plugin";
    public static final String AUTHOR = "Eufranio";

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configFile;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    public MagiBridge(Logger logger) {
        this.logger = logger;
    }

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

    public static MagiBridge getInstance() { return instance; }

    public static ChatListener UCListener;

    public static SpongeChatListener NucleusListener;

    public static JDA jda;

    public static Logger logger;

    @Listener
    public void gameConstruct(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void gameInitialization(GameInitializationEvent event) {
        UCListener = new ChatListener();
        NucleusListener = new SpongeChatListener();
    }

    @Listener
    public void init(GamePostInitializationEvent event) {
        initStuff(false);
    }

    @Listener
    public void stop(GameStoppingEvent event) throws Exception {
        stopStuff(false);
    }

    @Listener
    public void reload(GameReloadEvent event) throws Exception {
        stopStuff(true);
        initStuff(true);
        logger.info("Plugin reloaded successfully!");
    }

    public void initStuff(Boolean fake) {
        try {
            logger.info("MagiBridge is starting!");

            config = new Config(this, configFile, configDir);
            config.load();

            if(!initJDA()) return;

            // Registering listeners
            if(Config.USE_NUCLEUS && Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                Sponge.getEventManager().registerListeners(this, NucleusListener);
                logger.info("Hooking into Nucleus");
            } else if(Config.USE_UCHAT && Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
                Sponge.getEventManager().registerListeners(this, UCListener);
                logger.info("Hooking into UltimateChat");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!fake) {
            DiscordHandler.sendMessageToChannel(Config.MAIN_DISCORD_CHANNEL, Config.SERVER_STARTING_MESSAGE);
        }

    }

    public void stopStuff(Boolean fake) {

        if(!fake) {
            DiscordHandler.sendMessageToChannel(Config.MAIN_DISCORD_CHANNEL, Config.SERVER_STOPPING_MESSAGE);
        }

        config = null;

        logger.info("Disconnecting from Discord...");
        try {
            jda.shutdown(false);
        } catch (NullPointerException e) {}

        // Unregistering listeners
        if(Config.USE_NUCLEUS && Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
            Sponge.getEventManager().unregisterListeners(NucleusListener);
        } else if(Config.USE_UCHAT && Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
            Sponge.getEventManager().unregisterListeners(UCListener);
        }

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
