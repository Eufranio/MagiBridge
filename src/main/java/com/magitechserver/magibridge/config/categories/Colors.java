package com.magitechserver.magibridge.config.categories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class Colors extends ConfigCategory {

    private Map<String, String> defaultValues;

    public Colors() {
        defaultValues = new HashMap<>();

        defaultValues.put("99AAB5", "&f");
        defaultValues.put("1ABC9C", "&a");
        defaultValues.put("2ECC71", "&a");
        defaultValues.put("3498DB", "&3");
        defaultValues.put("9B59B6", "&5");
        defaultValues.put("E91E63", "&d");
        defaultValues.put("F1C40F", "&e");
        defaultValues.put("E67E22", "&6");
        defaultValues.put("E74C3C", "&c");
        defaultValues.put("95A5A6", "&7");
        defaultValues.put("607D8B", "&8");
        defaultValues.put("11806A", "&2");
        defaultValues.put("1F8B4C", "&2");
        defaultValues.put("206694", "&1");
        defaultValues.put("71368A", "&5");
        defaultValues.put("AD1457", "&d");
        defaultValues.put("C27C0E", "&6");
        defaultValues.put("A84300", "&6");
        defaultValues.put("992D22", "&4");
        defaultValues.put("979C9F", "&7");
        defaultValues.put("546E7A", "&8");
    }

    @Setting(value = "discord-to-mc-replacer", comment = "Conversor from RGB to in-game colors (for %toprolecolor%)" +
            "Format: \"RGB\" = \"IN-GAME COLOR\"")
    public Map<String, String> COLORS = defaultValues;

}
