package Bots;

import static Bots.Main.guildLocales;

// maybe this could become more than just a workaround for createQuickError and toTimestamp, but who knows.
public class LocaleMiddleman {
    private static final ThreadLocal<Long> threadedGuildID = new ThreadLocal<>();

    public static void setGuildID(Long guildID) {
        threadedGuildID.set(guildID);
    }

    public static String getLocalisedError() {
        Long guildID = threadedGuildID.get();
        if (guildID != null) {
            return guildLocales.get(guildID).get("Main.error");
        }
        // fallback to english word if something happens.
        return "Error";
    }

    public enum timeUnits {
        second, minute, hour, day
    }

    public static String getLocalisedTimeUnits(boolean plural, int unit) {
        Long guildID = threadedGuildID.get();

        String[] unitKeys = {"second", "minute", "hour", "day"};
        String key = "Main." + unitKeys[unit] + (plural ? ".plural" : "");
        String defaultText = "%1$s " + unitKeys[unit] + "s";

        if (guildID != null && guildLocales.containsKey(guildID)) {
            return guildLocales.get(guildID).get(key);
        }

        return defaultText;
    }

    public static void clear() {
        threadedGuildID.remove();
    }
}

