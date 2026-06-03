package net.silvertide.irons_spellbooks_pufferfish_compat.skills;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.puffish.skillsmod.api.SkillsAPI;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnatePool;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrants;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.InnatePoolPayload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class InnateSyncBootstrap {
    private static final Map<UUID, List<InnateSpellGrant>> lastSentPoolByPlayer = new ConcurrentHashMap<>();

    private InnateSyncBootstrap() {}

    public static void registerSkillChangeListeners() {
        SkillsAPI.registerSkillUnlockEvent(InnateSyncBootstrap::onSkillChanged);
        SkillsAPI.registerSkillLockEvent(InnateSyncBootstrap::onSkillChanged);
    }

    private static void onSkillChanged(ServerPlayer player, ResourceLocation category, String skill) {
        if (InnateSpellGrants.INSTANCE.grantingSkills().contains(new SkillKey(category, skill))) {
            syncIfChanged(player);
        }
    }

    @SubscribeEvent
    static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(InnateSyncBootstrap::syncIfChanged);
    }

    @SubscribeEvent
    static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSentPoolByPlayer.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    static void onServerStopping(ServerStoppingEvent event) {
        lastSentPoolByPlayer.clear();
    }

    private static void syncIfChanged(ServerPlayer player) {
        List<InnateSpellGrant> currentPool = InnatePool.currentPool(player);
        List<InnateSpellGrant> lastSentPool = lastSentPoolByPlayer.get(player.getUUID());
        if (currentPool.equals(lastSentPool)) return;
        lastSentPoolByPlayer.put(player.getUUID(), currentPool);
        PacketDistributor.sendToPlayer(player, new InnatePoolPayload(currentPool));
    }
}
