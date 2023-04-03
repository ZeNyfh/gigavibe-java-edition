package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.io.IOException;

public class CommandSetAnnouncement extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {

    }

    @Override
    public String[] getNames() {
        return new String[]{"setannouncement","setannounce","seta"};
    }

    @Override
    public String getCategory() {
        return "Dev";
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Sets the announcement channel.";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}
