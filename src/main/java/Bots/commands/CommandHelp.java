package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

public class CommandHelp implements ICommand {
    @Override
    public void execute(ExecuteArgs event) {
        event.getTextChannel().sendMessage("literally only the help command rn").queue(); // will be worked on in the future
        //ArrayList General = new ArrayList();
        //ArrayList Music = new ArrayList();
        //ArrayList DJ = new ArrayList();
        //ArrayList Admin = new ArrayList();
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
