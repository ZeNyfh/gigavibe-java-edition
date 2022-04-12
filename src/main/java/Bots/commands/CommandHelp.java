package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

public class CommandHelp implements ICommand {
    public static final CommandLoop CLoop = new CommandLoop();
    @Override
    public void execute(ExecuteArgs event) {
        event.getTextChannel().sendMessage("literally only the help command rn").queue(); // will be worked on in the future
        CLoop.getCategory()
    }

    public String getCategory() {
        return "General";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String helpMessage() {
        return "Shows you a list of commands.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
