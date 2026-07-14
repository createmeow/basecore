package dev.anye.mc.basecore.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.UUID;

public class BasecoreCommands {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FAILED = 0;
    private static final int SUCCESS = Command.SINGLE_SUCCESS;
    private static void sendCommandMsg(CommandContext<CommandSourceStack> context,String msg){
        context.getSource().sendSuccess(() -> Component.literal(msg), false);
    }
    public static int leave(CommandContext<CommandSourceStack> context){
        if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer){
            String basename = context.getArgument("basename",String.class);
            if (!basename.isEmpty()){
                boolean[] r = {false};
                BasecoreServerHelper.getBaseCoreBlockEntities().forEach(be ->{
                    if (be.getEntityData().getName().equals(basename)) {
                        r[0] = true;
                        if (be.getOwner() == null) return;
                        if (!be.getOwner().equals(serverPlayer.getUUID())) {
                            if (be.getEntityData().hasPermission(serverPlayer.getUUID())) {
                                be.getEntityData().rmUser(serverPlayer.getUUID());
                                sendCommandMsg(context, "您已离开基地【" + basename + "】");
                            } else sendCommandMsg(context, "您不位于基地【" + basename + "】");
                        }else sendCommandMsg(context,"您无法离开自己的基地");
                    }
                });
                if (r[0]) return SUCCESS;
            }
            context.getSource().sendSuccess(() -> Component.literal("基地不存在或未加载"), false);
            return SUCCESS;
        }
        return FAILED;
    }
    public static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer){
            String basename = context.getArgument("basename",String.class);
            if (!basename.isEmpty()){

                Player player = EntityArgument.getPlayer(context, "player");
                boolean[] r = {false};
                BasecoreServerHelper.getBaseCoreBlockEntities().forEach(be ->{
                    if (be.getEntityData().getName().equals(basename)) {
                        r[0] = true;
                        if (be.getOwner() == null) return;
                        if (be.getOwner().equals(serverPlayer.getUUID())) {
                            if (!be.getEntityData().hasPermission(player.getUUID())) {
                                be.getEntityData().addUser(player.getUUID());
                                sendCommandMsg(context, "已添加对方为基地成员");
                            } else sendCommandMsg(context, "对方已是基地成员");
                        }else sendCommandMsg(context,"您无权限为基地【"+basename+"】添加成员");
                    }
                });
                if (r[0]) return SUCCESS;
            }
            context.getSource().sendSuccess(() -> Component.literal("基地不存在，请检查基地名称"), false);
            return SUCCESS;
        }
        return FAILED;
    }
    public static int rm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer){
            String basename = context.getArgument("basename",String.class);
            if (!basename.isEmpty()){

                Player player = EntityArgument.getPlayer(context, "player");
                boolean[] r = {false};
                BasecoreServerHelper.getBaseCoreBlockEntities().forEach(be ->{
                    if (be.getEntityData().getName().equals(basename)) {
                        r[0] = true;
                        if (be.getOwner() == null) return;
                        if (be.getOwner().equals(serverPlayer.getUUID())) {
                            if (be.getEntityData().hasPermission(player.getUUID())) {
                                be.getEntityData().rmUser(player.getUUID());
                                sendCommandMsg(context, "已将对方移除");
                            } else sendCommandMsg(context, "对方非基地成员");
                        }else sendCommandMsg(context,"您无权限为基地【"+basename+"】添加成员");
                    }
                });
                if (r[0]) return SUCCESS;
            }
            context.getSource().sendSuccess(() -> Component.literal("基地不存在，请检查基地名称"), false);
            return SUCCESS;
        }
        return FAILED;
    }
    public static int rename(CommandContext<CommandSourceStack> context){
        if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            String oldName = context.getArgument("old_name", String.class);
            String newName = context.getArgument("new_name", String.class);
            if (!newName.isEmpty() && !newName.equals(oldName)) {
                BaseCoreBlockEntity baseCoreBlockEntity = null;
                for (BaseCoreBlockEntity blockEntity : BasecoreServerHelper.getBaseCoreBlockEntities()){
                    if (blockEntity.getEntityData().getName().equals(oldName)){
                        baseCoreBlockEntity = blockEntity;
                    }else if (blockEntity.getEntityData().getName().equals(newName)){
                        sendCommandMsg(context, "此名称已存在");
                        return SUCCESS;
                    }
                }
                if (baseCoreBlockEntity != null ){
                    if (baseCoreBlockEntity.getOwner() != null && baseCoreBlockEntity.getOwner().equals(serverPlayer.getUUID())) {
                        baseCoreBlockEntity.getEntityData().setName(newName);
                        baseCoreBlockEntity.updateToClient();
                        sendCommandMsg(context, "已更改基地名称");
                    }else sendCommandMsg(context, "无权限");
                }else sendCommandMsg(context, "符合名称的基地不存在");
            } else {
                sendCommandMsg(context, "新名称不符合规范");
            }
            return SUCCESS;
        }
        return FAILED;
    }
}
