package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.item.component.LootPartBundleItem;
import dev.anye.mc.basecore.item.component.PartBundleItem;
import dev.anye.mc.basecore.item.component.PartItem;
import dev.anye.mc.basecore.item.module.*;
import dev.anye.mc.basecore.item.module.basecore.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegister {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BaseCore.MOD_ID);

    public static final DeferredHolder<Item, Item> Basecore = ITEMS.register("basecore", BasecoreItem::new);

    public static final DeferredHolder<Item, RangeModuleItemItem> RangeModule = ITEMS.register("range_module", RangeModuleItemItem::new);
    public static final DeferredHolder<Item, SecureModuleItem> SecureModule = ITEMS.register("secure_module", SecureModuleItem::new);
    public static final DeferredHolder<Item, DefModuleItem> DefModule = ITEMS.register("def_module", DefModuleItem::new);
    public static final DeferredHolder<Item, AutoRepairModuleItem> AutoRepairModule = ITEMS.register("auto_repair_module", AutoRepairModuleItem::new);
    public static final DeferredHolder<Item, ThornsModuleItem> ThornsModule = ITEMS.register("thorns_module", ThornsModuleItem::new);
    public static final DeferredHolder<Item, ExpDefModuleItem> ExpDefModule = ITEMS.register("exp_def_module", ExpDefModuleItem::new);
    public static final DeferredHolder<Item, BasecoreCounterReconnaissanceMdoule> BasecoreCounterReconnaissanceMdoule = ITEMS.register("basecore_counter_reconnaissance_module", BasecoreCounterReconnaissanceMdoule::new);

    public static final DeferredHolder<Item, EffectModuleItem> StrengthModule = ITEMS.register("strength_module", () -> new EffectModuleItem(MobEffects.DAMAGE_BOOST, 200) {
        @Override
        public int getDefendMaxCount() {
            return 2;
        }
    });
    public static final DeferredHolder<Item, EffectModuleItem> JumpBoostModule = ITEMS.register("jump_boost_module", () -> new EffectModuleItem(MobEffects.JUMP, 200));
    public static final DeferredHolder<Item, EffectModuleItem> RegenerationModule = ITEMS.register("regeneration_module", () -> new EffectModuleItem(MobEffects.REGENERATION, 200));
    public static final DeferredHolder<Item, EffectModuleItem> ResistanceModule = ITEMS.register("resistance_module", () -> new EffectModuleItem(MobEffects.DAMAGE_RESISTANCE, 200));
    public static final DeferredHolder<Item, EffectModuleItem> DigSpeedModule = ITEMS.register("dig_speed_module", () -> new EffectModuleItem(MobEffects.DIG_SPEED, 200));
    public static final DeferredHolder<Item, EffectModuleItem> MovementSpeedModule = ITEMS.register("movement_speed_module", () -> new EffectModuleItem(MobEffects.MOVEMENT_SPEED, 200));

    public static final DeferredHolder<Item, EffectModuleItem> DigSlowdownModule = ITEMS.register("dig_slowdown_module", () -> new EffectModuleItem(MobEffects.DIG_SLOWDOWN, 200, true));
    public static final DeferredHolder<Item, EffectModuleItem> WeaknessModule = ITEMS.register("weakness_module", () -> new EffectModuleItem(MobEffects.WEAKNESS, 200, true));
    public static final DeferredHolder<Item, EffectModuleItem> MovementSlowdownModule = ITEMS.register("movement_slowdown_module", () -> new EffectModuleItem(MobEffects.MOVEMENT_SLOWDOWN, 200, true));

    public static final DeferredHolder<Item, DisguiseItem> DISGUISE = ITEMS.register("disguise", () -> new DisguiseItem(Rarity.COMMON, 0, 1200));
    public static final DeferredHolder<Item, DisguiseItem> Advanced_Disguise_Device = ITEMS.register("advanced_disguise_device", () -> new DisguiseItem(Rarity.UNCOMMON, 1, 1800));
    public static final DeferredHolder<Item, DisguiseItem> Extreme_Disguise_Device = ITEMS.register("extreme_disguise_device", () -> new DisguiseItem(Rarity.RARE, 2, 2400));

    public static final DeferredHolder<Item, Item> ElectronicComponent = ITEMS.register("electronic_component", () -> new EasyItem(Rarity.RARE, 64));
    public static final DeferredHolder<Item, Item> Chip = ITEMS.register("chip", () -> new EasyItem(Rarity.UNCOMMON, 64));
    public static final DeferredHolder<Item, Item> Refined_Chip = ITEMS.register("refined_chip", () -> new EasyItem(Rarity.RARE, 64));
    public static final DeferredHolder<Item, Item> Wire = ITEMS.register("wire", () -> new EasyItem(Rarity.UNCOMMON, 64));
    public static final DeferredHolder<Item, Item> Antenna = ITEMS.register("antenna", () -> new EasyItem(Rarity.UNCOMMON, 64));
    public static final DeferredHolder<Item, Item> Copper_Wire = ITEMS.register("copper_wire", () -> new EasyItem(Rarity.COMMON, 64));
    public static final DeferredHolder<Item, Item> Module_Substrate = ITEMS.register("module_substrate", () -> new EasyItem(Rarity.RARE, 64));

    public static final DeferredHolder<Item, ProtectionSignalShieldItem> PROTECTION_SIGNAL_SHIELD = ITEMS.register("protection_signal_shield", ProtectionSignalShieldItem::new);
    public static final DeferredHolder<Item, ProsthodonticItem> PROSTHODONTIC = ITEMS.register("prosthodontic", ProsthodonticItem::new);
    public static final DeferredHolder<Item, ElectromagneticPulseBombItem> ElectromagneticPulseBombItem = ITEMS.register("electromagnetic_pulse_bomb", ElectromagneticPulseBombItem::new);

    // Component mode: Part items
    public static final DeferredHolder<Item, PartItem> PART = ITEMS.register("part", PartItem::new);
    public static final DeferredHolder<Item, PartBundleItem> PART_BUNDLE = ITEMS.register("part_bundle", PartBundleItem::new);

    // Airdrop loot: realityvalue-style part bundles
    public static final DeferredHolder<Item, LootPartBundleItem> SMALL_PART_BUNDLE = ITEMS.register("small_part_bundle", () -> new LootPartBundleItem(Rarity.UNCOMMON, 5, 35));
    public static final DeferredHolder<Item, LootPartBundleItem> MEDIUM_PART_BUNDLE = ITEMS.register("medium_part_bundle", () -> new LootPartBundleItem(Rarity.RARE, 34, 60));
    public static final DeferredHolder<Item, LootPartBundleItem> LARGE_PART_BUNDLE = ITEMS.register("large_part_bundle", () -> new LootPartBundleItem(Rarity.EPIC, 59, 100));

    public static void reg(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}