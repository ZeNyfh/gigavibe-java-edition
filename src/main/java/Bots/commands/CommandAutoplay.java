package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.AutoplayHelper;
import Bots.lavaplayer.LastFMManager;
import static Bots.Main.*;

public class CommandAutoplay extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC, Check.IS_PLAYING};
    }

    @Override
    public void execute(MessageEvent event) {
        if (!LastFMManager.isInitialized()) {
            event.replyEmbeds(createQuickError("The bot has not been given an API key for LastFM, this command does not work without it."));
            return;
        }
        if (AutoplayHelper.includes(event.getGuild().getIdLong())) {
            event.replyEmbeds(createQuickEmbed("❌ ♾\uFE0F", "No longer autoplaying."));
            AutoplayHelper.remove(event.getGuild().getIdLong());
        } else {
            event.replyEmbeds(createQuickEmbed("✅ ♾\uFE0F", "Now autoplaying."));
            AutoplayHelper.add(event.getGuild().getIdLong());
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"autoplay", "ap"};
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getDescription() {
        return "Toggles autoplay.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}