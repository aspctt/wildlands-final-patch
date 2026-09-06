package com.aspctt.wildlandspatch.mixin.client;

import com.aspctt.wildlandspatch.Config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops an equipped elytra being drawn twice.
 *
 * <p>Better End adds its own elytra render layer to the player, to humanoid mobs and to armour
 * stands, and never suppresses the vanilla one. Its layer draws Better End's armoured elytras, but
 * it also draws the plain vanilla elytra: the check in its own render method is
 * {@code if (!stack.is(Items.ELYTRA) && !(stack.getItem() instanceof BCLElytraItem)) return;}. So
 * with any elytra on, two get drawn, vanilla's and Better End's.
 *
 * <p>Every Better End install has this. It normally goes unnoticed because both models sit in the
 * same place in the same pose and overlap into what looks like one elytra. Fresh Animations is what
 * exposes it: Entity Model Features swaps the model behind the vanilla layer for the animated one
 * from the FA player extension, so that copy starts moving with the character while Better End's
 * copy holds the stiff vanilla pose, and the second elytra becomes obvious.
 *
 * <p>This cancels Better End's layer outright rather than only for vanilla elytras, because the
 * pack removes {@code betterend:elytra_armored} and {@code elytra_crystalite} from the game, so
 * that layer has nothing of its own left to draw. The one consequence to know: if those items are
 * ever put back, they will render as nothing, since the vanilla layer only handles
 * {@code Items.ELYTRA} and will not pick them up. Turn this fix off in that case.
 *
 * <p>Targeted by class name rather than compiled against, so this mod gains no build dependency on
 * Better End and the JAR is unaffected when Better End is not installed.
 */
@Mixin(targets = "org.betterx.betterend.client.render.ArmoredElytraLayer", remap = false)
public class ArmoredElytraLayerMixin {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void wildlands_patch$dropDuplicateElytra(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        if (Config.enabled(Config.BETTER_END_DOUBLE_ELYTRA)) {
            ci.cancel();
        }
    }
}
