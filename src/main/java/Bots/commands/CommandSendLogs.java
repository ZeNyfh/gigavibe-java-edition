package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker.Check;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandSendLogs extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DEV};
    }

    @Override
    public void execute(CommandEvent event) throws IOException {
        if (event.getArgs().length == 1) {
            event.replyEmbeds(createQuickError("No arguments specified"));
            return;
        }

        if (event.getArgs()[1].equalsIgnoreCase("list")) {
            Stream<Path> stream = Files.list(Paths.get(new File("logs/").toURI()));
            StringBuilder builder = new StringBuilder();
            for (Path file : stream.toList()) {
                String filename = file.getFileName().toString().split("\\.")[0];
                if (!filename.equalsIgnoreCase("log"))
                    builder.append("* `").append(filename).append(" | ").append(file.toFile().length()).append("KB`\n");
            }
            event.replyEmbeds(createQuickEmbed("Log files", builder.toString()));
        } else if (event.getArgs()[1].equalsIgnoreCase("send")) {
            File file;
            if (event.getArgs().length == 2 || event.getArgs()[2].equalsIgnoreCase("log")) {
                file = new File("logs/log.log");
            } else {
                file = new File("logs/" + event.getArgs()[2] + ".zip");
                if (!file.exists()) {
                    event.replyEmbeds(createQuickError("Specified log file does not exist"));
                    return;
                }
            }
            event.replyFiles(FileUpload.fromData(file));
        } else {
            event.replyEmbeds(createQuickError("Not a valid option for SendLogs"));
        }
    }

    @Override
    public Category getCategory() {
        return Category.Dev;
    }

    @Override
    public String[] getNames() {
        return new String[]{"logs"};
    }

    @Override
    public String getDescription() {
        return "Gets or sends specific logs.";
    }

    @Override
    public String getOptions() {
        return "list | send [name]";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("list", "Lists the logs."),
                new SubcommandData("send", "Sends a log.").addOptions(
                        new OptionData(OptionType.STRING, "name", "The name of the log. None for current.", false)
                )
        );
    }
}
