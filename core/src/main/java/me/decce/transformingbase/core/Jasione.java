package me.decce.transformingbase.core;

import me.decce.transformingbase.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Jasione {
    public static final Logger LOGGER = LogManager.getLogger();
    public static JasioneConfig config;

    public static JasioneConfig getConfig() {
        return getConfig(Path.of("config"));
    }

    public static JasioneConfig getConfig(Path configDirectory) {
        if (config == null) {
            config = loadConfig(configDirectory.resolve(Constants.MOD_ID + ".toml"));
            cleanup();
        }
        return config;
    }

    private static JasioneConfig loadConfig(Path configFile) {
        config = ConfigLoader.load(configFile);
        ConfigLoader.save(configFile, config);
        return config;
    }

    private static void cleanup() {
        // Cleanup previously dumped classes
        if (Files.exists(Constants.OUTPUT_PATH)) {
            try (var stream = Files.walk(Constants.OUTPUT_PATH)) {
                stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            } catch (IOException ignored) {
            }
        }
    }
}
