package dev.anye.mc.basecore.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DataGen_Recipe extends RecipeProvider implements IConditionBuilder {
    public DataGen_Recipe(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        /*
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUBlocks.INDEX.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Items.SNOW_BLOCK)
                .define('B', Items.CRYING_OBSIDIAN)
                .define('C', Items.DRAGON_EGG)
                .unlockedBy(getHasName(Items.DRAGON_EGG),has(Items.DRAGON_EGG))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUBlocks.EQUIPMENT_ENHANCER.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("DDD")
                .define('A', Items.REDSTONE_BLOCK)
                .define('B', Items.NETHER_STAR)
                .define('C', Items.DRAGON_EGG)
                .define('D', Items.OBSIDIAN)
                .unlockedBy(getHasName(Items.DRAGON_EGG),has(Items.DRAGON_EGG))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.YANG.get())
                .pattern("AAA")
                .define('A', Items.QUARTZ_BLOCK)
                .unlockedBy(getHasName(Items.QUARTZ_BLOCK),has(Items.QUARTZ_BLOCK))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.YIN.get())
                .pattern("AAA")
                .define('A', Items.OBSIDIAN)
                .unlockedBy(getHasName(Items.OBSIDIAN),has(Items.OBSIDIAN))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.QIAN.get())
                .pattern("A")
                .pattern("A")
                .pattern("A")
                .define('A', NUItems.YANG.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.KUN.get())
                .pattern("A")
                .pattern("A")
                .pattern("A")
                .define('A', NUItems.YIN.get())
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.DUI.get())
                .pattern("B")
                .pattern("A")
                .pattern("A")
                .define('B', NUItems.YIN.get())
                .define('A', NUItems.YANG.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.GEN.get())
                .pattern("B")
                .pattern("A")
                .pattern("A")
                .define('A', NUItems.YIN.get())
                .define('B', NUItems.YANG.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.KAN.get())
                .pattern("A")
                .pattern("B")
                .pattern("A")
                .define('A', NUItems.YIN.get())
                .define('B', NUItems.YANG.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.LI.get())
                .pattern("A")
                .pattern("B")
                .pattern("A")
                .define('A', NUItems.YANG.get())
                .define('B', NUItems.YIN.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.XUN.get())
                .pattern("A")
                .pattern("A")
                .pattern("B")
                .define('A', NUItems.YANG.get())
                .define('B', NUItems.YIN.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUItems.ZHEN.get())
                .pattern("A")
                .pattern("A")
                .pattern("B")
                .define('A', NUItems.YIN.get())
                .define('B', NUItems.YANG.get())
                .unlockedBy(getHasName(NUItems.YANG.get()),has(NUItems.YANG.get()))
                .unlockedBy(getHasName(NUItems.YIN.get()),has(NUItems.YIN.get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NUBlocks.THE_EIGHT_TRIGRAMS.get())
                .pattern("ABC")
                .pattern("DEF")
                .pattern("GHI")
                .define('A', NUItems.ZHEN.get())
                .define('B', NUItems.QIAN.get())
                .define('C', NUItems.DUI.get())
                .define('D', NUItems.KAN.get())
                .define('E', Items.NETHERITE_SWORD)
                .define('F', NUItems.LI.get())
                .define('G', NUItems.GEN.get())
                .define('H', NUItems.KUN.get())
                .define('I', NUItems.XUN.get())
                .unlockedBy(getHasName(Items.NETHERITE_SWORD),has(Items.NETHERITE_SWORD))
                .save(consumer);

         */
    }
}
