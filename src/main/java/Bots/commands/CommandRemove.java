package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker.Check;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandRemove extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC};
    }

    @Override
    public void execute(CommandEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            event.replyEmbeds(createQuickError(event.getLocaleString("CommandRemove.noSongs")));
            return;
        }
        if (event.getArgs().length == 1 || !event.getArgs()[1].matches("^\\d+$")) {
            event.replyEmbeds(createQuickError(event.getLocaleString("CommandRemove.invalidArgs")));
            return;
        }
        int position = Integer.parseInt(event.getArgs()[1]);
        if (queue.size() < position - 1) {
            event.replyEmbeds(createQuickError(event.getLocaleString("CommandRemove.tooLarge")));
            return;
        }
        musicManager.scheduler.queue.clear();
        queue.remove(position - 1);
        for (AudioTrack audioTrack : queue) {
            musicManager.scheduler.queue(audioTrack.makeClone());
        }
        event.replyEmbeds(createQuickEmbed(" ", "✅" + String.format(event.getLocaleString("CommandRemove.removed"), "**" + position + "**")));
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "position", "The track to remove from the position in the queue.", true);
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getOptions() {
        return "<Queue_Position>";
    }

    @Override
    public String[] getNames() {
        return new String[]{"remove", "rem"};
    }

    @Override
    public String getDescription() {
        return "Removes a specified track from the queue.";
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
