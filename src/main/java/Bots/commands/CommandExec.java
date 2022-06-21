package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;

import java.util.List;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandExec extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        String args = event.getMessage().getContentRaw();
        args = args.replaceAll("&exec ","");
        long id = 211789389401948160L;
        if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
            id = 260016427900076033L;
            if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You dont have the permission to run this command.")).queue();
                return;
            }
        }
        try {
            JShell.create().eval(args);
            event.getTextChannel().sendMessage("\uD83D\uDC4D\n\n").queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", e.getMessage())).queue();
        }
    }


    @Override
    public String getName() {
        return "exec";
    }

    @Override
    public String getCategory() {
        return "Dev";
    }

    @Override
    public String getDescription() {
        return "**- evaluates code.";
    }
}
