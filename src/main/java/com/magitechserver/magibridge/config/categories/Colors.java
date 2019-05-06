package com.magitechserver.magibridge.config.categories;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class Colors {

    @Setting(value = "discord-to-mc-replacer", comment = "Conversor from RGB to in-game colors (for %toprolecolor%)\n" +
            "Format: \"RGB\" = \"IN-GAME COLOR\"")
    public Map<String, String> COLORS = Maps.newHashMap();

    public Colors() {
        COLORS.put("99AAB5", "&f");
        COLORS.put("1ABC9C", "&a");
        COLORS.put("2ECC71", "&a");
        COLORS.put("3498DB", "&3");
        COLORS.put("9B59B6", "&5");
        COLORS.put("E91E63", "&d");
        COLORS.put("F1C40F", "&e");
        COLORS.put("E67E22", "&6");
        COLORS.put("E74C3C", "&c");
        COLORS.put("95A5A6", "&7");
        COLORS.put("607D8B", "&8");
        COLORS.put("11806A", "&2");
        COLORS.put("1F8B4C", "&2");
        COLORS.put("206694", "&1");
        COLORS.put("71368A", "&5");
        COLORS.put("AD1457", "&d");
        COLORS.put("C27C0E", "&6");
        COLORS.put("A84300", "&6");
        COLORS.put("992D22", "&4");
        COLORS.put("979C9F", "&7");
        COLORS.put("546E7A", "&8");
    }

}
