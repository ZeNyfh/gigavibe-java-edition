package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;

import static Bots.LocaleManager.languages;
import static Bots.Main.*;

public class CommandLocale extends BaseCommand {

    @Override
    public void execute(CommandEvent event) throws Exception {
        if (event.getArgs().length == 1) {
            event.replyEmbeds(createQuickEmbed("List: ", "- English\n- Polski\n- Nederlands\n- Dansk"));
        } else {
            String lang = event.getArgs()[1].toLowerCase();
            if (languages.containsKey(lang)) {
                event.getConfig().put("Locale", lang);
                guildLocales.put(event.getGuild().getIdLong(), languages.get(lang));
                event.replyEmbeds(createQuickEmbed("Success", "Language changed to " + lang));
            } else {
                event.replyEmbeds(createQuickError("Unrecognised language"));
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"language", "locale"}; // language is more user-friendly.
    }

    @Override
    public Category getCategory() {
        return Category.Admin;
    }

    @Override
    public String getDescription() {
        return "Changes the language of Zenvibe for the server";
    }
}
