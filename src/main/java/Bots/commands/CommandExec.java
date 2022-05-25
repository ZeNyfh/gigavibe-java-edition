package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import jdk.jshell.JShell;

import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandExec extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        long id = 211789389401948160L;
        if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
            id = 260016427900076033L;
            if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You dont have the permission to run this command.")).queue();
                return;
            }
        }
        String string = event.getMessage().getContentRaw();
        String[] args = string.split(" ", 2);
        try {
            JShell.create().eval(args[1]);
            event.getTextChannel().sendMessage("\uD83D\uDC4D\n\n").queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", String.valueOf(e))).queue();
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