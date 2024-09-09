package Bots;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.guildLocales;

public class LocaleManager {
    File file = new File("locales/en.txt");
    HashMap<String, String> english = readLocale("locales/en.txt");


    public HashMap<String, String> readLocale(String localeFile) {
        File file = new File(localeFile);
        List<String> lines = new ArrayList<>();
        try {
           lines = Files.readAllLines(Path.of(file.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, String> localeMap = new HashMap<>();

        for (String line : lines) {
            String[] lineSplit = line.split("=");
            Objects.requireNonNull(english).put(lineSplit[0], lineSplit[1]);
        }
        return localeMap;
    }

    Pattern serializePattern = Pattern.compile("\\{(\\d+)}");
    public String serializeString(String localeInput) {
        Matcher matcher = serializePattern.matcher(localeInput);
        for (int i = 0; i > matcher.groupCount(); i++) {
            localeInput = localeInput.replaceAll(matcher.group(i), "%" + matcher.group(i) + "$s");
        }
        return localeInput;
    }
}
