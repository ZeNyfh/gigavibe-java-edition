package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.util.Objects;

import static Bots.Main.createQuickError;

public class CommandBug extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (event.getArgs().length == 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Please provide something to report.")).queue();
            return;
        }
        Objects.requireNonNull(event.getJDA().getUserById("211789389401948160")).openPrivateChannel().queue(a -> a.sendMessage("------------------------------\n" + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + "\n\n" + event.getMessage().getContentRaw()).queue());
        event.getChannel().asTextChannel().sendMessage("Thanks for sending in a bug report!\nYou may receive a friend request or a DM from the developer. Otherwise, a github issue will be made or your bug report was ignored.").queue();
    }

    public String getName() {
        return "bug";
    }

    public String getCategory() {
        return "General";
    }

    public String getDescription() {
        return "Sends a bug report to the developer.";
    }

    public long getTimeout() {
        return 60000;
    }
}
