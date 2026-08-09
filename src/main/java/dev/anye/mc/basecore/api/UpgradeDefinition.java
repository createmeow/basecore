package dev.anye.mc.basecore.api;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 定义一个可在"零件升级"界面（基地核心 / 防御炮）中购买的升级项。
 * <p>
 * 社区模组通过 {@link BasecoreUpgradeApi#registerUpgrade(UpgradeDefinition)} 注册自定义升级。
 * 升级对应的模块物品必须继承 {@link BasecoreModuleItem}，才能被基地核心/防御炮识别并生效。
 * </p>
 * <p>
 * 由于注册通常发生在模组构造阶段（此时物品尚未注册完成），模块使用 {@link Supplier} 延迟解析，
 * 购买/展示时才会真正取回物品实例。
 * </p>
 */
public final class UpgradeDefinition {
    private final Supplier<? extends BasecoreModuleItem> moduleSupplier;
    private final IntSupplier partCost;
    private final boolean appliesToBaseCore;
    private final boolean appliesToDefend;

    /**
     * 创建一个仅适用于基地核心的升级项。
     *
     * @param module   模块物品（延迟解析，例如 {@code ItemRegister.MyModule::get}）
     * @param partCost 购买所需零件数（每次购买时实时取值，便于动态调整）
     */
    public UpgradeDefinition(Supplier<? extends BasecoreModuleItem> module, IntSupplier partCost) {
        this(module, partCost, true, false);
    }

    /**
     * 创建一个升级项，并指定其适用的目标。
     *
     * @param module            模块物品（延迟解析）
     * @param partCost          购买所需零件数
     * @param appliesToBaseCore 是否出现在基地核心的升级界面
     * @param appliesToDefend   是否出现在防御炮的升级界面
     */
    public UpgradeDefinition(Supplier<? extends BasecoreModuleItem> module, IntSupplier partCost,
                             boolean appliesToBaseCore, boolean appliesToDefend) {
        this.moduleSupplier = Objects.requireNonNull(module, "module");
        this.partCost = Objects.requireNonNull(partCost, "partCost");
        this.appliesToBaseCore = appliesToBaseCore;
        this.appliesToDefend = appliesToDefend;
    }

    /** 返回升级对应的模块物品。若尚未注册完成可能返回 null。 */
    public BasecoreModuleItem getModule() {
        return moduleSupplier.get();
    }

    /** 返回购买该升级所需的零件数。 */
    public int getPartCost() {
        return partCost.getAsInt();
    }

    public boolean appliesToBaseCore() {
        return appliesToBaseCore;
    }

    public boolean appliesToDefend() {
        return appliesToDefend;
    }
}
