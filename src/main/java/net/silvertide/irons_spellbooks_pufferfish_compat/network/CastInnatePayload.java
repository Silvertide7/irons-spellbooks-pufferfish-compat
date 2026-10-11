package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record CastInnatePayload(ResourceLocation spellId) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(spellId);
    }

    public static CastInnatePayload decode(FriendlyByteBuf buffer) {
        return new CastInnatePayload(buffer.readResourceLocation());
    }
}
