package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.cast.InnateSpellCaster;
import net.silvertide.irons_spellbooks_pufferfish_compat.client.InnatePayloadHandler;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class CompatPayloads {
    public static final String PROTOCOL_VERSION = "1";

    private CompatPayloads() {}

    @SubscribeEvent
    static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(
                CastInnatePayload.TYPE,
                CastInnatePayload.STREAM_CODEC,
                CompatPayloads::onServerReceiveCastInnate);
        registrar.playToClient(
                InnatePoolPayload.TYPE,
                InnatePoolPayload.STREAM_CODEC,
                CompatPayloads::onClientReceiveInnatePool);
    }

    private static void onServerReceiveCastInnate(CastInnatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                InnateSpellCaster.castFromClientRequest(serverPlayer, payload.poolIndex());
            }
        });
    }

    private static void onClientReceiveInnatePool(InnatePoolPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        context.enqueueWork(() -> InnatePayloadHandler.onInnatePool(payload));
    }
}
