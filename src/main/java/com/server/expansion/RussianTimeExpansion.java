package com.server.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RussianTimeExpansion extends PlaceholderExpansion {

    private final Map<Character, String> smallCapsMap = new HashMap<>();

    public RussianTimeExpansion() {
        String[][] mapping = {
                {"а", "ᴀ"}, {"б", "б"}, {"в", "ʙ"}, {"г", "ᴦ"}, {"д", "д"},
                {"е", "ᴇ"}, {"ё", "ᴇ"}, {"ж", "ж"}, {"з", "ᴢ"}, {"и", "и"},
                {"й", "й"}, {"к", "ᴋ"}, {"л", "л"}, {"м", "ᴍ"}, {"н", "н"},
                {"о", "ᴏ"}, {"п", "п"}, {"р", "ᴘ"}, {"с", "ᴄ"}, {"т", "ᴛ"},
                {"у", "у"}, {"ф", "ȹ"}, {"х", "x"}, {"ц", "ц"}, {"ч", "ч"},
                {"ш", "ш"}, {"щ", "щ"}, {"ъ", "ъ"}, {"ы", "ы"}, {"ь", "ь"},
                {"э", "э"}, {"ю", "ю"}, {"я", "я"}
        };

        for (String[] pair : mapping) {
            char normal = pair[0].charAt(0);
            String sc = pair[1];
            smallCapsMap.put(normal, sc);
            smallCapsMap.put(Character.toUpperCase(normal), sc);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rutime"; // 
    }

    @Override
    public @NotNull String getAuthor() {
        return "Gemini";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; 
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(params, new Locale("ru"));
            String dateStr = sdf.format(new Date());

            StringBuilder result = new StringBuilder();
            for (char c : dateStr.toCharArray()) {
                result.append(smallCapsMap.getOrDefault(c, String.valueOf(c)));
            }

            return result.toString();
        } catch (Exception e) {
            return "Ошибка формата";
        }
    }
}