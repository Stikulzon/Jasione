package me.decce.transformingbase.core;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {
    private static CommentedFileConfig makeNightConfig(Path configFile) {
        return CommentedFileConfig.builder(configFile, TomlFormat.instance())
                .preserveInsertionOrder()
                .sync()
                .build();
    }

    public static void save(Path configFile, JasioneConfig config) {
        try (var night = toNightConfig(configFile, config)) {
            Files.createDirectories(configFile.getParent());
            night.save();
        } catch (Exception e) {
            Jasione.LOGGER.error("Failed to save configuration at {}", configFile.toAbsolutePath(), e);
        }
    }

    public static JasioneConfig load(Path configFile) {
        if (Files.exists(configFile)) {
            try {
                return fromNightConfig(configFile);
            } catch (Exception e) {
                Jasione.LOGGER.error("Failed to read configuration at {}", configFile.toAbsolutePath(), e);
            }
        }
        return new JasioneConfig();
    }

    private static CommentedFileConfig toNightConfig(Path configFile, JasioneConfig config) {
        var night = makeNightConfig(configFile);
        try {
            for (Field field : JasioneConfig.class.getDeclaredFields()) {
                var modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }
                String key = field.getName();
                if (field.isAnnotationPresent(JasioneConfig.Key.class)) {
                    key = field.getAnnotation(JasioneConfig.Key.class).value();
                }
                night.set(key, field.get(config));
                if (field.isAnnotationPresent(JasioneConfig.Comment.class)) {
                    night.setComment(key, field.getAnnotation(JasioneConfig.Comment.class).value());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return night;
    }

    private static JasioneConfig fromNightConfig(Path configFile) {
        var config = new JasioneConfig();
        try (var night = makeNightConfig(configFile)) {
            night.load();
            for (Field field : JasioneConfig.class.getDeclaredFields()) {
                var modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }
                String key = field.getName();
                if (field.isAnnotationPresent(JasioneConfig.Key.class)) {
                    key = field.getAnnotation(JasioneConfig.Key.class).value();
                }
                if (night.contains(key)) {
                    field.set(config, night.get(key));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return config;
    }
}
