package com.aspctt.wildlandspatch;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client entry point. Never loaded on a dedicated server, so client only classes are safe here.
 *
 * <p>Rendering, HUD, and input fixes belong on this side. Anything that decides an outcome belongs
 * on the server side instead, or the two will disagree.
 */
@Mod(value = WildlandsFinalPatch.MODID, dist = Dist.CLIENT)
public final class WildlandsFinalPatchClient {
    public WildlandsFinalPatchClient(ModContainer container) {
        // Puts the fix toggles behind the mod's Config button on the Mods screen.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
