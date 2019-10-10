package com.magitechserver.magibridge.chat;

/**
 * Created by Frani on 09/10/2019.
 */
public interface MessageBuilder {

    Type getType();

    enum Type {

        SERVER_TO_DISCORD,
        DISCORD_TO_SERVER

    }

}
