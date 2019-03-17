package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;

import java.util.function.Supplier;

/**
 * Created by Frani on 27/09/2017.
 */
public enum FormatType {

    DISCORD_TO_SERVER_FORMAT(() -> MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_FORMAT),
    DISCORD_TO_SERVER_STAFF_FORMAT(() -> MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_STAFF_FORMAT),
    SERVER_TO_DISCORD_FORMAT(() -> MagiBridge.getConfig().MESSAGES.SERVER_TO_DISCORD_FORMAT),
    SERVER_TO_DISCORD_STAFF_FORMAT(() -> MagiBridge.getConfig().MESSAGES.SERVER_TO_DISCORD_STAFF_FORMAT),
    ADVANCEMENT_MESSAGE(() -> MagiBridge.getConfig().MESSAGES.ADVANCEMENT_MESSAGE),
    DEATH_MESSAGE(() -> MagiBridge.getConfig().MESSAGES.DEATH_MESSAGE),
    NEW_PLAYERS_MESSAGE(() -> MagiBridge.getConfig().MESSAGES.NEW_PLAYERS_MESSAGE),
    TOPIC_FORMAT(() -> MagiBridge.getConfig().MESSAGES.TOPIC_MESSAGE),
    PRESENCE_FORMAT(() -> MagiBridge.getConfig().MESSAGES.BOT_GAME_STATUS),
    OFFLINE_TOPIC_FORMAT(() -> MagiBridge.getConfig().MESSAGES.OFFLINE_TOPIC),
    JOIN_MESSAGE(() -> MagiBridge.getConfig().MESSAGES.PLAYER_JOIN),
    QUIT_MESSAGE(() -> MagiBridge.getConfig().MESSAGES.PLAYER_QUIT),
    GOING_AFK(() -> MagiBridge.getConfig().MESSAGES.AFK.GOING),
    RETURNING_AFK(() -> MagiBridge.getConfig().MESSAGES.AFK.RETURNING);

    private Supplier<String> s;

    FormatType(Supplier<String> s) {
        this.s = s;
    }

    public String get() {
        return s.get();
    }
}
