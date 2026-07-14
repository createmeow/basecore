package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.datagen.condition.ComponentModeDisabledCondition;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class DataGen_Recipe extends RecipeProvider {
    public DataGen_Recipe(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }
    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        RecipeOutput moduleOutput = pRecipeOutput.withConditions(new ComponentModeDisabledCondition());

        // ===== Base Materials =====

        // 9 copper_ingot → 9 copper_wire
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Copper_Wire.get(), 9)
                .pattern("   ")
                .pattern("ccc")
                .pattern("   ")
                .define('c', Items.COPPER_INGOT)
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(pRecipeOutput);

        // leather + copper_wire → wire
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Wire.get(), 1)
                .pattern("###")
                .pattern("ccc")
                .pattern("###")
                .define('#', Items.LEATHER)
                .define('c', ItemRegister.Copper_Wire.get())
                .unlockedBy("has_copper_wire", has(ItemRegister.Copper_Wire.get()))
                .save(pRecipeOutput);

        // wire + iron_ingot + gold_ingot + redstone + copper_ingot + diamond → chip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Chip.get(), 1)
                .pattern("iSg")
                .pattern("SRS")
                .pattern("cSD")
                .define('i', Items.IRON_INGOT)
                .define('S', ItemRegister.Wire.get())
                .define('g', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE)
                .define('c', Items.COPPER_INGOT)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_wire", has(ItemRegister.Wire.get()))
                .save(pRecipeOutput);

        // wire + chip → electronic_component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.ElectronicComponent.get(), 1)
                .pattern("///")
                .pattern("/D/")
                .pattern("///")
                .define('/', ItemRegister.Wire.get())
                .define('D', ItemRegister.Chip.get())
                .unlockedBy("has_chip", has(ItemRegister.Chip.get()))
                .save(pRecipeOutput);

        // iron_block + wire + gold_block + chip + copper_block + diamond_block → refined_chip
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Refined_Chip.get(), 1)
                .pattern("#SG")
                .pattern("LLL")
                .pattern("CSD")
                .define('#', Items.IRON_BLOCK)
                .define('S', ItemRegister.Wire.get())
                .define('G', Items.GOLD_BLOCK)
                .define('L', ItemRegister.Chip.get())
                .define('C', Items.COPPER_BLOCK)
                .define('D', Items.DIAMOND_BLOCK)
                .unlockedBy("has_chip", has(ItemRegister.Chip.get()))
                .save(pRecipeOutput);

        // wire + iron_ingot + chip + copper_wire → module_substrate
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Module_Substrate.get(), 1)
                .pattern("###")
                .pattern("iSi")
                .pattern("ccc")
                .define('#', ItemRegister.Wire.get())
                .define('i', Items.IRON_INGOT)
                .define('S', ItemRegister.Chip.get())
                .define('c', ItemRegister.Copper_Wire.get())
                .unlockedBy("has_wire", has(ItemRegister.Wire.get()))
                .save(pRecipeOutput);

        // iron_ingot + electronic_component + wire → antenna
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Antenna.get(), 1)
                .pattern(" i ")
                .pattern("EiE")
                .pattern(" I ")
                .define('i', Items.IRON_INGOT)
                .define('E', ItemRegister.ElectronicComponent.get())
                .define('I', ItemRegister.Wire.get())
                .unlockedBy("has_electronic_component", has(ItemRegister.ElectronicComponent.get()))
                .save(pRecipeOutput);

        // ===== Disguise Devices =====

        // electronic_component + leather_helmet → disguise
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.DISGUISE.get(), 1)
                .pattern("EEE")
                .pattern("EHE")
                .pattern("   ")
                .define('E', ItemRegister.ElectronicComponent.get())
                .define('H', Items.LEATHER_HELMET)
                .unlockedBy("has_electronic_component", has(ItemRegister.ElectronicComponent.get()))
                .save(pRecipeOutput);

        // gold_ingot + disguise → advanced_disguise_device
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Advanced_Disguise_Device.get(), 1)
                .pattern("GGG")
                .pattern("GDG")
                .pattern("GGG")
                .define('G', Items.GOLD_INGOT)
                .define('D', ItemRegister.DISGUISE.get())
                .unlockedBy("has_disguise", has(ItemRegister.DISGUISE.get()))
                .save(pRecipeOutput);

        // diamond + advanced_disguise_device → extreme_disguise_device
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Extreme_Disguise_Device.get(), 1)
                .pattern("DDD")
                .pattern("DAD")
                .pattern("DDD")
                .define('D', Items.DIAMOND)
                .define('A', ItemRegister.Advanced_Disguise_Device.get())
                .unlockedBy("has_advanced_disguise", has(ItemRegister.Advanced_Disguise_Device.get()))
                .save(pRecipeOutput);

        // ===== Prosthodontic & Electromagnetic Pulse Bomb & Protection Signal Shield =====

        // refined_chip + gold_ingot + iron_ingot + iron_ingot → 4 prosthodontic (shapeless)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegister.PROSTHODONTIC.get(), 4)
                .requires(ItemRegister.Refined_Chip.get())
                .requires(Items.GOLD_INGOT)
                .requires(Items.IRON_INGOT)
                .requires(Items.IRON_INGOT)
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(pRecipeOutput);

        // gunpowder + electronic_component + antenna → electromagnetic_pulse_bomb
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.ElectromagneticPulseBombItem.get(), 1)
                .pattern("GGG")
                .pattern("GEG")
                .pattern("GAG")
                .define('G', Items.GUNPOWDER)
                .define('E', ItemRegister.ElectronicComponent.get())
                .define('A', ItemRegister.Antenna.get())
                .unlockedBy("has_electronic_component", has(ItemRegister.ElectronicComponent.get()))
                .save(pRecipeOutput);

        // refined_chip + antenna + module_substrate → protection_signal_shield
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.PROTECTION_SIGNAL_SHIELD.get(), 1)
                .pattern(" R ")
                .pattern("ASA")
                .pattern(" R ")
                .define('R', ItemRegister.Refined_Chip.get())
                .define('A', ItemRegister.Antenna.get())
                .define('S', ItemRegister.Module_Substrate.get())
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(pRecipeOutput);

        // ===== Core Items =====

        // iron_ingot + gold_ingot + electronic_component + chip + refined_chip + diamond_block + redstone + dirt → basecore
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.Basecore.get(), 1)
                .pattern("iGi")
                .pattern("EcR")
                .pattern("DrD")
                .define('i', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('E', ItemRegister.ElectronicComponent.get())
                .define('c', ItemRegister.Chip.get())
                .define('R', ItemRegister.Refined_Chip.get())
                .define('D', Items.DIAMOND_BLOCK)
                .define('r', Items.REDSTONE)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(pRecipeOutput);

        // ===== Modules: Secure → Def / CounterRecon / AutoRepair / ExpDef =====

        // wire + electronic_component + module_substrate + obsidian + refined_chip → secure_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.SecureModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("OAO")
                .define('#', ItemRegister.Wire.get())
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', Items.OBSIDIAN)
                .define('A', ItemRegister.Refined_Chip.get())
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(moduleOutput);

        // iron_ingot + secure_module + refined_chip + electronic_component + antenna → def_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.DefModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("IAI")
                .define('#', Items.IRON_INGOT)
                .define('S', ItemRegister.SecureModule.get())
                .define('C', ItemRegister.Refined_Chip.get())
                .define('I', ItemRegister.ElectronicComponent.get())
                .define('A', ItemRegister.Antenna.get())
                .unlockedBy("has_secure_module", has(ItemRegister.SecureModule.get()))
                .save(moduleOutput);

        // electronic_component + secure_module + refined_chip + ender_pearl + antenna → basecore_counter_reconnaissance_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.BasecoreCounterReconnaissanceMdoule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("IAI")
                .define('#', ItemRegister.ElectronicComponent.get())
                .define('S', ItemRegister.SecureModule.get())
                .define('C', ItemRegister.Refined_Chip.get())
                .define('I', Items.ENDER_PEARL)
                .define('A', ItemRegister.Antenna.get())
                .unlockedBy("has_secure_module", has(ItemRegister.SecureModule.get()))
                .save(moduleOutput);

        // copper_ingot + regeneration_module + refined_chip + electronic_component + antenna → auto_repair_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.AutoRepairModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("IAI")
                .define('#', Items.COPPER_INGOT)
                .define('S', ItemRegister.RegenerationModule.get())
                .define('C', ItemRegister.Refined_Chip.get())
                .define('I', ItemRegister.ElectronicComponent.get())
                .define('A', ItemRegister.Antenna.get())
                .unlockedBy("has_regeneration_module", has(ItemRegister.RegenerationModule.get()))
                .save(moduleOutput);

        // tnt + resistance_module + refined_chip + electronic_component + antenna → exp_def_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.ExpDefModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("IAI")
                .define('#', Items.TNT)
                .define('S', ItemRegister.ResistanceModule.get())
                .define('C', ItemRegister.Refined_Chip.get())
                .define('I', ItemRegister.ElectronicComponent.get())
                .define('A', ItemRegister.Antenna.get())
                .unlockedBy("has_resistance_module", has(ItemRegister.ResistanceModule.get()))
                .save(moduleOutput);

        // ===== Modules: Effect Modules ( ### / SCS / OAO pattern ) =====

        // wire + electronic_component + module_substrate + diamond_sword + refined_chip → strength_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.StrengthModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("OAO")
                .define('#', ItemRegister.Wire.get())
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', Items.DIAMOND_SWORD)
                .define('A', ItemRegister.Refined_Chip.get())
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(moduleOutput);

        // wire + electronic_component + module_substrate + diamond_chestplate + refined_chip → resistance_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.ResistanceModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("OAO")
                .define('#', ItemRegister.Wire.get())
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', Items.DIAMOND_CHESTPLATE)
                .define('A', ItemRegister.Refined_Chip.get())
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(moduleOutput);

        // wire + electronic_component + module_substrate + diamond_pickaxe + refined_chip → dig_speed_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.DigSpeedModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("OAO")
                .define('#', ItemRegister.Wire.get())
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', Items.DIAMOND_PICKAXE)
                .define('A', ItemRegister.Refined_Chip.get())
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(moduleOutput);

        // wire + electronic_component + module_substrate + rabbit_foot + chip → jump_boost_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.JumpBoostModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("OAO")
                .define('#', ItemRegister.Wire.get())
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', Items.RABBIT_FOOT)
                .define('A', ItemRegister.Chip.get())
                .unlockedBy("has_chip", has(ItemRegister.Chip.get()))
                .save(moduleOutput);

        // wire + electronic_component + module_substrate + amethyst_block + chip → regeneration_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.RegenerationModule.get(), 1)
                .pattern("###")
                .pattern("SCS")
                .pattern("OAO")
                .define('#', ItemRegister.Wire.get())
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', Items.AMETHYST_BLOCK)
                .define('A', ItemRegister.Chip.get())
                .unlockedBy("has_chip", has(ItemRegister.Chip.get()))
                .save(moduleOutput);

        // ===== Modules: Range Module ( SSS / SCS / OAO pattern ) =====

        // electronic_component + module_substrate + chip + refined_chip → range_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.RangeModule.get(), 1)
                .pattern("SSS")
                .pattern("SCS")
                .pattern("OAO")
                .define('S', ItemRegister.ElectronicComponent.get())
                .define('C', ItemRegister.Module_Substrate.get())
                .define('O', ItemRegister.Chip.get())
                .define('A', ItemRegister.Refined_Chip.get())
                .unlockedBy("has_refined_chip", has(ItemRegister.Refined_Chip.get()))
                .save(moduleOutput);

        // ===== Modules: Simple ( A / S ) pattern modules =====

        // antenna + module_substrate → thorns_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.ThornsModule.get(), 1)
                .pattern("A")
                .pattern("S")
                .define('A', ItemRegister.Antenna.get())
                .define('S', ItemRegister.Module_Substrate.get())
                .unlockedBy("has_antenna", has(ItemRegister.Antenna.get()))
                .save(moduleOutput);

        // antenna + module_substrate → movement_speed_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.MovementSpeedModule.get(), 1)
                .pattern("A")
                .pattern("S")
                .define('A', ItemRegister.Antenna.get())
                .define('S', ItemRegister.Module_Substrate.get())
                .unlockedBy("has_antenna", has(ItemRegister.Antenna.get()))
                .save(moduleOutput);

        // antenna + module_substrate → dig_slowdown_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.DigSlowdownModule.get(), 1)
                .pattern("A")
                .pattern("S")
                .define('A', ItemRegister.Antenna.get())
                .define('S', ItemRegister.Module_Substrate.get())
                .unlockedBy("has_antenna", has(ItemRegister.Antenna.get()))
                .save(moduleOutput);

        // antenna + module_substrate → weakness_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.WeaknessModule.get(), 1)
                .pattern("A")
                .pattern("S")
                .define('A', ItemRegister.Antenna.get())
                .define('S', ItemRegister.Module_Substrate.get())
                .unlockedBy("has_antenna", has(ItemRegister.Antenna.get()))
                .save(moduleOutput);

        // antenna + module_substrate → movement_slowdown_module
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegister.MovementSlowdownModule.get(), 1)
                .pattern("A")
                .pattern("S")
                .define('A', ItemRegister.Antenna.get())
                .define('S', ItemRegister.Module_Substrate.get())
                .unlockedBy("has_antenna", has(ItemRegister.Antenna.get()))
                .save(moduleOutput);

        // ===== Blocks =====

        // iron_ingot + refined_chip + gold_ingot + copper_ingot + wire + diamond_block → defend block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegister.DEFEND.get().asItem(), 1)
                .pattern("iEg")
                .pattern("EcG")
                .pattern("ccc")
                .define('i', Items.IRON_INGOT)
                .define('E', ItemRegister.Refined_Chip.get())
                .define('g', Items.GOLD_INGOT)
                .define('c', Items.COPPER_INGOT)
                .define('G', ItemRegister.Wire.get())
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(pRecipeOutput);

        // iron_ingot + chest → hash_chest
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegister.HASH_CHEST.get().asItem(), 1)
                .pattern("iii")
                .pattern("iCi")
                .pattern("iii")
                .define('i', Items.IRON_INGOT)
                .define('C', Items.CHEST)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(pRecipeOutput);
    }
}