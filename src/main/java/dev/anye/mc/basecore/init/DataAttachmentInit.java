package dev.anye.mc.basecore.init;

import com.mojang.serialization.Codec;
import dev.anye.mc.basecore.BaseCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataAttachmentInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BaseCore.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> PARTS = ATTACHMENT_TYPES.register("parts", () ->
            AttachmentType.<Integer>builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build()
    );

    public static void reg(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
