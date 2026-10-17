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
import java.util.ArrayList;
import java.util.List;

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

    @Expose
    @SerializedName("balance")
    private BalanceSettings balance = new BalanceSettings();

    public static class BalanceSettings {
        @Expose
        @SerializedName("basecore_range")
        public int basecoreRange = 10;

        @Expose
        @SerializedName("defend_range")
        public int defendRange = 15;

        @Expose
        @SerializedName("defend_health")
        public int defendHealth = 50;

        @Expose
        @SerializedName("defend_damage")
        public float defendDamage = 4.0f;

        @Expose
        @SerializedName("defend_attack_interval")
        public int defendAttackInterval = 80;

        /** 机械部署/升级时长（秒），默认 30 秒保持原有玩法 */
        @Expose
        @SerializedName("placement_seconds")
        public int placementSeconds = 30;

        /** 怪物攻击基地核心（占点玩法），默认关闭 */
        @Expose
        @SerializedName("monster_attack_core")
        public boolean monsterAttackCore = false;

        /** 怪物每次攻击对核心造成的伤害，仅在 monster_attack_core 开启时生效 */
        @Expose
        @SerializedName("monster_core_damage")
        public float monsterCoreDamage = 2.0f;

        /** 怪物破坏核心时一轮裂纹积累的总时长（秒），默认 2 秒 */
        @Expose
        @SerializedName("monster_crack_seconds")
        public int monsterCrackSeconds = 2;

        /** 同时攻击核心的最大怪物数量，默认 10 */
        @Expose
        @SerializedName("monster_attack_max_count")
        public int monsterAttackMaxCount = 10;

        /** 怪物可感知核心的距离（格），范围内的怪物会仇恨并走向核心，默认 32 */
        @Expose
        @SerializedName("monster_sense_range")
        public int monsterSenseRange = 32;

        /** 怪物名单模式：all = 所有怪物（默认）；whitelist = 仅名单内；blacklist = 名单外 */
        @Expose
        @SerializedName("monster_attack_list_mode")
        public String monsterAttackListMode = "all";

        /** 怪物实体 ID 名单，如 "minecraft:zombie"、"minecraft:skeleton" */
        @Expose
        @SerializedName("monster_attack_list")
        public List<String> monsterAttackList = new ArrayList<>();
    }

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

    // ----- Balance settings -----

    public static int getBasecoreRange() {
        ensureLoaded();
        return Math.max(0, INSTANCE.balance.basecoreRange);
    }

    public static int getDefendRange() {
        ensureLoaded();
        return Math.max(0, INSTANCE.balance.defendRange);
    }

    public static int getDefendHealth() {
        ensureLoaded();
        return Math.max(1, INSTANCE.balance.defendHealth);
    }

    public static float getDefendDamage() {
        ensureLoaded();
        return Math.max(0.0f, INSTANCE.balance.defendDamage);
    }

    /** 攻击间隔（tick），最小 1 tick */
    public static int getDefendAttackInterval() {
        ensureLoaded();
        return Math.max(1, INSTANCE.balance.defendAttackInterval);
    }

    /** 机械部署/升级时长（tick），最小 1 tick */
    public static int getPlacementTicks() {
        ensureLoaded();
        return Math.max(1, INSTANCE.balance.placementSeconds * 20);
    }

    public static boolean isMonsterAttackCore() {
        ensureLoaded();
        return INSTANCE.balance.monsterAttackCore;
    }

    public static float getMonsterCoreDamage() {
        ensureLoaded();
        return Math.max(0.0f, INSTANCE.balance.monsterCoreDamage);
    }

    /** 怪物破坏核心一轮裂纹积累的总时长（tick），最小 1 tick */
    public static int getMonsterCrackTicks() {
        ensureLoaded();
        return Math.max(1, INSTANCE.balance.monsterCrackSeconds * 20);
    }

    /** 同时攻击核心的最大怪物数量，最小 1 */
    public static int getMonsterAttackMaxCount() {
        ensureLoaded();
        return Math.max(1, INSTANCE.balance.monsterAttackMaxCount);
    }

    /** 怪物可感知核心的距离（格），最小 1 */
    public static int getMonsterSenseRange() {
        ensureLoaded();
        return Math.max(1, INSTANCE.balance.monsterSenseRange);
    }

    /** 判断指定怪物是否会攻击核心（按 monster_attack_list_mode / monster_attack_list 过滤） */
    public static boolean shouldMonsterAttackCore(net.minecraft.world.entity.Entity entity) {
        ensureLoaded();
        String mode = INSTANCE.balance.monsterAttackListMode == null
                ? "all" : INSTANCE.balance.monsterAttackListMode.trim().toLowerCase(java.util.Locale.ROOT);
        if ("whitelist".equals(mode) || "blacklist".equals(mode)) {
            String id = net.minecraft.world.entity.EntityType.getKey(entity.getType()).toString();
            boolean inList = INSTANCE.balance.monsterAttackList != null && INSTANCE.balance.monsterAttackList.contains(id);
            return "whitelist".equals(mode) == inList;
        }
        return true; // all
    }

    private static void ensureLoaded() {
        if (INSTANCE == null) {
            load();
        }
    }
}
