package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import org.json.simple.JSONObject;

import static Bots.CommandEvent.createQuickSuccess;
import static Bots.LocaleManager.languages;
import static Bots.LocaleManager.managerLocalise;
import static Bots.Main.createQuickEmbed;
import static Bots.Main.guildLocales;

public class CommandLocale extends BaseCommand {

    @Override
    public void execute(CommandEvent event) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (String langName : languages.keySet()) {
            builder.append(String.format("- %s\n", Character.toUpperCase(langName.charAt(0)) + langName.substring(1)));
        }
        String languagesString = builder.toString().trim();

        if (event.getArgs().length == 1) {
            event.replyEmbeds(createQuickEmbed(event.localise("cmd.loc.list"), languagesString));
        } else {
            String lang = event.getArgs()[1].toLowerCase();
            if (languages.containsKey(lang)) {
                JSONObject config = event.getConfig();
                config.put("Locale", lang);
                guildLocales.put(event.getGuild().getIdLong(), languages.get(lang));
                event.replyEmbeds(createQuickSuccess(managerLocalise("cmd.loc.languageChanged", languages.get(lang), lang), languages.get(lang)));
            } else {
                event.replyEmbeds(event.createQuickError(event.localise("cmd.loc.unrecognised") + "\n" + languagesString));
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"language", "locale", "lang"}; // language is more user-friendly.
    }

    @Override
    public Category getCategory() {
        return Category.Admin;
    }

    @Override
    public String getDescription() {
        return "Changes the language of Zenvibe for the server";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}
