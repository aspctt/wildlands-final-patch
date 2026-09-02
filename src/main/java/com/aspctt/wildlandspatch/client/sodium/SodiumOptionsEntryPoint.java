package com.aspctt.wildlandspatch.client.sodium;

import java.util.ArrayList;
import java.util.List;

import com.aspctt.wildlandspatch.Config;
import com.aspctt.wildlandspatch.Mods;
import com.aspctt.wildlandspatch.WildlandsFinalPatch;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;

/**
 * Puts back the options pages that mods lost when Sodium replaced its GUI.
 *
 * <p>Sodium 0.6 let a mod add its settings to the video settings screen by mixing into
 * {@code SodiumGameOptionPages}. Sodium 0.8 deleted that class and replaced it with the config API
 * used here. Mods that have not been updated still carry the old mixin, which now silently fails to
 * apply: their settings simply stop appearing, with nothing in the log beyond a mixin target
 * warning. This class re-registers those settings through the supported API.
 *
 * <p>Everything is registered as pages under this mod rather than under the mod each page belongs
 * to. Sodium allows a mod to register options on another mod's behalf, and doing so would put each
 * page under its own mod's name, but two registrations under one id are a hard crash on startup:
 * Sodium throws on the duplicate and turns it into a crash report. That is exactly what would
 * happen the day one of these mods ships its own Sodium 0.8 integration while this patch is still
 * installed, so the pages live here, where nothing else can collide with them.
 *
 * <p>Sodium finds this class through the {@link ConfigEntryPointForge} annotation, loading it by
 * name only after it has scanned the mod list. Nothing else references it, so when Sodium is absent
 * it and the classes it touches are never loaded at all.
 */
@ConfigEntryPointForge(WildlandsFinalPatch.MODID)
public final class SodiumOptionsEntryPoint implements ConfigEntryPoint {
    /** Instantiated reflectively by Sodium, so this has to stay public and take no arguments. */
    public SodiumOptionsEntryPoint() {
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        List<PageBuilder> pages = new ArrayList<>(2);

        // Each block is written out rather than routed through a shared helper on purpose: naming
        // the class inside the branch is what keeps the JVM from loading it, and the mod it reaches
        // into, when that mod is not installed.
        if (Mods.loaded(Mods.BETTER_BIOME_REBLEND) && Config.enabled(Config.SODIUM_OPTIONS_BETTER_BIOME_REBLEND)) {
            try {
                pages.add(BetterBiomeReblendOptions.createPage(builder));
            } catch (Throwable t) {
                logFailure(Mods.BETTER_BIOME_REBLEND, t);
            }
        }

        if (Mods.loaded(Mods.CUBES_WITHOUT_BORDERS) && Config.enabled(Config.SODIUM_OPTIONS_CUBES_WITHOUT_BORDERS)) {
            try {
                pages.add(CubesWithoutBordersOptions.createPage(builder));
            } catch (Throwable t) {
                logFailure(Mods.CUBES_WITHOUT_BORDERS, t);
            }
        }

        // Registering with no pages at all is rejected by Sodium, so only claim an entry in the
        // video settings screen once there is something to put in it.
        if (pages.isEmpty()) {
            return;
        }

        ModOptionsBuilder options = builder.registerOwnModOptions();
        for (PageBuilder page : pages) {
            options.addPage(page);
        }
    }

    /**
     * Every page is built separately, and Throwable is caught rather than Exception, because the
     * failure to expect is a {@link NoClassDefFoundError} from one of these mods moving a class in
     * an update. One mod doing that must not take the rest of the screen down with it.
     */
    private static void logFailure(String modId, Throwable t) {
        WildlandsFinalPatch.LOGGER.error(
                "Could not add {}'s options to Sodium's video settings. Its settings will be missing from that screen, "
                        + "and this usually means the mod changed since this patch was built.", modId, t);
    }
}
