package dev.anye.mc.basecore.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.lib._JsonConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class BasecoreConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static BasecoreConfig INSTANCE;

    @Expose
    @SerializedName("gameplay")
    private String gameplay = "modular";

    @Expose
    @SerializedName("part_death_drop_percent")
    private int partDeathDropPercent = 10;

    @Expose
    @SerializedName("upgrades")
    private UpgradeCosts upgrades = new UpgradeCosts();

    public static class UpgradeCosts {
        @Expose
        @SerializedName("range_cost")
        public int rangeCost = 64;

        @Expose
        @SerializedName("defense_cost")
        public int defenseCost = 128;

        @Expose
        @SerializedName("secure_cost")
        public int secureCost = 96;

        @Expose
        @SerializedName("auto_repair_cost")
        public int autoRepairCost = 160;

        @Expose
        @SerializedName("thorns_cost")
        public int thornsCost = 80;

        @Expose
        @SerializedName("exp_def_cost")
        public int expDefCost = 120;

        @Expose
        @SerializedName("counter_recon_cost")
        public int counterReconCost = 100;

        @Expose
        @SerializedName("strength_cost")
        public int strengthCost = 72;

        @Expose
        @SerializedName("jump_boost_cost")
        public int jumpBoostCost = 40;

        @Expose
        @SerializedName("regeneration_cost")
        public int regenerationCost = 144;

        @Expose
        @SerializedName("resistance_cost")
        public int resistanceCost = 96;

        @Expose
        @SerializedName("dig_speed_cost")
        public int digSpeedCost = 56;

        @Expose
        @SerializedName("movement_speed_cost")
        public int movementSpeedCost = 48;

        @Expose
        @SerializedName("dig_slowdown_cost")
        public int digSlowdownCost = 64;

        @Expose
        @SerializedName("weakness_cost")
        public int weaknessCost = 64;

        @Expose
        @SerializedName("movement_slowdown_cost")
        public int movementSlowdownCost = 64;

        @Expose
        @SerializedName("defend_range_cost")
        public int defendRangeCost = 48;

        @Expose
        @SerializedName("defend_defense_cost")
        public int defendDefenseCost = 64;

        @Expose
        @SerializedName("defend_strength_cost")
        public int defendStrengthCost = 72;

        @Expose
        @SerializedName("defend_regeneration_cost")
        public int defendRegenerationCost = 120;

        @Expose
        @SerializedName("defend_thorns_cost")
        public int defendThornsCost = 80;

        @Expose
        @SerializedName("defend_auto_repair_cost")
        public int defendAutoRepairCost = 120;
    }

    public static void load() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        File configFile = configDir.resolve(BaseCore.MOD_ID + ".json").toFile();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                INSTANCE = _JsonConfig.GSON.fromJson(reader, BasecoreConfig.class);
                if (INSTANCE == null) INSTANCE = new BasecoreConfig();
                LOGGER.info("Loaded config: gameplay={}", INSTANCE.gameplay);
            } catch (IOException e) {
                LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new BasecoreConfig();
            }
        } else {
            INSTANCE = new BasecoreConfig();
            save();
        }
    }

    public static void save() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        File configFile = configDir.resolve(BaseCore.MOD_ID + ".json").toFile();

        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                _JsonConfig.GSON_PRETTY.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public static boolean isComponentMode() {
        ensureLoaded();
        return "component".equals(INSTANCE.gameplay);
    }

    public static int getPartDeathDropPercent() {
        ensureLoaded();
        return INSTANCE.partDeathDropPercent;
    }

    public static int getUpgradeRangeCost() {
        ensureLoaded();
        return INSTANCE.upgrades.rangeCost;
    }

    public static int getUpgradeDefenseCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defenseCost;
    }

    public static int getUpgradeSecureCost() {
        ensureLoaded();
        return INSTANCE.upgrades.secureCost;
    }

    public static int getUpgradeAutoRepairCost() {
        ensureLoaded();
        return INSTANCE.upgrades.autoRepairCost;
    }

    public static int getUpgradeThornsCost() {
        ensureLoaded();
        return INSTANCE.upgrades.thornsCost;
    }

    public static int getUpgradeExpDefCost() {
        ensureLoaded();
        return INSTANCE.upgrades.expDefCost;
    }

    public static int getUpgradeCounterReconCost() {
        ensureLoaded();
        return INSTANCE.upgrades.counterReconCost;
    }

    public static int getUpgradeStrengthCost() {
        ensureLoaded();
        return INSTANCE.upgrades.strengthCost;
    }

    public static int getUpgradeJumpBoostCost() {
        ensureLoaded();
        return INSTANCE.upgrades.jumpBoostCost;
    }

    public static int getUpgradeRegenerationCost() {
        ensureLoaded();
        return INSTANCE.upgrades.regenerationCost;
    }

    public static int getUpgradeResistanceCost() {
        ensureLoaded();
        return INSTANCE.upgrades.resistanceCost;
    }

    public static int getUpgradeDigSpeedCost() {
        ensureLoaded();
        return INSTANCE.upgrades.digSpeedCost;
    }

    public static int getUpgradeMovementSpeedCost() {
        ensureLoaded();
        return INSTANCE.upgrades.movementSpeedCost;
    }

    public static int getUpgradeDigSlowdownCost() {
        ensureLoaded();
        return INSTANCE.upgrades.digSlowdownCost;
    }

    public static int getUpgradeWeaknessCost() {
        ensureLoaded();
        return INSTANCE.upgrades.weaknessCost;
    }

    public static int getUpgradeMovementSlowdownCost() {
        ensureLoaded();
        return INSTANCE.upgrades.movementSlowdownCost;
    }

    public static int getUpgradeDefendRangeCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defendRangeCost;
    }

    public static int getUpgradeDefendDefenseCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defendDefenseCost;
    }

    public static int getUpgradeDefendStrengthCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defendStrengthCost;
    }

    public static int getUpgradeDefendRegenerationCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defendRegenerationCost;
    }

    public static int getUpgradeDefendThornsCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defendThornsCost;
    }

    public static int getUpgradeDefendAutoRepairCost() {
        ensureLoaded();
        return INSTANCE.upgrades.defendAutoRepairCost;
    }

    private static void ensureLoaded() {
        if (INSTANCE == null) {
            load();
        }
    }
}
