package com.aspctt.wildlandspatch.mixin;

import com.aspctt.wildlandspatch.Config;

import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sereneseasons.season.SeasonHooks;

/**
 * Breaks the infinite recursion that crashes a single player world a few seconds after it loads.
 *
 * <p>Serene Seasons 10.1.0.7 added a client mixin that intercepts {@code Biome.hasPrecipitation}
 * and answers it from {@code SeasonHooks.hasPrecipitationSeasonal}. That method handles biomes
 * tagged tropical itself, and for every other biome falls back to asking the biome, which is the
 * method the mixin just intercepted. The hook therefore calls itself until the stack runs out,
 * crashing with a {@link StackOverflowError} in the server tick loop. It needs a client to happen
 * at all, since the mixin bails out when there is no client level, which is why a dedicated server
 * is unaffected and why the crash lands a moment after the world opens.
 *
 * <p>This guards re-entry rather than touching the fallback. The first call through is left alone
 * and counted; a call arriving while one is already in progress can only have come back round
 * through the hook, so it is answered directly from the biome's own precipitation flag and never
 * reaches the fallback. NeoForge routes {@code getModifiedClimateSettings()} through the same biome
 * modifiers {@code hasPrecipitation()} would have seen, so the answer is the one Serene Seasons
 * asked for.
 *
 * <p>The guard sits on the outside of the method deliberately. The first version of this fix
 * redirected the fallback call itself, which was precise and lasted exactly one Serene Seasons
 * release: 10.1.0.9 moved that call into a lambda without fixing the recursion, the redirect
 * matched nothing, and a required injection that matches nothing is a hard crash. Injecting at HEAD
 * and RETURN depends only on the method existing, not on what its body looks like this week.
 *
 * <p>Confirmed still needed as of 10.1.0.9: {@code MixinBiomeClient.onHasPrecipitation} is still
 * there, and {@code precipitationOverrideSeasonal} still returns an empty Optional for any biome
 * that is not tropical, falling through to {@code holder.value().hasPrecipitation()}.
 */
@Mixin(value = SeasonHooks.class, remap = false)
public class SeasonHooksMixin {
    /**
     * Depth of the current thread's trip through the hook, in a one element array so the count can
     * be changed without re-boxing and re-setting the ThreadLocal on every call.
     */
    @Unique
    private static final ThreadLocal<int[]> wildlands_patch$depth = ThreadLocal.withInitial(() -> new int[1]);

    @Inject(
            method = "hasPrecipitationSeasonal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void wildlands_patch$breakPrecipitationRecursion(
            Level level, Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enabled(Config.SERENE_SEASONS_PRECIPITATION_RECURSION)) {
            return;
        }

        int[] depth = wildlands_patch$depth.get();
        if (depth[0] > 0) {
            // Already inside the hook, so this call came back round through it. Cancelling here
            // means the RETURN injection below is never reached for this invocation, which is what
            // keeps the count balanced.
            cir.setReturnValue(biome.value().getModifiedClimateSettings().hasPrecipitation());
            return;
        }

        depth[0]++;
    }

    @Inject(
            method = "hasPrecipitationSeasonal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;)Z",
            at = @At("RETURN"))
    private static void wildlands_patch$clearPrecipitationGuard(
            Level level, Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enabled(Config.SERENE_SEASONS_PRECIPITATION_RECURSION)) {
            return;
        }

        int[] depth = wildlands_patch$depth.get();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }
}
