package net.silvertide.irons_spellbooks_pufferfish_compat.client;

import net.silvertide.irons_spellbooks_pufferfish_compat.client.hud.InnateSelectedSpellOverlay;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.InnatePoolPayload;

public final class InnatePayloadHandler {
    private InnatePayloadHandler() {}

    public static void onInnatePool(InnatePoolPayload payload) {
        boolean poolChanged = !ClientInnateState.pool().equals(payload.pool());
        ClientInnateState.replacePool(payload.pool());
        if (poolChanged) {
            InnateSelectedSpellOverlay.reveal();
        }
    }
}
