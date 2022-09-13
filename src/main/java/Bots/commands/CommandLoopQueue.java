package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;

import static Bots.Main.*;

public class CommandLoopQueue extends BaseCommand {
    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();

        if (!audioManager.isConnected()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("I am not playing anything.")).queue();
            return;
        }

        if (LoopQueueGuilds.contains(event.getGuild().getId())) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current queue.")).queue();
            LoopQueueGuilds.remove(event.getGuild().getId());
        } else {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current queue.")).queue();
            LoopQueueGuilds.add(event.getGuild().getId());
        }
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("loopq");
        return list;
    }

    public String getName() {
        return "loopqueue";
    }

    public String getCategory() {
        return "Music";
    }

    public String getDescription() {
        return "Loops the current queue.";
    }

    public long getTimeout() {
        return 2500;
    }
}
