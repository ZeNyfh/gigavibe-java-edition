package Bots;

import static Bots.Main.guildLocales;

// maybe this could become more than just a workaround for createQuickError and toTimestamp, but who knows.
public class LocaleMiddleman {
    private static final ThreadLocal<Long> threadedGuildID = new ThreadLocal<>();

    public static void setGuildID(Long guildID) {
        threadedGuildID.set(guildID);
    }

    public static String getLocalisedSuccess() {
        Long guildID = threadedGuildID.get();
        if (guildID != null) {
            return guildLocales.get(guildID).get("main.success");
        }
        // fallback to english word if something happens.
        return "Success";
    }

    public static String getLocalisedError() {
        Long guildID = threadedGuildID.get();
        if (guildID != null) {
            return guildLocales.get(guildID).get("main.error");
        }
        // fallback to english word if something happens.
        return "Error";
    }

    public enum timeUnits {
        second, minute, hour, day
    }

    public static String getLocalisedTimeUnits(boolean plural, int unit, long guildID) {
        String[] unitKeys = {"second", "minute", "hour", "day"};
        String key = "main." + unitKeys[unit] + (plural ? ".plural" : "");
        return guildLocales.get(guildID).get(key);
    }

    public static void clear() {
        threadedGuildID.remove();
    }
}

