package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.util.Objects;

import static Bots.Main.*;

public class CommandJoin extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
            event.replyEmbeds(createQuickError("You are not in a vc."));
            return;
        }
        event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
        event.replyEmbeds(createQuickEmbed(" ", "✅ Joined your vc."));

    }

    @Override
    public String[] getNames() {
        return new String[]{"connect", "join"};
    }

    @Override
    public String getOptions() {
        return "";
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
    public long getRatelimit() {
        return 5000;
    }
}
