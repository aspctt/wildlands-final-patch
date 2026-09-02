package com.aspctt.wildlandspatch;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import org.slf4j.Logger;

/**
 * Compatibility and bug fix patches for the Wildlands modpack.
 *
 * <p>This mod registers no content of its own. Everything it does is a fix for behaviour that
 * comes from somewhere else, so the only job of this class is to bring the config up and report
 * what ended up active.
 */
@Mod(WildlandsFinalPatch.MODID)
public final class WildlandsFinalPatch {
    /** Must match {@code mod_id} in gradle.properties. */
    public static final String MODID = "wildlands_patch";

    public static final Logger LOGGER = LogUtils.getLogger();

    public WildlandsFinalPatch(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modEventBus, modContainer);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Config.logFixes();
    }
}
