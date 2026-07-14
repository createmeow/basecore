package dev.anye.mc.basecore.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.basecore.BasecoreClientHelper;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = BaseCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation Bar = new ResourceLocation("textures/gui/bars.png");
    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Pre event){
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            BlockPos blockPos = BasecoreClientHelper.getNear(minecraft.player.position(),0);
            if (blockPos != null) {
                BasecoreBlockEntityData basecoreBlockEntityData = BasecoreClientHelper.getData(blockPos);
                if (basecoreBlockEntityData != null) {
                    GuiGraphics guiGraphics = event.getGuiGraphics();
                    int w = 182,
                            y = 6,
                            cx = guiGraphics.guiWidth() >> 1
                            ,x = cx - 91;

                    PoseStack poseStack = guiGraphics.pose();
                    poseStack.pushPose();
                    int ws = w * basecoreBlockEntityData.getHealth() / basecoreBlockEntityData.getMaxHealth();
                    String name = basecoreBlockEntityData.getName();
                    if (name.isEmpty()) name = "No Name";
                    guiGraphics.drawCenteredString(minecraft.font,name,cx,y,0xff0000ff);
                    y += minecraft.font.lineHeight;

                    poseStack.translate(x, 0, 0);

                    guiGraphics.blit(Bar,0,y,0,0,w,5,256,256);
                    guiGraphics.blit(Bar,0,y,0,5,ws,5,256,256);

                    y+= 6;

                    guiGraphics.drawString(minecraft.font, basecoreBlockEntityData.getHealth() + "/" + basecoreBlockEntityData.getMaxHealth(), 0,y,0xffffff00);

                    y+= minecraft.font.lineHeight;

                    if (basecoreBlockEntityData.getInterferenceTime() > 0) {

                        guiGraphics.drawString(minecraft.font, Component.translatable("gui.basecore.basecore.interference", basecoreBlockEntityData.getInterferenceTime() / 20), 0, y, 0xffffff00);
                    }
                    poseStack.popPose();
                }
            }

        }
    }
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        if (event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.END){
            BasecoreClientHelper.tick();
        }
    }
}
