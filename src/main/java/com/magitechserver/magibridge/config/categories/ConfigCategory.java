package com.magitechserver.magibridge.config.categories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public final class ConfigCategory {

    @Setting(value = "core")
    public CoreCategory CORE = new CoreCategory();

    @Setting(value = "channel")
    public Channel CHANNELS = new Channel();

    @Setting(value = "messages")
    public Messages MESSAGES = new Messages();

    @Setting(value = "discord-to-mc-replacer")
    public Replacer REPLACER = new Replacer();

    @Setting(value = "colors-converter")
    public Colors COLORS = new Colors();

}
