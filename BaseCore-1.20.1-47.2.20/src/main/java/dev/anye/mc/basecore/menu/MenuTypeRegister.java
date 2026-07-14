package dev.anye.mc.basecore.menu;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypeRegister {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BaseCore.MOD_ID);
    public static final RegistryObject<MenuType<BaseCoreMenu>> BASECORE_MENU = registryMenuType("basecore", BaseCoreMenu::new);
    public static final RegistryObject<MenuType<DefendMenu>> DEFEND_MENU = registryMenuType("defend", DefendMenu::new);
    public static final RegistryObject<MenuType<HashChestMenu>> HashChest = registryMenuType("hash_chest", HashChestMenu::new);

    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registryMenuType(String name, IContainerFactory<T> factory){
        return MENUS.register(name,()-> IForgeMenuType.create(factory));
    }
    public static void reg(IEventBus eventBus){
        MENUS.register(eventBus);
    }
}
