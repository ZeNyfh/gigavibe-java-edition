package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.createQuickError;

public class CommandBug extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (event.getArgs().length == 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Please provide something to report.")).queue();
            return;
        }
        Objects.requireNonNull(event.getJDA().getTextChannelById(1055224772092506163L)).sendMessage("------------------------------\n" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "\n\n" + event.getContentRaw()).queue();
        event.reply("Thanks for sending in a bug report!\nYou may receive a friend request or a DM from the developer. Otherwise, a github issue will be made or your bug report was ignored.");
    }

    @Override
    public String[] getNames() {
        return new String[]{"bug"};
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getDescription() {
        return "Sends a bug report to the developer.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "message", "The bug report", true);
    }

    @Override
    public long getRatelimit() {
        return 60000;
    }
}
