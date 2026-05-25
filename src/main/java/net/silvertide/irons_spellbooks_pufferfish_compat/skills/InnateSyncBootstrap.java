package net.silvertide.irons_spellbooks_pufferfish_compat.skills;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnatePool;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.InnatePoolPayload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class InnateSyncBootstrap {
    public static final int RESYNC_INTERVAL_TICKS = 20;

    private static final Map<UUID, List<InnateSpellGrant>> lastSentPoolByPlayer = new ConcurrentHashMap<>();
    private static int sinceLastTickCheck = 0;

    private InnateSyncBootstrap() {}

    @SubscribeEvent
    static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncIfChanged(player);
    }

    @SubscribeEvent
    static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSentPoolByPlayer.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    static void onServerStopping(ServerStoppingEvent event) {
        lastSentPoolByPlayer.clear();
        sinceLastTickCheck = 0;
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (++sinceLastTickCheck < RESYNC_INTERVAL_TICKS) return;
        sinceLastTickCheck = 0;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            syncIfChanged(player);
        }
    }

    private static void syncIfChanged(ServerPlayer player) {
        List<InnateSpellGrant> currentPool = InnatePool.currentPool(player);
        List<InnateSpellGrant> lastSentPool = lastSentPoolByPlayer.get(player.getUUID());
        if (currentPool.equals(lastSentPool)) return;
        lastSentPoolByPlayer.put(player.getUUID(), currentPool);
        PacketDistributor.sendToPlayer(player, new InnatePoolPayload(currentPool));
    }
}
