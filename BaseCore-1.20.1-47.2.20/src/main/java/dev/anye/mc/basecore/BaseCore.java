package dev.anye.mc.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.entity.EntityTypeRegister;
import dev.anye.mc.basecore.item.CreativeTabs;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.lib._File;
import dev.anye.mc.basecore.menu.MenuTypeRegister;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BaseCore.MOD_ID)
public class BaseCore
{
    public static final String MOD_ID = "basecore";
    public static final String CONFIG_DIR = _File.getFileFullPathWithRun("config","basecore");
    public static final String CONFIG_DATA_DIR = _File.getFilePath(CONFIG_DIR,"data");

    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        _File.checkAndCreateDir(CONFIG_DIR);
        _File.checkAndCreateDir(CONFIG_DATA_DIR);
    }

    public BaseCore()
    {
        this(FMLJavaModLoadingContext.get());
    }
    public BaseCore(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        EasyNetRegister.register(modEventBus);
        EffectRegister.reg(modEventBus);
        BlockRegister.reg(modEventBus);
        MenuTypeRegister.reg(modEventBus);
        BlockEntityRegister.reg(modEventBus);
        ItemRegister.register(modEventBus);
        EntityTypeRegister.reg(modEventBus);
        CreativeTabs.reg(modEventBus);
        NetReg.reg(modEventBus);
    }
}
