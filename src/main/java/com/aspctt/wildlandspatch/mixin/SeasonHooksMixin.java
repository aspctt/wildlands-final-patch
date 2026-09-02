package com.aspctt.wildlandspatch.mixin;

import com.aspctt.wildlandspatch.Config;

import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import sereneseasons.season.SeasonHooks;

/**
 * Breaks the infinite recursion that crashes a single player world a few seconds after it loads.
 *
 * <p>Serene Seasons 10.1.0.7 added a client mixin that intercepts {@code Biome.hasPrecipitation}
 * and answers it from {@code SeasonHooks.hasPrecipitationSeasonal} instead. That method handles
 * biomes tagged tropical itself, and for every other biome falls back to asking the biome:
 *
 * <pre>return holder.value().hasPrecipitation();</pre>
 *
 * <p>Which is the method the mixin just intercepted. So for any biome that is not tropical, and
 * that is nearly all of them, the hook calls the method that calls the hook, until the stack runs
 * out. The crash is a {@link StackOverflowError} in the server tick loop with several hundred
 * frames of the two alternating. It needs a client to happen at all, since the mixin bails out when
 * {@code Minecraft.getInstance().level} is null, which is why a dedicated server is unaffected and
 * why it takes a moment after the world opens.
 *
 * <p>The fallback is redirected to read the biome's own precipitation flag directly. NeoForge
 * routes {@code getModifiedClimateSettings()} through the same biome modifiers that
 * {@code hasPrecipitation()} would have seen, so the answer is the one Serene Seasons was asking
 * for, without going back through its own hook.
 *
 * <p>Version 10.1.0.8 is current as of writing, and 10.1.0.3, from before the hook was added, does
 * not have the bug. Once upstream fixes this, the redirect stops matching and Mixin will say so
 * loudly rather than doing anything quietly wrong.
 */
@Mixin(value = SeasonHooks.class, remap = false)
public class SeasonHooksMixin {
    @Redirect(
            method = "hasPrecipitationSeasonal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;hasPrecipitation()Z"))
    private static boolean wildlands_patch$readPrecipitationWithoutRecursing(Biome biome) {
        if (!Config.enabled(Config.SERENE_SEASONS_PRECIPITATION_RECURSION)) {
            // Deliberately the original call, recursion and all, so the toggle really is off.
            return biome.hasPrecipitation();
        }

        return biome.getModifiedClimateSettings().hasPrecipitation();
    }
}
