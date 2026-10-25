package dev.pvphud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PvpHud {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private static final int GREEN = 0xFF55FF55;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int RED = 0xFFFF5555;
    private static final int BG_IDLE = 0x80000000;
    private static final int BG_DOWN = 0xB0FFFFFF;

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private PvpHud() {}

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        PvpConfig c = PvpConfig.get();
        Options o = mc.options;
        Font font = mc.font;

        PvpHudClient.LEFT.update(o.keyAttack.isDown());
        PvpHudClient.RIGHT.update(o.keyUse.isDown());

        // ===== Колонка 1: CPS, комбо, кейстроки, эффекты =====
        int x = c.hudX;
        int y = c.hudY;

        if (c.showCps) {
            g.drawString(font, PvpHudClient.LEFT.cps() + " | " + PvpHudClient.RIGHT.cps() + " CPS", x, y, WHITE, true);
            y += 12;
        }
        if (c.showCombo) {
            g.drawString(font, "Combo: " + PvpHudClient.combo, x, y, WHITE, true);
            y += 12;
        }
        if (c.showKeys) {
            int ky = y + 4;
            key(g, font, "W", o.keyUp.isDown(), x + 22, ky, 20, 20);
            key(g, font, "A", o.keyLeft.isDown(), x, ky + 22, 20, 20);
            key(g, font, "S", o.keyDown.isDown(), x + 22, ky + 22, 20, 20);
            key(g, font, "D", o.keyRight.isDown(), x + 44, ky + 22, 20, 20);
            key(g, font, "LMB", o.keyAttack.isDown(), x, ky + 44, 31, 20);
            key(g, font, "RMB", o.keyUse.isDown(), x + 33, ky + 44, 31, 20);
            key(g, font, "", o.keyJump.isDown(), x, ky + 66, 64, 8);
            y = ky + 82;
        }
        if (c.showEffects) {
            for (MobEffectInstance e : player.getActiveEffects()) {
                String name = e.getEffect().value().getDisplayName().getString();
                String time = e.isInfiniteDuration() ? "∞" : formatTicks(e.getDuration());
                g.drawString(font, name + " " + time, x, y, WHITE, true);
                y += 11;
            }
        }

        // ===== Колонка 2: броня, reach/цель, предметы =====
        int x2 = c.hudX + 80;
        int y2 = c.hudY;

        if (c.showArmor) {
            for (EquipmentSlot slot : ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.isEmpty()) continue;
                g.renderItem(stack, x2, y2);
                if (stack.isDamageableItem()) {
                    int max = stack.getMaxDamage();
                    int left = max - stack.getDamageValue();
                    int pct = max > 0 ? left * 100 / max : 100;
                    int color = pct > 50 ? GREEN : pct > 25 ? YELLOW : RED;
                    g.drawString(font, pct + "%", x2 + 20, y2 + 4, color, true);
                }
                y2 += 18;
            }
            y2 += 4;
        }

        if (c.showCombatInfo && PvpHudClient.lastTarget != null
                && System.currentTimeMillis() - PvpHudClient.lastHitMs < 5000) {
            LivingEntity t = PvpHudClient.lastTarget;
            g.drawString(font, String.format("Reach: %.2f", PvpHudClient.lastReach), x2, y2, WHITE, true);
            y2 += 11;
            g.drawString(font, t.getName().getString() + " " + String.format("%.1f/%.0f", t.getHealth(), t.getMaxHealth()),
                    x2, y2, WHITE, true);
            y2 += 15;
        }

        if (c.showItems) {
            Inventory inv = player.getInventory();
            y2 = counter(g, font, inv, Items.ENDER_PEARL, x2, y2);
            y2 = counter(g, font, inv, Items.GOLDEN_APPLE, x2, y2);
            counter(g, font, inv, Items.TOTEM_OF_UNDYING, x2, y2);
        }
    }

    /** Рисует иконку предмета и количество, если он есть в инвентаре. Возвращает новый y. */
    private static int counter(GuiGraphics g, Font font, Inventory inv, Item item, int x, int y) {
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item)) n += s.getCount();
        }
        if (n <= 0) return y;
        g.renderItem(new ItemStack(item), x, y);
        g.drawString(font, "x" + n, x + 20, y + 4, WHITE, true);
        return y + 18;
    }

    private static void key(GuiGraphics g, Font font, String label, boolean down, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, down ? BG_DOWN : BG_IDLE);
        if (!label.isEmpty()) {
            int tx = x + (w - font.width(label)) / 2;
            int ty = y + (h - 8) / 2;
            g.drawString(font, label, tx, ty, down ? BLACK : WHITE, false);
        }
    }

    private static String formatTicks(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
