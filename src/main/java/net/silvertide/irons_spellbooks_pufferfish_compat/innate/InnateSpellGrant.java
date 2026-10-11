package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record InnateSpellGrant(ResourceLocation spell, int level) {
    public static final int DEFAULT_LEVEL = 1;
    public static final int MIN_LEVEL = 1;

    public static final Codec<InnateSpellGrant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(InnateSpellGrant::spell),
            Codec.intRange(MIN_LEVEL, Integer.MAX_VALUE)
                    .optionalFieldOf("level", DEFAULT_LEVEL).forGetter(InnateSpellGrant::level)
    ).apply(instance, InnateSpellGrant::new));

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(spell);
        buffer.writeVarInt(level);
    }

    public static InnateSpellGrant decode(FriendlyByteBuf buffer) {
        return new InnateSpellGrant(buffer.readResourceLocation(), buffer.readVarInt());
    }
}
