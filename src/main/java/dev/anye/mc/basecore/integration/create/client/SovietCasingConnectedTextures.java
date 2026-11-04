package dev.anye.mc.basecore.integration.create.client;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 为苏式计算机外壳启用 Create 式连接纹理（Connected Textures）。
 * <p>
 * basecore 对 Create 保持软依赖，因此这里全部通过反射调用：
 * <ol>
 *     <li>{@code CTSpriteShifter.getCT(AllCTTypes.CROSS, block/x, block/x_connected)} 生成 SpriteShiftEntry；</li>
 *     <li>{@code CreateClient.CASING_CONNECTIVITY.makeCasing(block, shift)} 让相邻同名外壳可连接；</li>
 *     <li>在模型烘焙完成后用 {@code CTModel} 包裹外壳模型，按四邻接关系重映射 UV。</li>
 * </ol>
 * 选择 {@code AllCTTypes.CROSS}：sheetSize=4（4x4 个 16px 贴片 = 64x64 的连接图集），
 * 上下文只要求上下左右四个方向，纹理索引为 {@code (up?1)+(down?2)+(left?4)+(right?8)}，
 * 对应贴片位置为 {@code (index%4, index/4)}，未连接的一侧绘制边框。
 * <p>
 * 连接图集本身无需额外声明：原版 {@code minecraft:atlases/blocks.json} 自带
 * {@code directory -> block} 源，所有 {@code assets/<ns>/textures/block/*.png} 都会被 stitch 进方块图集。
 */
@EventBusSubscriber(modid = BaseCore.MOD_ID, value = Dist.CLIENT)
public final class SovietCasingConnectedTextures {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");

    private static final String CT_PACKAGE = "com.simibubi.create.foundation.block.connected.";
    private static final String CASING_TEXTURE = "block/soviet_computer_casing";

    /** CTModel(BakedModel, ConnectedTextureBehaviour) 构造器。 */
    private static Constructor<?> ctModelConstructor;
    /** EncasedCTBehaviour 实例（ConnectedTextureBehaviour 的子类）。 */
    private static Object behaviour;
    private static boolean ready = false;

    private SovietCasingConnectedTextures() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!CREATE_LOADED) return;
        event.enqueueWork(SovietCasingConnectedTextures::register);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        if (!ready) return;
        Block casing = BlockRegister.SOVIET_COMPUTER_CASING.get();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(casing);
        for (BlockState state : casing.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation location = BlockModelShaper.stateToModelLocation(blockId, state);
            BakedModel original = event.getModels().get(location);
            if (original == null) continue;
            try {
                event.getModels().put(location, (BakedModel) ctModelConstructor.newInstance(original, behaviour));
            } catch (Throwable t) {
                LOGGER.warn("Failed to wrap soviet computer casing model with connected textures", t);
                return;
            }
        }
    }

    private static void register() {
        try {
            Object crossType = findCrossType();
            if (crossType == null) {
                LOGGER.warn("Create connected-texture type CROSS not found, casing stays plain");
                return;
            }

            ResourceLocation original = ResourceLocation.fromNamespaceAndPath(BaseCore.MOD_ID, CASING_TEXTURE);
            ResourceLocation connected = ResourceLocation.fromNamespaceAndPath(BaseCore.MOD_ID, CASING_TEXTURE + "_connected");

            Class<?> shifter = Class.forName(CT_PACKAGE + "CTSpriteShifter");
            Method getCT = null;
            for (Method method : shifter.getMethods()) {
                if (method.getName().equals("getCT") && method.getParameterCount() == 3) {
                    getCT = method;
                    break;
                }
            }
            if (getCT == null) return;
            Object shift = getCT.invoke(null, crossType, original, connected);
            if (shift == null) return;

            // 注册到 Create 的机壳连接表，否则 EncasedCTBehaviour 的连接判定恒为 false
            Class<?> createClientClass = Class.forName("com.simibubi.create.CreateClient");
            Object connectivity = createClientClass.getField("CASING_CONNECTIVITY").get(null);
            Block casing = BlockRegister.SOVIET_COMPUTER_CASING.get();
            connectivity.getClass()
                    .getMethod("makeCasing", Block.class, shift.getClass())
                    .invoke(connectivity, casing, shift);

            Class<?> behaviourClass = Class.forName(CT_PACKAGE + "ConnectedTextureBehaviour");
            Class<?> encasedBehaviour = Class.forName("com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour");
            behaviour = encasedBehaviour.getConstructor(shift.getClass()).newInstance(shift);
            ctModelConstructor = Class.forName(CT_PACKAGE + "CTModel")
                    .getConstructor(BakedModel.class, behaviourClass);
            ready = true;
        } catch (Throwable t) {
            LOGGER.warn("Failed to register connected textures for soviet computer casing", t);
        }
    }

    private static Object findCrossType() throws ClassNotFoundException {
        Class<?> allTypes = Class.forName(CT_PACKAGE + "AllCTTypes");
        for (Object constant : allTypes.getEnumConstants()) {
            if (constant instanceof Enum<?> e && e.name().equals("CROSS")) return constant;
        }
        return null;
    }
}
