package com.magitechserver.magibridge.util;

import com.google.common.collect.Iterables;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.categories.Messages;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextHelper {

    // This pattern was copied from Nucleus:
    // https://github.com/NucleusPowered/Nucleus/blob/34e1d279d59fc22333ae3e38a6db8dcae64cee2c/nucleus-core/src/main/java/io/github/nucleuspowered/nucleus/services/impl/textstyle/TextStyleService.java#L52
    private static final Pattern URL_PARSER =
            Pattern.compile("(?<url>(http(s)?://)?([A-Za-z0-9-]+\\.)+[A-Za-z0-9]{2,}\\S*)",
                    Pattern.CASE_INSENSITIVE
            );

    public static Text replaceLink(Text text) {
        Messages.Link linkConfig = MagiBridge.getInstance().getConfig().MESSAGES.LINK_FORMAT;
        List<Text> hover = linkConfig.HOVER.stream().map(Utils::toText).collect(Collectors.toList());
        Text hoverText = Text.joinWith(Text.NEW_LINE, hover);

        Function<String, Text> toReplace = urlString -> {
            System.out.println(urlString);
            URL url = null;
            try {
                String urlStringWithHttp = urlString.startsWith("http") ?
                        urlString :
                        "http://" + urlString;
                url = new URL(urlStringWithHttp);
            } catch (MalformedURLException e) {}

            String format = linkConfig.FORMAT.replace("%url%", urlString);
            if (linkConfig.limit_length && format.length() > linkConfig.max_length) {
                format = format.substring(0, linkConfig.max_length) + "...";
            }

            return Utils.toText(format)
                    .toBuilder()
                    .onHover(TextActions.showText(hoverText))
                    .onClick(url != null ? TextActions.openUrl(url) : null)
                    .build();
        };

        return replace(URL_PARSER, text, toReplace, false);
    }

    public static Text replace(Text text, Text oldValue, Text newValue) {
        return replace(Pattern.compile(oldValue.toPlain(), Pattern.LITERAL), text, val -> newValue, true);
    }

    public static Text replace(Pattern pattern, Text thisText, Function<String, Text> toReplaceFunction, boolean lossy) {
        // recursively call the function on child elements and produce something ready to return
        Text text = thisText.getChildren().isEmpty() ?
                thisText :
                thisText.toBuilder().removeAll().append(
                        thisText.getChildren().stream()
                                .map(child -> replace(pattern, child, toReplaceFunction, lossy))
                                .collect(Collectors.toList())
                ).build();
        String plain = text.toPlainSingle();
        Matcher matcher = pattern.matcher(plain);
        if (!matcher.find()) {
            if (lossy) {
                // will assimilating children find it?
                plain = text.toPlain();
                matcher = pattern.matcher(plain);
                if (matcher.find()) {
                    // lossy mode required
                    text = text.toBuilder().removeAll().build();
                } else {
                    return text;
                }
            } else {
                return text;
            }
        }
        if (matcher.matches()) {
            // the entire component matches; no replacement necessary
            return reformat(thisText, Text.builder()).append(toReplaceFunction.apply(plain)).append(text.getChildren()).build();
        }
        Text.Builder builder = Text.builder();
        // split and interleave
        List<String> strs = Arrays.asList(pattern.split(plain, -1));
        for (String str : Iterables.limit(strs, strs.size() - 1)) {
            builder.append(Text.of(str));
            if (matcher.find()) {
                builder.append(toReplaceFunction.apply(matcher.group()));
            } else {
                builder.append(toReplaceFunction.apply(plain));
            }
        }
        builder.append(Text.of(strs.get(strs.size() - 1))).append(text.getChildren());
        return reformat(thisText, builder).build();
    }

    static Text.Builder reformat(Text thisText, Text.Builder builder) {
        builder.format(thisText.getFormat());
        thisText.getClickAction().ifPresent(builder::onClick);
        thisText.getShiftClickAction().ifPresent(builder::onShiftClick);
        thisText.getHoverAction().ifPresent(builder::onHover);
        return builder;
    }

}
