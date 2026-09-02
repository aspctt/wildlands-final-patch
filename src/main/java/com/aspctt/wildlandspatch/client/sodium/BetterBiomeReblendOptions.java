package com.aspctt.wildlandspatch.client.sodium;

import com.aspctt.wildlandspatch.Mods;
import com.aspctt.wildlandspatch.WildlandsFinalPatch;

import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import neoforge.fionathemortal.betterbiomeblend.BetterBiomeBlendClient;

/**
 * Better Biome Reblend's blend radius, rebuilt on Sodium's config API.
 *
 * <p>The mod keeps its radius in its own {@code OptionInstance} rather than the vanilla one, which
 * is what lets it go past vanilla's 15x15 ceiling. The value is the slider position, and the radius
 * it means is {@code 2 * value + 1}, so 14 is the 29x29 the mod defaults to.
 */
final class BetterBiomeReblendOptions {
    /**
     * Highest slider position the mod's own option accepts. Its {@code IntRange} stops at 14, and a
     * larger value here would be clamped on the way in and read back wrong.
     */
    private static final int MAX_RADIUS_STEP = 14;

    /** The mod's own default, matching what it uses when options.txt has no entry yet. */
    private static final int DEFAULT_RADIUS_STEP = 14;

    private BetterBiomeReblendOptions() {
    }

    static OptionPageBuilder createPage(ConfigBuilder builder) {
        IntegerOptionBuilder radius = builder
                .createIntegerOption(ResourceLocation.fromNamespaceAndPath(
                        WildlandsFinalPatch.MODID, Mods.BETTER_BIOME_REBLEND + "/blend_radius"))
                .setRange(0, MAX_RADIUS_STEP, 1)
                // Sodium's own biomeBlend formatter rejects anything past 7, which is the vanilla
                // ceiling, and renders it as an error string. This is the same label vanilla builds,
                // reading the keys the mod adds for the radii above 15x15.
                .setValueFormatter(value -> Component.translatable("options.biomeBlendRadius." + (value * 2 + 1)))
                .setName(Component.translatable("bbb.biomeBlendRadius"))
                .setTooltip(Component.translatable("bbb.biomeBlendRadius.tooltip"))
                .setImpact(OptionImpact.LOW)
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                .setDefaultValue(DEFAULT_RADIUS_STEP)
                .setBinding(BetterBiomeReblendOptions::setRadiusStep, BetterBiomeReblendOptions::getRadiusStep)
                // The radius is stored in options.txt, which Sodium's screen has no reason to write
                // on its own, so ask the game to save its options after the change is applied.
                .setStorageHandler(() -> Minecraft.getInstance().options.save());

        return builder.createOptionPage()
                .setName(Component.translatable("wildlands_patch.sodium.page.better_biome_reblend"))
                .addOption(radius);
    }

    private static int getRadiusStep() {
        return BetterBiomeBlendClient.betterBiomeBlendRadius.get();
    }

    private static void setRadiusStep(int value) {
        // The mod's own option reloads the level renderer when this changes, so the flag above is
        // belt and braces rather than the thing doing the work.
        BetterBiomeBlendClient.betterBiomeBlendRadius.set(value);
    }
}
