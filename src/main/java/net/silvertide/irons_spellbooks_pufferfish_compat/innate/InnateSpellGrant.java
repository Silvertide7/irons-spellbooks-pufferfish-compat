package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record InnateSpellGrant(ResourceLocation spell, int level) {
    public static final int DEFAULT_LEVEL = 1;

    public static final Codec<InnateSpellGrant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(InnateSpellGrant::spell),
            Codec.INT.optionalFieldOf("level", DEFAULT_LEVEL).forGetter(InnateSpellGrant::level)
    ).apply(instance, InnateSpellGrant::new));

    public static final StreamCodec<ByteBuf, InnateSpellGrant> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, InnateSpellGrant::spell,
            ByteBufCodecs.VAR_INT, InnateSpellGrant::level,
            InnateSpellGrant::new);
}
