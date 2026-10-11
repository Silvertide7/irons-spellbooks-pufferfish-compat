package net.silvertide.irons_spellbooks_pufferfish_compat.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.cast.InnateSpellCaster;
import net.silvertide.irons_spellbooks_pufferfish_compat.client.InnatePayloadHandler;

import java.util.function.Supplier;

public final class CompatPayloads {
    public static final String PROTOCOL_VERSION = "2";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(IronsSpellbooksPufferfishCompat.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private CompatPayloads() {}

    public static void register() {
        CHANNEL.messageBuilder(CastInnatePayload.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastInnatePayload::encode)
                .decoder(CastInnatePayload::decode)
                .consumerMainThread(CompatPayloads::onServerReceiveCastInnate)
                .add();
        CHANNEL.messageBuilder(InnatePoolPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(InnatePoolPayload::encode)
                .decoder(InnatePoolPayload::decode)
                .consumerMainThread((payload, context) -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT, () -> () -> InnatePayloadHandler.onInnatePool(payload)))
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, InnatePoolPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToServer(CastInnatePayload payload) {
        CHANNEL.sendToServer(payload);
    }

    private static void onServerReceiveCastInnate(CastInnatePayload payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) {
            InnateSpellCaster.castFromClientRequest(sender, payload.spellId());
        }
    }
}
