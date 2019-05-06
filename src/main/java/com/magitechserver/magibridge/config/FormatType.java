package com.magitechserver.magibridge.config;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.Utils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Frani on 27/09/2017.
 */
public class FormatType {

    public static FormatType DISCORD_TO_SERVER_FORMAT = new FormatType(() -> MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_FORMAT);

    public static FormatType DISCORD_TO_SERVER_STAFF_FORMAT = new FormatType(() -> MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_STAFF_FORMAT);

    public static FormatType SERVER_TO_DISCORD_FORMAT = new FormatType(() -> MagiBridge.getConfig().MESSAGES.SERVER_TO_DISCORD_FORMAT);

    public static FormatType SERVER_TO_DISCORD_STAFF_FORMAT = new FormatType(() -> MagiBridge.getConfig().MESSAGES.SERVER_TO_DISCORD_STAFF_FORMAT);

    public static FormatType ADVANCEMENT_MESSAGE = new FormatType(() -> MagiBridge.getConfig().MESSAGES.ADVANCEMENT_MESSAGE);

    public static FormatType DEATH_MESSAGE = new FormatType(() -> MagiBridge.getConfig().MESSAGES.DEATH_MESSAGE);

    public static FormatType NEW_PLAYERS_MESSAGE = new FormatType(() -> MagiBridge.getConfig().MESSAGES.NEW_PLAYERS_MESSAGE);

    public static FormatType TOPIC_FORMAT = new FormatType(() -> MagiBridge.getConfig().MESSAGES.TOPIC_MESSAGE);

    public static FormatType OFFLINE_TOPIC_FORMAT = new FormatType(() -> MagiBridge.getConfig().MESSAGES.OFFLINE_TOPIC);

    public static FormatType JOIN_MESSAGE = new FormatType(() -> MagiBridge.getConfig().MESSAGES.PLAYER_JOIN);

    public static FormatType QUIT_MESSAGE = new FormatType(() -> MagiBridge.getConfig().MESSAGES.PLAYER_QUIT);

    public static FormatType GOING_AFK = new FormatType(() -> MagiBridge.getConfig().MESSAGES.AFK.GOING);

    public static FormatType RETURNING_AFK = new FormatType(() -> MagiBridge.getConfig().MESSAGES.AFK.RETURNING);

    public static FormatType WEBHOOK_NAME = new FormatType(() -> MagiBridge.getConfig().MESSAGES.WEBHOOK_NAME);

    public static FormatType SERVER_STARTING = new FormatType(() -> MagiBridge.Config.MESSAGES.SERVER_STARTING);

    public static FormatType SERVER_STOPPING = new FormatType(() -> MagiBridge.Config.MESSAGES.SERVER_STOPPING);

    private Supplier<String> s;

    FormatType(Supplier<String> s) {
        this.s = s;
    }

    public String get() {
        return s.get();
    }

    public String format(Map<String, String> map) {
        return Utils.replaceEach(this.s.get(), map);
    }

    public static FormatType of(Supplier<String> supp) {
        return new FormatType(supp);
    }

}
