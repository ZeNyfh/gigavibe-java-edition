package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.IsDJ;
import static Bots.Main.createQuickEmbed;

public class CommandJoin extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getTextChannel(), event.getMember())) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You are not dj.")).queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **error**", "You are not in a vc.")).queue();
            return;
        }
        event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Joined your vc.")).queue();

    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("join");
        return list;
    }

    @Override
    public String getName() {
        return "connect";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Makes the bot forcefully join your vc.";
    }

    @Override
    public long getTimeout() {
        return 5000;
    }
}
