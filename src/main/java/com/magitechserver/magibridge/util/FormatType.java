package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;

/**
 * Created by Frani on 27/09/2017.
 */
public enum FormatType {

    DISCORD_TO_SERVER_FORMAT            (MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_FORMAT),
    DISCORD_TO_SERVER_STAFF_FORMAT      (MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_STAFF_FORMAT),
    SERVER_TO_DISCORD_FORMAT            (MagiBridge.getConfig().MESSAGES.SERVER_TO_DISCORD_FORMAT),
    SERVER_TO_DISCORD_STAFF_FORMAT      (MagiBridge.getConfig().MESSAGES.DISCORD_TO_SERVER_STAFF_FORMAT),
    ACHIEVEMENT_MESSAGE                 (MagiBridge.getConfig().MESSAGES.ACHIEVEMENT_MESSAGE),
    DEATH_MESSAGE                       (MagiBridge.getConfig().MESSAGES.DEATH_MESSAGE),
    NEW_PLAYERS_MESSAGE                 (MagiBridge.getConfig().MESSAGES.NEW_PLAYERS_MESSAGE),
    TOPIC_FORMAT                        (MagiBridge.getConfig().MESSAGES.TOPIC_MESSAGE),
    OFFLINE_TOPIC_FORMAT                (MagiBridge.getConfig().MESSAGES.OFFLINE_TOPIC),
    JOIN_MESSAGE                        (MagiBridge.getConfig().MESSAGES.PLAYER_JOIN),
    QUIT_MESSAGE                        (MagiBridge.getConfig().MESSAGES.PLAYER_QUIT);

    private String s;
    FormatType(String s){
        this.s = s;
    }
    public String get(){
        return s;
    }
}
