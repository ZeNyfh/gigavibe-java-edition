package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static Bots.LocaleManager.languages;
import static Bots.Main.createQuickEmbed;
import static Bots.Main.guildLocales;

public class CommandLocale extends BaseCommand {

    @Override
    public void execute(CommandEvent event) throws Exception {
        if (event.getArgs().length == 0) {
            event.replyEmbeds(createQuickEmbed("List: ", "- English\n- Polski\n- Nederlands\n- Dansk"));
        } else {
            JSONArray locale = (JSONArray) event.getConfig().get("Locale");
            String lang = event.getArgs()[0].toLowerCase();
            if (languages.containsKey(lang)) {
                locale.clear();
                locale.add(lang);
                guildLocales.put(event.getGuild().getIdLong(), languages.get(lang));
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
