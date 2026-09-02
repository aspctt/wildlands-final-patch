package com.aspctt.wildlandspatch.client.sodium;

import com.aspctt.wildlandspatch.Mods;
import com.aspctt.wildlandspatch.WildlandsFinalPatch;

import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.kir.cubeswithoutborders.client.FullscreenManager;
import dev.kir.cubeswithoutborders.client.FullscreenMode;

/**
 * Cubes Without Borders' fullscreen mode, rebuilt on Sodium's config API.
 *
 * <p>The mod turns the fullscreen toggle into a three way choice: off, exclusive fullscreen, or a
 * borderless window. Sodium's own option is still a plain toggle and other options on its General
 * page read it as a boolean, so this adds the three way control alongside rather than replacing it.
 * Setting a mode drives the window immediately and writes the mod's own config, so nothing here
 * needs to persist it.
 */
final class CubesWithoutBordersOptions {
    private CubesWithoutBordersOptions() {
    }

    static OptionPageBuilder createPage(ConfigBuilder builder) {
        EnumOptionBuilder<FullscreenMode> mode = builder
                .createEnumOption(
                        ResourceLocation.fromNamespaceAndPath(
                                WildlandsFinalPatch.MODID, Mods.CUBES_WITHOUT_BORDERS + "/fullscreen_mode"),
                        FullscreenMode.class)
                .setName(Component.translatable("options.fullscreen"))
                .setTooltip(Component.translatable("sodium.options.fullscreen.tooltip"))
                .setElementNameProvider(value -> Component.translatable(value.getTranslationKey()))
                .setDefaultValue(FullscreenMode.OFF)
                .setBinding(CubesWithoutBordersOptions::setMode, CubesWithoutBordersOptions::getMode)
                // Switching modes writes Minecraft's own fullscreen option as a side effect, and
                // that lives in options.txt.
                .setStorageHandler(() -> Minecraft.getInstance().options.save());

        return builder.createOptionPage()
                .setName(Component.translatable("wildlands_patch.sodium.page.cubes_without_borders"))
                .addOption(mode);
    }

    private static FullscreenMode getMode() {
        return FullscreenManager.getInstance().getFullscreenMode();
    }

    private static void setMode(FullscreenMode value) {
        FullscreenManager.getInstance().setFullscreenMode(value);
    }
}
