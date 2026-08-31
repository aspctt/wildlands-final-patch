package com.aspct.wildlandspatch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's configuration, and the registry of individual fix toggles.
 *
 * <p>Every fix gets its own switch. A patch mod is the thing you disable first when something in
 * the pack misbehaves, and a per-fix switch means that can be one line in a config file instead of
 * pulling the JAR and losing every other fix with it. Declare a fix with {@link #fix}, keep the
 * returned key in the class that implements it, and gate the fix on {@link #enabled(String)}.
 */
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Fix key to config value, in declaration order. */
    private static final Map<String, ModConfigSpec.BooleanValue> TOGGLES = new LinkedHashMap<>();

    /** Fix key to default, read when the config has not been loaded yet. See {@link #enabled}. */
    private static final Map<String, Boolean> DEFAULTS = new LinkedHashMap<>();

    public static final ModConfigSpec.BooleanValue LOG_APPLIED_FIXES = BUILDER
            .comment("Log which fixes are active during startup. Leave this on: it is what makes a log",
                     "from a player enough to tell whether a fix was even running.")
            .define("general.logAppliedFixes", true);

    // ---------------------------------------------------------------------------------------
    // Fix toggles. One line per fix, added as fixes land, for example:
    //
    //   public static final String SOME_MOD_DUPED_DROPS =
    //           fix("someModDupedDrops", "Stops <mod> dropping its block twice when broken by <other mod>.", true);
    //
    // Keep the comment aimed at a player reading the config file: what breaks without the fix,
    // and which mods are involved.
    // ---------------------------------------------------------------------------------------

    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Whether the config file has been read. Mixins can run against classes loaded long before
     * config loading happens, so a fix that consults its toggle that early would otherwise crash.
     */
    private static volatile boolean loaded;

    private Config() {
    }

    static void register(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, SPEC);

        // Both events are fired on the owning mod's bus, so these only ever see our own config.
        modEventBus.addListener((ModConfigEvent.Loading event) -> loaded = true);
        modEventBus.addListener((ModConfigEvent.Reloading event) -> loaded = true);
    }

    /**
     * Declares a fix toggle and returns its key.
     *
     * @param key          config key, camelCase, unique across fixes
     * @param comment      what the fix does, written for whoever is reading the config file
     * @param defaultValue whether the fix is on out of the box
     */
    @SuppressWarnings("unused") // Called from this class's field initialisers as fixes are added.
    private static String fix(String key, String comment, boolean defaultValue) {
        TOGGLES.put(key, BUILDER.comment(comment).define("fixes." + key, defaultValue));
        DEFAULTS.put(key, defaultValue);
        return key;
    }

    /**
     * Whether the given fix should run. Falls back to the fix's default if the config has not been
     * loaded yet, so this is safe to call from anywhere, including during class transformation.
     *
     * @throws IllegalArgumentException if the key was never declared with {@link #fix}
     */
    public static boolean enabled(String key) {
        ModConfigSpec.BooleanValue value = TOGGLES.get(key);
        if (value == null) {
            throw new IllegalArgumentException("No fix is registered under the key '" + key + "'");
        }
        return loaded ? value.get() : DEFAULTS.get(key);
    }

    /** The declared fix keys, in declaration order. */
    public static Map<String, ModConfigSpec.BooleanValue> toggles() {
        return Collections.unmodifiableMap(TOGGLES);
    }

    static void logFixes() {
        if (TOGGLES.isEmpty()) {
            WildlandsFinalPatch.LOGGER.info("No fixes are registered in this build.");
            return;
        }

        if (!LOG_APPLIED_FIXES.get()) {
            return;
        }

        TOGGLES.forEach((key, value) ->
                WildlandsFinalPatch.LOGGER.info("Fix {}: {}", key, value.get() ? "enabled" : "disabled"));
    }
}
