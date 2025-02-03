package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import org.json.simple.JSONObject;

import static Bots.CommandEvent.localise;
import static Bots.LocaleManager.languages;
import static Bots.LocaleManager.managerLocalise;
import static Bots.Main.*;

public class CommandLocale extends BaseCommand {

    @Override
    public void execute(CommandEvent event) throws Exception {
        if (event.getArgs().length == 1) {
            event.replyEmbeds(createQuickEmbed(localise("cmd.loc.list"), "- English\n- Polski\n- Nederlands\n- Dansk\n- Español"));
        } else {
            String lang = event.getArgs()[1].toLowerCase();
            if (languages.containsKey(lang)) {
                JSONObject config = event.getConfig();
                config.put("Locale", lang);
                guildLocales.put(event.getGuild().getIdLong(), languages.get(lang));
                event.replyEmbeds(createQuickSuccess(managerLocalise("cmd.loc.languageChanged", languages.get(lang), lang)));
            } else {
                event.replyEmbeds(createQuickError(localise("cmd.loc.unrecognised") + "\n- English\n- Polski\n- Nederlands\n- Dansk\n- Español"));
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
