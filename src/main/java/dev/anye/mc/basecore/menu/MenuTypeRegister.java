package dev.anye.mc.basecore.menu;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypeRegister {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, BaseCore.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<BaseCoreMenu>> BASECORE_MENU = registryMenuType("basecore", BaseCoreMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<DefendMenu>> DEFEND_MENU = registryMenuType("defend", DefendMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<HashChestMenu>> HashChest = registryMenuType("hash_chest", HashChestMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<BasecoreUpgradeMenu>> BASECORE_UPGRADE_MENU = registryMenuType("basecore_upgrade", BasecoreUpgradeMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registryMenuType(String name, IContainerFactory<T> factory){
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }
    public static void reg(IEventBus eventBus){
        MENUS.register(eventBus);
    }
}