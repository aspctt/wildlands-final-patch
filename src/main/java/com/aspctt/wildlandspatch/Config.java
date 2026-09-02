package com.aspctt.wildlandspatch;

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
    // Fix toggles. One line per fix. Keep the comment aimed at a player reading the config file:
    // what breaks without the fix, and which mods are involved.
    // ---------------------------------------------------------------------------------------

    public static final String SODIUM_OPTIONS_BETTER_BIOME_REBLEND = fix("sodiumOptionsBetterBiomeReblend", true,
            "Puts Better Biome Reblend's blend radius back in Sodium's video settings.",
            "Its own integration targets a class Sodium removed in 0.8, so without this the setting is",
            "missing from that screen and the blend radius cannot be changed in game.",
            "Takes effect on restart.");

    public static final String SODIUM_OPTIONS_CUBES_WITHOUT_BORDERS = fix("sodiumOptionsCubesWithoutBorders", true,
            "Puts Cubes Without Borders' fullscreen mode back in Sodium's video settings, as a three way",
            "choice of off, fullscreen, or borderless. Its own integration targets the same class Sodium",
            "removed in 0.8.",
            "Takes effect on restart.");

    public static final String DUPLICATE_EMPTY_LOOT_TABLE = fix("duplicateEmptyLootTable", true,
            "Stops a datapack that ships its own minecraft:empty loot table from crashing world creation.",
            "Minecraft registers its own copy after the datapacks have loaded and does not check first, so the",
            "second one is a duplicate key and the game dies on the click that opens the world creation screen.",
            "Datapacks written for 1.20 and earlier carry that file harmlessly, which is where they come from.");

    public static final String BROKEN_DATA_OVERRIDES = fix("brokenDataOverrides", true,
            "Replaces JSON files that other mods ship broken, from a datapack loaded above their own data.",
            "Currently: Create Deco's placard recipe, which is written in a syntax Minecraft only accepted",
            "from 1.21.2 and so never loads here, and Dungeons and Taverns' quest trader advancement, which",
            "names a parent that does not exist and so never loads either, leaving the trade ungranted.");

    public static final String SERENE_SEASONS_PRECIPITATION_RECURSION = fix("sereneSeasonsPrecipitationRecursion", true,
            "Stops Serene Seasons crashing a single player world a few seconds after it loads.",
            "Its precipitation hook falls back to asking the biome the same question the hook intercepts, so for",
            "any biome not tagged tropical it calls itself until the stack runs out. Added in 10.1.0.7.",
            "Turning this off restores the crash.");

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
     * @param defaultValue whether the fix is on out of the box
     * @param comment      what the fix does, written for whoever is reading the config file, one
     *                     argument per line
     */
    private static String fix(String key, boolean defaultValue, String... comment) {
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
