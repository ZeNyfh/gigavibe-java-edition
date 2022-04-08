package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

public class CommandDebug implements ICommand {
    @Override
    public void execute(ExecuteArgs event) {
        event.getTextChannel().sendMessage("debug").queue();
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String helpMessage() {
        return "just simple debug, dw";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
