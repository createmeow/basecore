package dev.anye.mc.basecore;

import dev.anye.mc.basecore.api.BasecoreUpgradeApi;
import dev.anye.mc.basecore.api.UpgradeDefinition;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.datagen.condition.ComponentModeDisabledCondition;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.entity.EntityTypeRegister;
import dev.anye.mc.basecore.event.ClientForgeEvent;
import dev.anye.mc.basecore.event.ForgeEvent;
import dev.anye.mc.basecore.init.DataAttachmentInit;
import dev.anye.mc.basecore.item.CreativeTabs;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.component.ItemComponentInit;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import dev.anye.mc.basecore.menu.MenuTypeRegister;
import dev.anye.mc.basecore.net.easy_net.EasyNetRegister;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.IntSupplier;

@Mod(BaseCore.MOD_ID)
public class BaseCore {
    public static final String MOD_ID = "basecore";

    public BaseCore(IEventBus modEventBus) {
        // Load config
        BasecoreConfig.load();

        BlockRegister.reg(modEventBus);
        BlockEntityRegister.reg(modEventBus);
        ItemRegister.reg(modEventBus);
        CreativeTabs.reg(modEventBus);
        MenuTypeRegister.reg(modEventBus);
        EntityTypeRegister.reg(modEventBus);
        EffectRegister.reg(modEventBus);

        // Register data attachments & components
        DataAttachmentInit.reg(modEventBus);
        ItemComponentInit.reg(modEventBus);

        NeoForge.EVENT_BUS.register(ForgeEvent.class);
        NeoForge.EVENT_BUS.register(ClientForgeEvent.class);

        modEventBus.addListener(RegisterPayloadHandlersEvent.class, EasyNetRegister::register);

        // Register recipe condition codec for component mode
        modEventBus.addListener(RegisterEvent.class, event -> {
            event.register(ResourceKey.createRegistryKey(ResourceLocation.parse("neoforge:condition_codecs")),
                    ResourceLocation.parse("basecore:component_mode_disabled"),
                    () -> ComponentModeDisabledCondition.CODEC);
        });

        // Register built-in upgrades through the public API
        registerBuiltinUpgrades();
    }

    /** Register all built-in upgrades via {@link BasecoreUpgradeApi} (component mode upgrade UI). */
    private static void registerBuiltinUpgrades() {
        // ----- 基地核心升级 -----
        registerUpgrade(ItemRegister.RangeModule, BasecoreConfig::getUpgradeRangeCost);
        registerUpgrade(ItemRegister.DefModule, BasecoreConfig::getUpgradeDefenseCost);
        registerUpgrade(ItemRegister.SecureModule, BasecoreConfig::getUpgradeSecureCost);
        registerUpgrade(ItemRegister.AutoRepairModule, BasecoreConfig::getUpgradeAutoRepairCost);
        registerUpgrade(ItemRegister.ThornsModule, BasecoreConfig::getUpgradeThornsCost);
        registerUpgrade(ItemRegister.ExpDefModule, BasecoreConfig::getUpgradeExpDefCost);
        registerUpgrade(ItemRegister.BasecoreCounterReconnaissanceMdoule, BasecoreConfig::getUpgradeCounterReconCost);
        registerUpgrade(ItemRegister.StrengthModule, BasecoreConfig::getUpgradeStrengthCost);
        registerUpgrade(ItemRegister.JumpBoostModule, BasecoreConfig::getUpgradeJumpBoostCost);
        registerUpgrade(ItemRegister.RegenerationModule, BasecoreConfig::getUpgradeRegenerationCost);
        registerUpgrade(ItemRegister.ResistanceModule, BasecoreConfig::getUpgradeResistanceCost);
        registerUpgrade(ItemRegister.DigSpeedModule, BasecoreConfig::getUpgradeDigSpeedCost);
        registerUpgrade(ItemRegister.MovementSpeedModule, BasecoreConfig::getUpgradeMovementSpeedCost);
        // 入侵干扰模块（对入侵玩家生效）
        registerUpgrade(ItemRegister.DigSlowdownModule, BasecoreConfig::getUpgradeDigSlowdownCost);
        registerUpgrade(ItemRegister.WeaknessModule, BasecoreConfig::getUpgradeWeaknessCost);
        registerUpgrade(ItemRegister.MovementSlowdownModule, BasecoreConfig::getUpgradeMovementSlowdownCost);

        // ----- 防御炮升级 -----
        registerDefendUpgrade(ItemRegister.RangeModule, BasecoreConfig::getUpgradeDefendRangeCost);
        registerDefendUpgrade(ItemRegister.DefModule, BasecoreConfig::getUpgradeDefendDefenseCost);
        registerDefendUpgrade(ItemRegister.ThornsModule, BasecoreConfig::getUpgradeDefendThornsCost);
        registerDefendUpgrade(ItemRegister.AutoRepairModule, BasecoreConfig::getUpgradeDefendAutoRepairCost);
        registerDefendUpgrade(ItemRegister.StrengthModule, BasecoreConfig::getUpgradeDefendStrengthCost);
    }

    /** 注册一个仅出现在基地核心升级界面的升级项。 */
    private static void registerUpgrade(DeferredHolder<net.minecraft.world.item.Item, ? extends BasecoreModuleItem> module,
                                        IntSupplier partCost) {
        BasecoreUpgradeApi.registerUpgrade(new UpgradeDefinition(module::get, partCost, true, false));
    }

    /** 注册一个仅出现在防御炮升级界面的升级项。 */
    private static void registerDefendUpgrade(DeferredHolder<net.minecraft.world.item.Item, ? extends BasecoreModuleItem> module,
                                              IntSupplier partCost) {
        BasecoreUpgradeApi.registerUpgrade(new UpgradeDefinition(module::get, partCost, false, true));
    }
}
