package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandSendAnnouncement extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (Objects.requireNonNull(event.getMember()).getIdLong() != 211789389401948160L) {
            event.replyEmbeds(createQuickError("You dont have the permission to run this command."));
            return;
        }
        if (event.getArgs().length < 2) {
            event.replyEmbeds(createQuickError("No argument given."));
            return;
        }
        for (int i = 0; i < event.getJDA().getGuilds().size(); i++) {
            Objects.requireNonNull(event.getJDA().getGuilds().get(i).getDefaultChannel()).asStandardGuildMessageChannel().sendMessageEmbeds(createQuickEmbed("**Announcement**", event.getContentRaw().replace(event.getArgs()[0], ""))).queue();
            try {
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String getCategory() {
        return "dev";
    }

    @Override
    public String[] getNames() {
        return new String[]{"sendannouncement", "announcement", "announce"};
    }

    @Override
    public String getOptions() {
        return "Message";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "message", "The announcement to send.", true);
    }

    @Override
    public String getDescription() {
        return "sends an announcement globally.";
    }
}