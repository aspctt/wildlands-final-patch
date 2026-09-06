package com.aspctt.wildlandspatch;

import net.neoforged.fml.ModList;

/**
 * Mod ids this patch knows about, and the check that guards every fix touching one.
 *
 * <p>The ids are the ones the mods declare, which are not always the ones their names suggest:
 * Better Biome Reblend is {@code betterbiomereblend}, not {@code betterbiomeblend}.
 */
public final class Mods {
    public static final String SODIUM = "sodium";

    /** Better Biome Reblend, the maintained fork of Better Biome Blend. */
    public static final String BETTER_BIOME_REBLEND = "betterbiomereblend";

    public static final String CUBES_WITHOUT_BORDERS = "cubes_without_borders";

    public static final String SERENE_SEASONS = "sereneseasons";

    public static final String BETTER_END = "betterend";

    private Mods() {
    }

    public static boolean loaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
