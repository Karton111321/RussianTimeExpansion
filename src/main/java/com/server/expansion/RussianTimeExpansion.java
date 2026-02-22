package com.server.expansion;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RussianTimeExpansion extends PlaceholderExpansion implements Configurable {

    private final char[] smallCapsMap = new char[Character.MAX_VALUE + 1];
    private final Map<String, DateTimeFormatter> formattersCache = new ConcurrentHashMap<>();

    private ZoneId zoneId = ZoneId.of("Europe/Moscow");
    private String defaultFormatPattern = "dd.MM.yyyy HH:mm";

    public RussianTimeExpansion() {
        for (int i = 0; i < smallCapsMap.length; i++) {
            smallCapsMap[i] = (char) i;
        }

        char[][] mapping = {
                // Русский
                {'а', 'ᴀ'}, {'б', 'б'}, {'в', 'ʙ'}, {'г', 'ᴦ'}, {'д', 'д'},
                {'е', 'ᴇ'}, {'ё', 'ᴇ'}, {'ж', 'ж'}, {'з', 'ᴢ'}, {'и', 'и'},
                {'й', 'й'}, {'к', 'ᴋ'}, {'л', 'л'}, {'м', 'ᴍ'}, {'н', 'н'},
                {'о', 'ᴏ'}, {'п', 'п'}, {'р', 'ᴘ'}, {'с', 'ᴄ'}, {'т', 'ᴛ'},
                {'у', 'у'}, {'ф', 'ȹ'}, {'х', 'x'}, {'ц', 'ц'}, {'ч', 'ч'},
                {'ш', 'ш'}, {'щ', 'щ'}, {'ъ', 'ъ'}, {'ы', 'ы'}, {'ь', 'ь'},
                {'э', 'э'}, {'ю', 'ю'}, {'я', 'я'},

                // Английский
                {'a', 'ᴀ'}, {'b', 'ʙ'}, {'c', 'ᴄ'}, {'d', 'ᴅ'}, {'e', 'ᴇ'},
                {'f', 'ꜰ'}, {'g', 'ɢ'}, {'h', 'ʜ'}, {'i', 'ɪ'}, {'j', 'ᴊ'},
                {'k', 'ᴋ'}, {'l', 'ʟ'}, {'m', 'ᴍ'}, {'n', 'ɴ'}, {'o', 'ᴏ'},
                {'p', 'ᴘ'}, {'q', 'ǫ'}, {'r', 'ʀ'}, {'s', 'ꜱ'}, {'t', 'ᴛ'},
                {'u', 'ᴜ'}, {'v', 'ᴠ'}, {'w', 'ᴡ'}, {'x', 'x'}, {'y', 'ʏ'},
                {'z', 'ᴢ'}
        };

        for (char[] pair : mapping) {
            char normal = pair[0];
            char sc = pair[1];
            smallCapsMap[normal] = sc;
            smallCapsMap[Character.toUpperCase(normal)] = sc;
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rutime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "qweyns";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("timezone", "Europe/Moscow");
        defaults.put("default_format", "dd.MM.yyyy HH:mm");
        return defaults;
    }

    @Override
    public boolean canRegister() {
        String tz = getString("timezone", "Europe/Moscow");
        try {
            this.zoneId = ZoneId.of(tz);
        } catch (Exception e) {
            this.zoneId = ZoneId.of("Europe/Moscow");
        }
        this.defaultFormatPattern = getString("default_format", "dd.MM.yyyy HH:mm");
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.isEmpty()) return null;

        String text = PlaceholderAPI.setBracketPlaceholders(player, params);

        if (text.startsWith("format_")) {
            String pattern = text.substring(7);
            if (pattern.isEmpty()) pattern = defaultFormatPattern;
            return applySmallCaps(formatDate(pattern));
        }

        if (text.startsWith("caps_")) {
            String content = text.substring(5);
            String resolved = PlaceholderAPI.setPlaceholders(player, "%" + content + "%");

            if (!resolved.equals("%" + content + "%")) {
                return applySmallCaps(resolved);
            }
            return applySmallCaps(content);
        }

        if (text.equalsIgnoreCase("now")) {
            return applySmallCaps(formatDate(defaultFormatPattern));
        }

        return null;
    }

    private String formatDate(String pattern) {
        try {
            DateTimeFormatter formatter = formattersCache.computeIfAbsent(pattern,
                    p -> DateTimeFormatter.ofPattern(p, new Locale("ru")));
            return LocalDateTime.now(zoneId).format(formatter);
        } catch (IllegalArgumentException e) {
            return "<Ошибка формата>";
        }
    }

    private String applySmallCaps(String input) {
        if (input == null || input.isEmpty()) return input;

        int length = input.length();
        char[] buffer = new char[length];
        boolean insideMiniMessageTag = false;

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);

            if (c == '<') {
                int closeIndex = input.indexOf('>', i);
                if (closeIndex != -1) {
                    insideMiniMessageTag = true;
                }
            }

            if (insideMiniMessageTag) {
                buffer[i] = c;
                if (c == '>') {
                    insideMiniMessageTag = false;
                }
                continue;
            }

            if ((c == '&' || c == '§') && i + 7 < length && input.charAt(i + 1) == '#') {
                for (int j = 0; j < 8; j++) {
                    buffer[i + j] = input.charAt(i + j);
                }
                i += 7;
                continue;
            }

            if ((c == '&' || c == '§') && i + 1 < length) {
                char next = input.charAt(i + 1);
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(next) != -1) {
                    buffer[i] = c;
                    buffer[i + 1] = next;
                    i++;
                    continue;
                }
            }

            buffer[i] = (c < smallCapsMap.length) ? smallCapsMap[c] : c;
        }

        return new String(buffer);
    }
}
