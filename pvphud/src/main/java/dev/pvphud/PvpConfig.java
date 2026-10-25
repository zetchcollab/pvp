package dev.pvphud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Настройки мода. Хранятся в config/pvphud.json. */
public class PvpConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PvpConfig instance = new PvpConfig();

    public boolean showCps = true;
    public boolean showCombo = true;
    public boolean showKeys = true;
    public boolean showEffects = true;
    public boolean showArmor = true;
    public boolean showCombatInfo = true; // reach + здоровье цели
    public boolean showItems = true;      // жемчуг / яблоки / тотемы

    public boolean transparentInventory = true;
    /** 0 = ванильный огонь, 1..3 = всё ниже. */
    public int fireLevel = 2;

    public int hudX = 6;
    public int hudY = 6;

    public static PvpConfig get() {
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("pvphud.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try {
                PvpConfig loaded = GSON.fromJson(Files.readString(p), PvpConfig.class);
                if (loaded != null) instance = loaded;
            } catch (Exception e) {
                System.err.println("[pvphud] Не удалось прочитать конфиг, используются значения по умолчанию: " + e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[pvphud] Не удалось сохранить конфиг: " + e);
        }
    }
}
