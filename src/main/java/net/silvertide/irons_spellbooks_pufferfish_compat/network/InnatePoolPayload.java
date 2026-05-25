package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;

import java.util.List;

public record InnatePoolPayload(List<InnateSpellGrant> pool) implements CustomPacketPayload {
    public static final Type<InnatePoolPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(IronsSpellbooksPufferfishCompat.MODID, "innate_pool"));

    public static final StreamCodec<ByteBuf, InnatePoolPayload> STREAM_CODEC = StreamCodec.composite(
            InnateSpellGrant.STREAM_CODEC.apply(ByteBufCodecs.list()),
            InnatePoolPayload::pool,
            InnatePoolPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
