package dev.anye.mc.basecore.api;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * BaseCore 公共扩展 API。
 * <p>
 * 社区模组可在自己的模组构造阶段调用本类注册自定义升级项，
 * 注册后会自动出现在基地核心/防御炮的"零件升级"界面中。
 * </p>
 * <pre>{@code
 * // 示例：注册一个自定义模块，仅在基地核心的升级界面中出现，售价 200 零件
 * BasecoreUpgradeApi.registerUpgrade(new UpgradeDefinition(
 *         ItemRegister.MyModule::get,
 *         () -> 200
 * ));
 *
 * // 示例：同时出现在基地核心与防御炮界面
 * BasecoreUpgradeApi.registerUpgrade(new UpgradeDefinition(
 *         ItemRegister.MyModule::get,
 *         () -> 300,
 *         true, true
 * ));
 * }</pre>
 */
public final class BasecoreUpgradeApi {
    private static final List<UpgradeDefinition> UPGRADES = new ArrayList<>();
    /** 防御炮模块数量上限覆盖（key 为模块物品实例）。 */
    private static final Map<Item, Integer> DEFEND_MAX = new HashMap<>();

    private BasecoreUpgradeApi() {
    }

    /**
     * 注册一个自定义升级项。
     *
     * @param definition 升级定义，不可为 null
     */
    public static void registerUpgrade(UpgradeDefinition definition) {
        UPGRADES.add(Objects.requireNonNull(definition, "definition"));
    }

    /**
     * 便捷重载：注册一个仅适用于基地核心、售价固定的升级项。
     *
     * @param module   模块物品（延迟解析，例如 {@code ItemRegister.MyModule::get}）
     * @param partCost 固定零件售价
     */
    public static void registerUpgrade(java.util.function.Supplier<? extends BasecoreModuleItem> module, int partCost) {
        registerUpgrade(new UpgradeDefinition(module, () -> partCost));
    }

    /**
     * 便捷重载：注册一个仅适用于基地核心、售价动态计算的升级项。
     *
     * @param module   模块物品（延迟解析）
     * @param partCost 零件售价（动态）
     */
    public static void registerUpgrade(java.util.function.Supplier<? extends BasecoreModuleItem> module,
                                       java.util.function.IntSupplier partCost) {
        registerUpgrade(new UpgradeDefinition(module, partCost));
    }

    /**
     * 覆盖防御炮中某个模块的最大数量上限（默认 4）。
     * <p>
     * 注意：请在物品注册完成后调用（例如 {@code FMLCommonSetupEvent} 中），
     * 直接传 {@code ItemRegister.X.get()} 在模组构造阶段会得到 null。
     * </p>
     *
     * @param module 模块物品
     * @param max    最大数量；小于 0 表示无上限
     */
    public static void setDefendModuleMax(Item module, int max) {
        DEFEND_MAX.put(Objects.requireNonNull(module, "module"), max);
    }

    // ===== 内部查询（供 basecore 自身使用，不推荐外部调用） =====

    /** 返回全部已注册的升级项（不可变快照）。 */
    public static List<UpgradeDefinition> getUpgrades() {
        return List.copyOf(UPGRADES);
    }

    /** 返回防御炮模块数量上限覆盖值；未覆盖返回 null。 */
    public static Integer getDefendModuleMax(Item module) {
        return DEFEND_MAX.get(module);
    }
}
