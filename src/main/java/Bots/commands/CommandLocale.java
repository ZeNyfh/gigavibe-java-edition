package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.simple.JSONObject;

import java.util.Objects;

import static Bots.CommandEvent.createQuickError;
import static Bots.CommandEvent.createQuickSuccess;
import static Bots.GuildDataManager.GetGuildConfig;
import static Bots.LocaleManager.languages;
import static Bots.LocaleManager.managerLocalise;
import static Bots.Main.*;

public class CommandLocale extends BaseCommand {

    private void HandleSelectionEvent(StringSelectInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.replyEmbeds(createQuickError("I currently do not work outside of discord servers.", null)).queue();
            return;
        }

        String label = event.getInteraction().getSelectedOptions().get(0).getValue();

        JSONObject config = GetGuildConfig(Objects.requireNonNull(event.getGuild()).getIdLong());
        config.put("Locale", label);
        guildLocales.put(event.getGuild().getIdLong(), languages.get(label));
        event.replyEmbeds(createQuickSuccess(managerLocalise("cmd.loc.languageChanged", languages.get(label), label), languages.get(label))).queue();
    }

    @Override
    public void execute(CommandEvent event) throws Exception {
        StringBuilder builder = new StringBuilder();
        StringSelectMenu.Builder menu = StringSelectMenu.create("langlist");
        for (String langName : languages.keySet()) {
            menu.addOption(languages.get(langName).get("main.flag") + " " +  langName, langName.toLowerCase());
            builder.append(String.format("- %s %s\n", languages.get(langName).get("main.flag"), Character.toUpperCase(langName.charAt(0)) + langName.substring(1)));
        }
        String languagesString = builder.toString().trim();
        event.replyEmbeds(response -> response.setActionRow(
                menu.build()), createQuickEmbed(event.localise("cmd.loc.list"), languagesString));
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
    public CommandStateChecker.Check[] getChecks() {
        return new CommandStateChecker.Check[]{CommandStateChecker.Check.IS_DJ};
    }

    @Override
    public String getDescription() {
        return "Changes the language of Zenvibe for the server";
    }

    @Override
    public void Init() {
        registerSelectionInteraction(new String[]{"langlist"}, this::HandleSelectionEvent);
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}
