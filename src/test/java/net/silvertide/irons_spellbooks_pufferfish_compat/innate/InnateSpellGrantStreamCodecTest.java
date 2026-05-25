package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnateSpellGrantStreamCodecTest {
    @Test
    void singleGrantRoundTrips() {
        InnateSpellGrant original = new InnateSpellGrant(
                ResourceLocation.parse("irons_spellbooks:fireball"), 4);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        InnateSpellGrant.STREAM_CODEC.encode(buf, original);
        InnateSpellGrant decoded = InnateSpellGrant.STREAM_CODEC.decode(buf);

        assertEquals(original, decoded);
    }

    @Test
    void emptyListRoundTrips() {
        StreamCodec<io.netty.buffer.ByteBuf, List<InnateSpellGrant>> listCodec =
                InnateSpellGrant.STREAM_CODEC.apply(ByteBufCodecs.list());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        listCodec.encode(buf, List.of());
        List<InnateSpellGrant> decoded = listCodec.decode(buf);

        assertTrue(decoded.isEmpty());
    }

    @Test
    void listOfManyGrantsRoundTrips() {
        StreamCodec<io.netty.buffer.ByteBuf, List<InnateSpellGrant>> listCodec =
                InnateSpellGrant.STREAM_CODEC.apply(ByteBufCodecs.list());
        List<InnateSpellGrant> original = List.of(
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:fireball"), 1),
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:iceball"), 2),
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:lightning_bolt"), 3));

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        listCodec.encode(buf, original);
        List<InnateSpellGrant> decoded = listCodec.decode(buf);

        assertEquals(original, decoded);
    }
}
