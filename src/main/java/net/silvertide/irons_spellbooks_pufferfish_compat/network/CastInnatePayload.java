package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

public record CastInnatePayload(ResourceLocation spellId) implements CustomPacketPayload {
    public static final Type<CastInnatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(IronsSpellbooksPufferfishCompat.MODID, "cast_innate"));

    public static final StreamCodec<ByteBuf, CastInnatePayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CastInnatePayload::spellId,
            CastInnatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
