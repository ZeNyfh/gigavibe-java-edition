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
            event.replyEmbeds(createQuickError("Please provide something to report."));
            return;
        }
        Objects.requireNonNull(event.getJDA().getUserById("211789389401948160")).openPrivateChannel().queue(a -> a.sendMessage("------------------------------\n" + event.getUser().getName() + "\n\n" + event.getContentRaw().split(" ", 2)[1]).queue());
        if (event.isSlash()) {
            event.deferReply(true); //hacky way of making it ephemeral
        }
        event.reply("Thanks for sending in a bug report!");
    }

    @Override
    public String[] getNames() {
        return new String[]{"bug"};
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String getDescription() {
        return "Sends a bug report to the developer.";
    }

    @Override
    public String getOptions() {
        return "<Message>";
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
