package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.item.module.*;
import dev.anye.mc.basecore.item.module.basecore.*;
import dev.anye.mc.basecore.item.module.defend.DefendDamageModuleItem;
import dev.anye.mc.basecore.item.module.defend.DefendHealthModuleItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegister {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BaseCore.MOD_ID);
    public static final RegistryObject<Item> Basecore = ITEMS.register("basecore",BasecoreItem::new);

    public static final RegistryObject<BasecoreModuleItem> RangeModule = ITEMS.register("range_module", RangeModuleItemItem::new);
    public static final RegistryObject<BasecoreModuleItem> SecureModule = ITEMS.register("secure_module", SecureModuleItem::new);
    public static final RegistryObject<BasecoreModuleItem> DefModule = ITEMS.register("def_module", DefModuleItem::new);
    public static final RegistryObject<BasecoreModuleItem> AutoRepairModule = ITEMS.register("auto_repair_module", AutoRepairModuleItem::new);
    public static final RegistryObject<BasecoreModuleItem> ThornsModule = ITEMS.register("thorns_module", ThornsModuleItem::new);
    public static final RegistryObject<BasecoreModuleItem> ExpDefModule = ITEMS.register("exp_def_module", ExpDefModuleItem::new);
    public static final RegistryObject<BasecoreCounterReconnaissanceMdoule> BasecoreCounterReconnaissanceMdoule = ITEMS.register("basecore_counter_reconnaissance_module", BasecoreCounterReconnaissanceMdoule::new);

    public static final RegistryObject<BasecoreModuleItem> StrengthModule = ITEMS.register("strength_module", () -> new EffectModuleItem(MobEffects.DAMAGE_BOOST,200));
    public static final RegistryObject<BasecoreModuleItem> JumpBoostModule = ITEMS.register("jump_boost_module",  () -> new EffectModuleItem(MobEffects.JUMP,200));
    public static final RegistryObject<BasecoreModuleItem> RegenerationModule = ITEMS.register("regeneration_module",  () -> new EffectModuleItem(MobEffects.REGENERATION,200));
    public static final RegistryObject<BasecoreModuleItem> ResistanceModule = ITEMS.register("resistance_module",  () -> new EffectModuleItem(MobEffects.DAMAGE_RESISTANCE,200));
    public static final RegistryObject<BasecoreModuleItem> DigSpeedModule = ITEMS.register("dig_speed_module",  () -> new EffectModuleItem(MobEffects.DIG_SPEED,200));
    public static final RegistryObject<BasecoreModuleItem> MovementSpeedModule = ITEMS.register("movement_speed_module",  () -> new EffectModuleItem(MobEffects.MOVEMENT_SPEED,200));

    public static final RegistryObject<BasecoreModuleItem> DigSlowdownModule = ITEMS.register("dig_slowdown_module",  () -> new EffectModuleItem(MobEffects.DIG_SLOWDOWN,200,true));
    public static final RegistryObject<BasecoreModuleItem> WeaknessModule = ITEMS.register("weakness_module",  () -> new EffectModuleItem(MobEffects.WEAKNESS,200,true));
    public static final RegistryObject<BasecoreModuleItem> MovementSlowdownModule = ITEMS.register("movement_slowdown_module",  () -> new EffectModuleItem(MobEffects.MOVEMENT_SLOWDOWN,200,true));







    public static final RegistryObject<DefendHealthModuleItem> DefendHealthModule = ITEMS.register("defend_health_module",  DefendHealthModuleItem::new);
    public static final RegistryObject<DefendDamageModuleItem> DefendDamageModule = ITEMS.register("defend_damage_module",  DefendDamageModuleItem::new);








    public static final RegistryObject<DisguiseItem> DISGUISE = ITEMS.register("disguise",()->new DisguiseItem(Rarity.COMMON,0,1200));
    public static final RegistryObject<DisguiseItem> Advanced_Disguise_Device = ITEMS.register("advanced_disguise_device",()->new DisguiseItem(Rarity.UNCOMMON,1,1800));
    public static final RegistryObject<DisguiseItem> Extreme_Disguise_Device = ITEMS.register("extreme_disguise_device",()->new DisguiseItem(Rarity.RARE,2,2400));








    public static final RegistryObject<Item> ElectronicComponent = ITEMS.register("electronic_component", ()->new EasyItem(Rarity.RARE,64));
    public static final RegistryObject<Item> Chip = ITEMS.register("chip", ()->new EasyItem(Rarity.UNCOMMON,64));
    public static final RegistryObject<Item> Refined_Chip = ITEMS.register("refined_chip", ()->new EasyItem(Rarity.RARE,64));
    public static final RegistryObject<Item> Wire = ITEMS.register("wire", ()->new EasyItem(Rarity.UNCOMMON,64));
    public static final RegistryObject<Item> Antenna = ITEMS.register("antenna", ()->new EasyItem(Rarity.UNCOMMON,64));
    public static final RegistryObject<Item> Copper_Wire = ITEMS.register("copper_wire", ()->new EasyItem(Rarity.COMMON,64));
    public static final RegistryObject<Item> Module_Substrate = ITEMS.register("module_substrate", ()->new EasyItem(Rarity.RARE,64));

    public static final RegistryObject<ProtectionSignalShieldItem> PROTECTION_SIGNAL_SHIELD = ITEMS.register("protection_signal_shield",ProtectionSignalShieldItem::new);
    public static final RegistryObject<ProsthodonticItem> PROSTHODONTIC = ITEMS.register("prosthodontic",ProsthodonticItem::new);
    public static final RegistryObject<ElectromagneticPulseBombItem> ElectromagneticPulseBombItem = ITEMS.register("electromagnetic_pulse_bomb",ElectromagneticPulseBombItem::new);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
