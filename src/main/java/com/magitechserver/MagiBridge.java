package com.magitechserver;

import com.google.inject.Inject;
import com.magitechserver.discord.MessageListener;
import com.magitechserver.listeners.*;
import com.magitechserver.util.CommandHandler;
import com.magitechserver.util.Config;
import com.magitechserver.util.TopicUpdater;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
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
                        @Dependency(id = "nucleus", optional = true),
                        @Dependency(id = "boop", version = "[1.5.0,)", optional = true)})

public class MagiBridge {

    private static MagiBridge instance = null;

    private TopicUpdater updater;

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

    public static Config getConfig() {
        return instance.config;
    }

    public static MagiBridge getInstance() { return instance; }

    public static ChatListener UCListener;
    public static SpongeChatListener NucleusListener;
    public static SpongeLoginListener LoginListener;
    public static AchievementListener AchievementListener;
    public static DeathListener DeathListener;

    public static JDA jda;

    public static Logger logger;

    @Listener
    public void rlyInit(GameInitializationEvent event) {
        UCListener = new ChatListener();
        NucleusListener = new SpongeChatListener();
        LoginListener = new SpongeLoginListener();
        AchievementListener = new AchievementListener();
        DeathListener = new DeathListener();
    }

    @Listener
    public void init(GamePostInitializationEvent event) {
        instance = this;
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
            if(getConfig().getBool("channel", "use-nucleus") && Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                Sponge.getEventManager().registerListeners(this, NucleusListener);
                logger.info("Hooking into Nucleus");
            } else if(getConfig().getBool("channel", "use-ultimatechat") && Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
                Sponge.getEventManager().registerListeners(this, UCListener);
                logger.info("Hooking into UltimateChat");
            }
            if(getConfig().getBool("misc", "death-messages-enabled")) {
                Sponge.getEventManager().registerListeners(this, DeathListener);
            }
            if(getConfig().getBool("misc", "achievement-messages-enabled")) {
                Sponge.getEventManager().registerListeners(this, AchievementListener);
            }
            Sponge.getEventManager().registerListeners(this, LoginListener);
            if(!getConfig().getString("messages", "bot-game-status").trim().isEmpty()) {
                jda.getPresence().setGame(Game.of(getConfig().getString("messages", "bot-game-status")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!fake) {
            DiscordHandler.sendMessageToChannel(getConfig().getString("channel", "main-discord-channel"), getConfig().getString("messages", "server-starting-message"));
            CommandHandler.registerBroadcastCommand();

            if (updater != null) {
                if (updater.getState() == Thread.State.NEW) {
                    updater.start();
                } else {
                    updater.interrupt();
                    updater = new TopicUpdater();
                    updater.start();
                }
            } else {
                updater = new TopicUpdater();
                updater.start();
            }

        }

    }

    public void stopStuff(Boolean fake) {

        if(!fake) {
            DiscordHandler.sendMessageToChannel(getConfig().getString("channel", "main-discord-channel"), getConfig().getString("messages", "server-stopping-message"));
            if (updater != null) updater.interrupt();
            try {
                jda.getTextChannelById(getConfig().getString("channel", "main-discord-channel")).getManager().setTopic(getConfig().getString("messages", "channel-topic-offline")).queue();
            } catch (NullPointerException e) {}
        }

        logger.info("Disconnecting from Discord...");
        try {
            jda.shutdown(false);
        } catch (NullPointerException e) {}

        // Unregistering listeners
        if(getConfig().getBool("channel", "use-nucleus") && Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
            Sponge.getEventManager().unregisterListeners(NucleusListener);
        } else if(getConfig().getBool("channel", "use-ultimatechat") && Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
            Sponge.getEventManager().unregisterListeners(UCListener);
        }
        if(getConfig().getBool("misc", "death-messages-enabled")) {
            Sponge.getEventManager().unregisterListeners(DeathListener);
        }
        if(getConfig().getBool("misc", "achievement-messages-enabled")) {
            Sponge.getEventManager().unregisterListeners(AchievementListener);
        }
        Sponge.getEventManager().unregisterListeners(LoginListener);
        config = null;
        logger.info("Plugin stopped successfully!");

    }

    private boolean initJDA() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(getConfig().getString("bot", "token")).buildBlocking();
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
