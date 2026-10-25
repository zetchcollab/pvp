package dev.pvphud.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pvphud.PvpConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Меньше огня на экране. В 1.21.9+ у renderFire три аргумента (PoseStack, MultiBufferSource, TextureAtlasSprite).
 * Сдвигаем PoseStack вниз в начале метода и возвращаем обратно в конце.
 * Обработчики static, чтобы работало независимо от того, статический ли целевой метод.
 */
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    private static final float STEP = 0.12F;

    @Inject(method = "renderFire", at = @At("HEAD"))
    private static void pvphud$lowerFireStart(PoseStack poseStack, MultiBufferSource buffer, TextureAtlasSprite sprite, CallbackInfo ci) {
        int level = PvpConfig.get().fireLevel;
        if (level > 0) poseStack.translate(0.0F, -STEP * level, 0.0F);
    }

    @Inject(method = "renderFire", at = @At("RETURN"))
    private static void pvphud$lowerFireEnd(PoseStack poseStack, MultiBufferSource buffer, TextureAtlasSprite sprite, CallbackInfo ci) {
        int level = PvpConfig.get().fireLevel;
        if (level > 0) poseStack.translate(0.0F, STEP * level, 0.0F);
    }
}
