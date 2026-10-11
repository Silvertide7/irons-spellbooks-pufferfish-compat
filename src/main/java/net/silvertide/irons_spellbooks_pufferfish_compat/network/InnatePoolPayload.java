package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;

import java.util.List;

public record InnatePoolPayload(List<InnateSpellGrant> pool) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeCollection(pool, (out, grant) -> grant.encode(out));
    }

    public static InnatePoolPayload decode(FriendlyByteBuf buffer) {
        return new InnatePoolPayload(buffer.readList(InnateSpellGrant::decode));
    }
}
