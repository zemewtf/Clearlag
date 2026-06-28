package me.minebuilders.clearlag.language;

import me.minebuilders.clearlag.Clearlag;
import me.minebuilders.clearlag.Util;
import me.minebuilders.clearlag.annotations.AutoWire;
import me.minebuilders.clearlag.config.ConfigHandler;
import me.minebuilders.clearlag.language.messages.Message;
import me.minebuilders.clearlag.modules.BroadcastHandler;
import me.minebuilders.clearlag.modules.ClearlagModule;

/**
 * @author bob7l
 */
public class LanguageManager extends ClearlagModule {

    @AutoWire
    private BroadcastHandler broadcastHandler;

    @AutoWire
    private ConfigHandler config;

    private LanguageLoader languageLoader;

    public Message getMessage(String key) {
        return languageLoader.getMessageByKey(key);
    }

    public LanguageLoader getLanguageLoader() {
        return languageLoader;
    }

    @Override
    public void setEnabled() {
        super.setEnabled();

        String desiredLanguage = config.getConfig().getString("settings.language") + ".lang";

        desiredLanguage = desiredLanguage.substring(0, 1).toUpperCase() + desiredLanguage.substring(1);

        languageLoader = new LanguageLoader(broadcastHandler);

        // Copy default lang files to data folder
        java.io.File languagesFolder = new java.io.File(Clearlag.getInstance().getDataFolder(), "languages");
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();
        }

        String[] langFiles = {
            "English.lang", "BrazilianPortuguese.lang", "ChineseSimplified.lang", 
            "ChineseTraditional.lang", "Czech.lang", "French.lang", "German.lang", 
            "Japanese.lang", "Korean.lang", "Polish.lang", "Russian.lang", "Spanish.lang"
        };

        for (String file : langFiles) {
            java.io.File outFile = new java.io.File(languagesFolder, file);
            if (!outFile.exists()) {
                try (java.io.InputStream in = Clearlag.class.getResourceAsStream("/languages/" + file)) {
                    if (in != null) {
                        java.nio.file.Files.copy(in, outFile.toPath());
                    }
                } catch (Exception e) {
                    Util.warning("Failed to save language file: " + file);
                }
            }
        }

        // Load the language file from data folder first, falling back to JAR
        java.io.File langFile = new java.io.File(languagesFolder, desiredLanguage);
        if (langFile.exists()) {
            try {
                languageLoader.setLanguageMap(new java.io.FileInputStream(langFile));
            } catch (Exception e) {
                Util.warning("Failed to load language from file " + desiredLanguage + ", trying JAR resource...");
                loadFromJar(desiredLanguage);
            }
        } else {
            loadFromJar(desiredLanguage);
        }

        for (Object object : Clearlag.getInstance().getAutoWirer().getWires()) {

            try {
                languageLoader.wireInMessages(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFromJar(String desiredLanguage) {
        try {
            languageLoader.setLanguageMap(Clearlag.class.getResource("/languages/" + desiredLanguage).openStream());
        } catch (Exception e) {
            Util.warning("Clearlag FAILED to find your desired language file '" + desiredLanguage + "'. Defaulting to English...");
            try {
                languageLoader.setLanguageMap(Clearlag.class.getResource("/languages/English.lang").openStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
