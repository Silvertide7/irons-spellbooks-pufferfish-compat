package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnatePoolPayloadTest {
    @Test
    void singleGrantRoundTrips() {
        InnateSpellGrant original = new InnateSpellGrant(
                ResourceLocation.parse("irons_spellbooks:fireball"), 4);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        original.encode(buf);

        assertEquals(original, InnateSpellGrant.decode(buf));
    }

    @Test
    void emptyPoolRoundTrips() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        new InnatePoolPayload(List.of()).encode(buf);

        assertTrue(InnatePoolPayload.decode(buf).pool().isEmpty());
    }

    @Test
    void poolOfManyGrantsRoundTrips() {
        InnatePoolPayload original = new InnatePoolPayload(List.of(
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:fireball"), 1),
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:iceball"), 2),
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:lightning_bolt"), 3)));

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        original.encode(buf);

        assertEquals(original, InnatePoolPayload.decode(buf));
    }

    @Test
    void castRequestRoundTrips() {
        CastInnatePayload original = new CastInnatePayload(ResourceLocation.parse("irons_spellbooks:fireball"));

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        original.encode(buf);

        assertEquals(original, CastInnatePayload.decode(buf));
    }
}
