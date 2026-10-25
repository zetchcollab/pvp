package dev.pvphud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private static final String[] FIRE_NAMES = {"Обычный", "Ниже", "Ещё ниже", "Очень низко"};

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Component.literal("PvP HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        PvpConfig c = PvpConfig.get();
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = 36;
        int step = 24;

        toggle(left, y, "CPS", () -> c.showCps, v -> c.showCps = v);
        toggle(right, y, "Кейстроки", () -> c.showKeys, v -> c.showKeys = v);
        y += step;
        toggle(left, y, "Комбо", () -> c.showCombo, v -> c.showCombo = v);
        toggle(right, y, "Эффекты", () -> c.showEffects, v -> c.showEffects = v);
        y += step;
        toggle(left, y, "Броня", () -> c.showArmor, v -> c.showArmor = v);
        toggle(right, y, "Reach и цель", () -> c.showCombatInfo, v -> c.showCombatInfo = v);
        y += step;
        toggle(left, y, "Жемчуг/яблоки/тотемы", () -> c.showItems, v -> c.showItems = v);
        toggle(right, y, "Прозрачный инвентарь", () -> c.transparentInventory, v -> c.transparentInventory = v);
        y += step;

        addRenderableWidget(Button.builder(fireLabel(c), b -> {
            c.fireLevel = (c.fireLevel + 1) % FIRE_NAMES.length;
            b.setMessage(fireLabel(c));
        }).bounds(left, y, 150, 20).build());
        y += step + 8;

        // Позиция HUD
        addRenderableWidget(Button.builder(Component.literal("X −"), b -> c.hudX = Math.max(0, c.hudX - 5))
                .bounds(left, y, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("X +"), b -> c.hudX += 5)
                .bounds(left + 78, y, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y −"), b -> c.hudY = Math.max(0, c.hudY - 5))
                .bounds(right, y, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y +"), b -> c.hudY += 5)
                .bounds(right + 78, y, 72, 20).build());
        y += step + 8;

        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(this.width / 2 - 100, y, 200, 20).build());
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).bounds(x, y, 150, 20).build());
    }

    private static Component label(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "Вкл" : "Выкл"));
    }

    private static Component fireLabel(PvpConfig c) {
        return Component.literal("Огонь: " + FIRE_NAMES[c.fireLevel]);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void removed() {
        PvpConfig.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
