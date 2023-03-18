package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.*;

public class CommandSendAnnouncement extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (Objects.requireNonNull(event.getMember()).getIdLong() != 211789389401948160L) {
            event.replyEmbeds(createQuickError("You dont have the permission to run this command."));
            return;
        }
        if (event.getArgs().length < 2) {
            event.replyEmbeds(createQuickError("No argument given."));
            return;
        }
        int i = 0;
        StringBuilder message = new StringBuilder();
        for (String string : event.getArgs()) {
            if (i == 0) {
                i++;
                continue;
            }
            message.append(string).append(" ");
            i++;
        }
        message.trimToSize();
        for (Guild guild : event.getJDA().getGuilds()) {
            try {
                Objects.requireNonNull(guild.getDefaultChannel()).asStandardGuildMessageChannel().sendMessageEmbeds(createQuickEmbed("**Announcement**", String.valueOf(message))).queue();
                Thread.sleep(10000);
            } catch (Exception ignored) {
                Objects.requireNonNull(event.getJDA().getUserById(guild.getOwnerId())).openPrivateChannel().queue(a -> a.sendMessage("The bot cannot message in the default channel of the server**" + guild.getName() + "** so I am dming you instead.").queue(embed -> embed.editMessageEmbeds(createQuickEmbed("**Announcement**", String.valueOf(message))).queue()));
            }
        }
    }

    @Override
    public String getCategory() {
        return "dev";
    }

    @Override
    public String[] getNames() {
        return new String[]{"sendannouncement", "announcement", "announce"};
    }

    @Override
    public String getOptions() {
        return "Message";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "message", "The announcement to send.", true);
    }

    @Override
    public String getDescription() {
        return "sends an announcement globally.";
    }
}