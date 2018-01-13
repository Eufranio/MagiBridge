package com.magitechserver.magibridge;

import com.google.inject.Inject;
import com.magitechserver.magibridge.config.ConfigManager;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import com.magitechserver.magibridge.discord.MessageListener;
import com.magitechserver.magibridge.listeners.*;
import com.magitechserver.magibridge.util.CommandHandler;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.TopicUpdater;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.security.auth.login.LoginException;
import java.io.File;
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

    public static MagiBridge instance = null;

    private TopicUpdater updater;

    public static final String ID = "magibridge";
    public static final String NAME = "MagiBridge";
    public static final String DESCRIPTION = "A utility Discord <-> Minecraft chat relay plugin";
    public static final String AUTHOR = "Eufranio";

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    public MagiBridge(Logger logger) {
        this.logger = logger;
    }

    @Inject
    public GuiceObjectMapperFactory factory;

    public static MagiBridge getInstance() { return instance; }

    public static ChatListener UCListener;
    public static SpongeChatListener NucleusListener;
    public static SpongeLoginListener LoginListener;
    public static com.magitechserver.magibridge.listeners.AchievementListener AchievementListener;
    public static com.magitechserver.magibridge.listeners.DeathListener DeathListener;
    public static VanillaChatListener VanillaListener;
    public static ConfigCategory Config;

    public static JDA jda;

    public static Logger logger;

    @Listener
    public void rlyInit(GameInitializationEvent event) {
        UCListener = new ChatListener();
        NucleusListener = new SpongeChatListener();
        LoginListener = new SpongeLoginListener();
        AchievementListener = new AchievementListener();
        DeathListener = new DeathListener();
        VanillaListener = new VanillaChatListener();
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

            Config = new ConfigManager(instance).loadConfig();

            if (!initJDA()) return;

            // Registering listeners
            if (Config.CHANNELS.USE_NUCLEUS) {
                if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                    Sponge.getEventManager().registerListeners(this, NucleusListener);
                    logger.info("Hooking into Nucleus");
                } else {
                    logger.error(" ");
                    logger.error(" MagiBridge is configured to hook into Nucleus, but it isn't loaded! Please disable using-nucleus or load Nucleus on your server!");
                    logger.error(" ");
                }
            } else if (Config.CHANNELS.USE_UCHAT) {
                if (Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
                    Sponge.getEventManager().registerListeners(this, UCListener);
                    logger.info("Hooking into UltimateChat");
                } else {
                    logger.error(" ");
                    logger.error(" MagiBridge is configured to hook into UltimateChat, but it isn't loaded! Please disable using-ultimatechat or load UltimateChat on your server!");
                    logger.error(" ");
                }
            } else {
                Sponge.getEventManager().registerListeners(this, VanillaListener);
                logger.info(" No Chat Hook enabled, hooking into the vanilla chat system");
                logger.info(" Some features may not work, and there will be no staff chat. If you want a more complete chat handling, use either Nucleus or UltimateChat.");
            }

            if (Config.CORE.DEATH_MESSAGES_ENABLED) {
                Sponge.getEventManager().registerListeners(this, DeathListener);
            }
            //if(Config.CORE.ACHIEVEMENT_MESSAGES_ENABLED) {
            //    Sponge.getEventManager().registerListeners(this, AchievementListener);
            //}
            Sponge.getEventManager().registerListeners(this, LoginListener);
            if (!Config.MESSAGES.BOT_GAME_STATUS.isEmpty()) {
                jda.getPresence().setGame(Game.of(Config.MESSAGES.BOT_GAME_STATUS));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!fake) {
            DiscordHandler.sendMessageToChannel(Config.CHANNELS.MAIN_CHANNEL, Config.MESSAGES.SERVER_STARTING);
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
            if (jda != null) DiscordHandler.sendMessageToChannel(Config.CHANNELS.MAIN_CHANNEL, Config.MESSAGES.SERVER_STOPPING);
            if (updater != null) updater.interrupt();
            try {
                jda.getTextChannelById(Config.CHANNELS.MAIN_CHANNEL).getManager().setTopic(FormatType.OFFLINE_TOPIC_FORMAT.get()).queue();
            } catch (NullPointerException e) {}
        }

        logger.info("Disconnecting from Discord...");
        try {
            jda.shutdownNow();
        } catch (NullPointerException | NoClassDefFoundError e) {}

        // Unregistering listeners
        if(Config.CHANNELS.USE_NUCLEUS && Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
            Sponge.getEventManager().unregisterListeners(NucleusListener);
        } else if(Config.CHANNELS.USE_UCHAT && Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
            Sponge.getEventManager().unregisterListeners(UCListener);
        }
        if(Config.CORE.DEATH_MESSAGES_ENABLED) {
            Sponge.getEventManager().unregisterListeners(DeathListener);
        }
        if(Config.CORE.ACHIEVEMENT_MESSAGES_ENABLED) {
            Sponge.getEventManager().unregisterListeners(AchievementListener);
        } else {
            Sponge.getEventManager().unregisterListeners(VanillaListener);
        }
        Sponge.getEventManager().unregisterListeners(LoginListener);
        Config = null;
        logger.info("Plugin stopped successfully!");
    }

    private boolean initJDA() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(Config.CORE.BOT_TOKEN).buildBlocking();
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

    public static ConfigCategory getConfig() {
        return Config;
    }
}
