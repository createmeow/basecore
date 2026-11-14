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

    @Expose
    @SerializedName("integration")
    private IntegrationSettings integration = new IntegrationSettings();

    /**
     * 联动开关配置。新增内容分为「冷汗联动」与「机械动力联动」两组，可分别单独开关。
     */
    public static class IntegrationSettings {
        @Expose
        @SerializedName("cold_sweat")
        public ColdSweat coldSweat = new ColdSweat();

        @Expose
        @SerializedName("create")
        public Create create = new Create();
    }

    /** 冷汗（Cold Sweat）联动：低温下基地核心液晶屏失效 / 彻底停机。 */
    public static class ColdSweat {
        /** 是否启用冷汗联动（关闭后基地核心不受环境温度影响）。 */
        @Expose
        @SerializedName("enabled")
        public boolean enabled = true;

        /** 低于该温度（摄氏度）无法打开基地核心 GUI。 */
        @Expose
        @SerializedName("gui_disable_temperature")
        public double guiDisableTemperature = -5.0d;

        /** 低于该温度（摄氏度）基地核心彻底停止工作。 */
        @Expose
        @SerializedName("shutdown_temperature")
        public double shutdownTemperature = -15.0d;
    }

    /** 机械动力（Create）联动：基地核心/防御炮需要应力驱动，新增应力供应设备。 */
    public static class Create {
        /** 是否启用机械动力联动（关闭后基地核心/防御炮不再需要应力）。默认关闭：应力方案尚未定稿。 */
        @Expose
        @SerializedName("enabled")
        public boolean enabled = false;

        /** 基地核心最低转速（RPM）。 */
        @Expose
        @SerializedName("basecore_min_rpm")
        public int basecoreMinRpm = 64;

        /** 单台基地核心所需应力（SU）。 */
        @Expose
        @SerializedName("basecore_stress")
        public int basecoreStress = 128;

        /** 防御炮最低转速（RPM）。 */
        @Expose
        @SerializedName("defend_min_rpm")
        public int defendMinRpm = 64;

        /** 单台防御炮所需应力（SU）。 */
        @Expose
        @SerializedName("defend_stress")
        public int defendStress = 128;

        /** 不间断应力供应器单次可存储的 air 总量。 */
        @Expose
        @SerializedName("supply_air_capacity")
        public int supplyAirCapacity = 1800;

        /** 不间断应力供应器输出转速（RPM）。 */
        @Expose
        @SerializedName("supply_rpm")
        public int supplyRpm = 32;

        /** 不间断应力供应器输出应力（SU）。 */
        @Expose
        @SerializedName("supply_stress")
        public int supplyStress = 128;

        /** 不间断应力供应器每消耗 1 air 所需的 tick 数（默认 40 tick = 2 秒）。 */
        @Expose
        @SerializedName("supply_air_interval_ticks")
        public int supplyAirIntervalTicks = 40;

        /** 不间断应力供应器与基地核心联动时的搜索半径（在基地范围之外额外增加的格数）。 */
        @Expose
        @SerializedName("supply_link_range")
        public int supplyLinkRange = 6;
    }

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

        /** 苏式基地计算机随机损坏的最短间隔（秒），默认 180 秒。 */
        @Expose
        @SerializedName("soviet_damage_min_seconds")
        public int sovietDamageMinSeconds = 180;

        /** 苏式基地计算机随机损坏的最长间隔（秒），默认 600 秒。 */
        @Expose
        @SerializedName("soviet_damage_max_seconds")
        public int sovietDamageMaxSeconds = 600;

        /** 能源条容量（Rs）。1 Rs = 基地核心 2 秒 / 苏式计算机 1 秒的运行时间。 */
        @Expose
        @SerializedName("energy_capacity")
        public int energyCapacity = 900;

        /** 普通基地核心每消耗 1 Rs 所需的秒数，默认 2 秒。 */
        @Expose
        @SerializedName("basecore_energy_seconds_per_rs")
        public int basecoreEnergySecondsPerRs = 2;

        /** 苏式基地计算机每消耗 1 Rs 所需的秒数，默认 1 秒。 */
        @Expose
        @SerializedName("soviet_energy_seconds_per_rs")
        public int sovietEnergySecondsPerRs = 1;

        /** 尝试用容器内红石补充能源的间隔（秒），默认 10 秒。 */
        @Expose
        @SerializedName("energy_refuel_seconds")
        public int energyRefuelSeconds = 10;

        /** 是否启用 Rs 能源系统；关闭后基地不再消耗能源、也不接受红石补充。 */
        @Expose
        @SerializedName("energy_enabled")
        public boolean energyEnabled = true;

        /** 是否启用苏式基地计算机玩法；关闭后无法放置、已放置的结构停止运行。 */
        @Expose
        @SerializedName("soviet_computer_enabled")
        public boolean sovietComputerEnabled = true;
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
                // 旧配置文件缺少的段落（如 integration / 苏式计算机项）补全后回写，方便用户直接修改
                boolean missingIntegration = INSTANCE.integration == null;
                boolean missingSovietFields = INSTANCE.balance != null
                        && INSTANCE.balance.sovietDamageMinSeconds <= 0;
                if (INSTANCE.balance != null && missingSovietFields) {
                    INSTANCE.balance.sovietDamageMinSeconds = 180;
                    INSTANCE.balance.sovietDamageMaxSeconds = 600;
                }
                integration();
                if (missingIntegration || missingSovietFields) save();
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

    // ----- 苏式基地计算机 -----

    /** 苏式基地计算机随机损坏的最短间隔（tick），最小 20 tick */
    public static int getSovietDamageMinTicks() {
        ensureLoaded();
        // 旧配置文件缺少该字段时 Gson 会填 0，此时回落到默认值
        int seconds = INSTANCE.balance.sovietDamageMinSeconds > 0
                ? INSTANCE.balance.sovietDamageMinSeconds : 180;
        return Math.max(20, seconds * 20);
    }

    /** 苏式基地计算机随机损坏的最长间隔（tick），最小 20 tick */
    public static int getSovietDamageMaxTicks() {
        ensureLoaded();
        int seconds = INSTANCE.balance.sovietDamageMaxSeconds > 0
                ? INSTANCE.balance.sovietDamageMaxSeconds : 600;
        return Math.max(getSovietDamageMinTicks(), seconds * 20);
    }

    // ----- 联动配置 -----

    /**
     * 旧配置文件缺少 integration 段时，Gson 会把整段填成 null，
     * 这里按需补全为默认值，避免读取时 NPE。
     */
    private static IntegrationSettings integration() {
        ensureLoaded();
        if (INSTANCE.integration == null) INSTANCE.integration = new IntegrationSettings();
        if (INSTANCE.integration.coldSweat == null) INSTANCE.integration.coldSweat = new ColdSweat();
        if (INSTANCE.integration.create == null) INSTANCE.integration.create = new Create();
        return INSTANCE.integration;
    }

    public static boolean isColdSweatIntegrationEnabled() {
        return integration().coldSweat.enabled;
    }

    public static double getColdSweatGuiDisableTemperature() {
        return integration().coldSweat.guiDisableTemperature;
    }

    public static double getColdSweatShutdownTemperature() {
        return integration().coldSweat.shutdownTemperature;
    }

    public static boolean isCreateIntegrationEnabled() {
        return integration().create.enabled;
    }

    public static int getCreateBasecoreMinRpm() {
        return Math.max(0, integration().create.basecoreMinRpm);
    }

    public static int getCreateBasecoreStress() {
        return Math.max(0, integration().create.basecoreStress);
    }

    public static int getCreateDefendMinRpm() {
        return Math.max(0, integration().create.defendMinRpm);
    }

    public static int getCreateDefendStress() {
        return Math.max(0, integration().create.defendStress);
    }

    public static int getSupplyAirCapacity() {
        return Math.max(1, integration().create.supplyAirCapacity);
    }

    public static int getSupplyRpm() {
        return Math.max(1, integration().create.supplyRpm);
    }

    public static int getSupplyStress() {
        return Math.max(1, integration().create.supplyStress);
    }

    public static int getSupplyAirIntervalTicks() {
        return Math.max(1, integration().create.supplyAirIntervalTicks);
    }

    public static int getSupplyLinkRange() {
        return Math.max(0, integration().create.supplyLinkRange);
    }

    // ----- 能源（Rs） -----

    /** 能源条容量，旧配置缺字段（0）时回落 900。 */
    public static int getEnergyCapacity() {
        ensureLoaded();
        int value = INSTANCE.balance.energyCapacity;
        return value > 0 ? value : 900;
    }

    /** 普通基地核心每消耗 1 Rs 的 tick 数（默认 2 秒 = 40 tick）。 */
    public static int getBasecoreEnergyTicksPerRs() {
        ensureLoaded();
        int seconds = INSTANCE.balance.basecoreEnergySecondsPerRs;
        return Math.max(1, (seconds > 0 ? seconds : 2) * 20);
    }

    /** 苏式基地计算机每消耗 1 Rs 的 tick 数（默认 1 秒 = 20 tick）。 */
    public static int getSovietEnergyTicksPerRs() {
        ensureLoaded();
        int seconds = INSTANCE.balance.sovietEnergySecondsPerRs;
        return Math.max(1, (seconds > 0 ? seconds : 1) * 20);
    }

    /** 尝试补充能源的间隔 tick（默认 10 秒 = 200 tick）。 */
    public static int getEnergyRefuelTicks() {
        ensureLoaded();
        int seconds = INSTANCE.balance.energyRefuelSeconds;
        return Math.max(20, (seconds > 0 ? seconds : 10) * 20);
    }

    /** 是否启用 Rs 能源系统。 */
    public static boolean isEnergyEnabled() {
        ensureLoaded();
        return INSTANCE.balance.energyEnabled;
    }

    /** 是否启用苏式基地计算机玩法。 */
    public static boolean isSovietComputerEnabled() {
        ensureLoaded();
        return INSTANCE.balance.sovietComputerEnabled;
    }

    private static void ensureLoaded() {
        if (INSTANCE == null) {
            load();
        }
    }
}
