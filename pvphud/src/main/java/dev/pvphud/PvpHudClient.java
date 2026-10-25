package dev.pvphud;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

public class PvpHudClient implements ClientModInitializer {
    public static final String MOD_ID = "pvphud";

    public static final ClickTracker LEFT = new ClickTracker();
    public static final ClickTracker RIGHT = new ClickTracker();

    public static int combo = 0;

    // Последний удар: дистанция и цель
    public static double lastReach = 0;
    public static LivingEntity lastTarget = null;
    public static long lastHitMs = 0;

    private static boolean wasHurt = false;
    private static boolean openSettings = false;

    @Override
    public void onInitializeClient() {
        PvpConfig.load();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                PvpHud::render
        );

        // Удар по живому существу: комбо, reach, цель (только от локального игрока).
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (player == Minecraft.getInstance().player && entity instanceof LivingEntity target) {
                combo++;
                lastTarget = target;
                lastReach = Math.sqrt(target.getBoundingBox().distanceToSqr(player.getEyePosition()));
                lastHitMs = System.currentTimeMillis();
            }
            return InteractionResult.PASS;
        });

        // Команда /pvphud открывает настройки.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("pvphud").executes(ctx -> {
                    openSettings = true;
                    return Command.SINGLE_SUCCESS;
                })));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (openSettings) {
                openSettings = false;
                mc.setScreen(new SettingsScreen(null));
            }
            if (mc.player == null) {
                combo = 0;
                wasHurt = false;
                lastTarget = null;
                return;
            }
            // Сброс комбо, когда ударили нас.
            boolean hurt = mc.player.hurtTime > 0;
            if (hurt && !wasHurt) {
                combo = 0;
            }
            wasHurt = hurt;
        });
    }
}
