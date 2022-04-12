package Bots;

import ca.tristan.jdacommands.ICommand;
import ca.tristan.jdacommands.JDACommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static Bots.Main.botPrefix;

public class CommandHandler extends ListenerAdapter {

    private final List<ICommand> commands;

    public CommandHandler(JDACommands jdaCommands) {
        this.commands = jdaCommands.getCommands();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("message found");
        if (event.getMessage().getContentRaw().startsWith(botPrefix)){
            System.out.println("prefix found");
            commands.forEach(iCommand -> {
                System.out.println(iCommand);
                if (event.getMessage().getContentRaw().startsWith(botPrefix + iCommand)) {
                    System.out.println("your command is: " + iCommand);
                }
            });
        }
    }
}