package dev.anye.mc.basecore.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component for PartBundleItem, storing the total part count.
 */
public record PartBundleComponent(int count) {
    public static final Codec<PartBundleComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("count").forGetter(PartBundleComponent::count)
            ).apply(instance, PartBundleComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PartBundleComponent> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeInt(value.count),
            buf -> new PartBundleComponent(buf.readInt())
    );

    public static PartBundleComponent of(int count) {
        return new PartBundleComponent(Math.max(0, count));
    }
}
