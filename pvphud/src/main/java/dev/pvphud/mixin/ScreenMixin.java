package dev.pvphud.mixin;

import dev.pvphud.PvpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Прозрачный инвентарь: убираем затемнение и размытие фона у экранов с контейнерами. */
@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void pvphud$transparentInventory(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Object self = this;
        if (PvpConfig.get().transparentInventory
                && self instanceof AbstractContainerScreen<?>
                && Minecraft.getInstance().level != null) {
            ci.cancel();
        }
    }
}
