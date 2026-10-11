package net.silvertide.irons_spellbooks_pufferfish_compat.skills;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.puffish.skillsmod.api.SkillsAPI;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnatePool;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrants;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.CompatPayloads;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.InnatePoolPayload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class InnateSyncBootstrap {
    private static final Map<UUID, List<InnateSpellGrant>> lastSentPoolByPlayer = new ConcurrentHashMap<>();

    private InnateSyncBootstrap() {}

    public static void registerSkillChangeListeners(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SkillsAPI.registerSkillUnlockEvent(InnateSyncBootstrap::onSkillChanged);
            SkillsAPI.registerSkillLockEvent(InnateSyncBootstrap::onSkillChanged);
        });
    }

    private static void onSkillChanged(ServerPlayer player, ResourceLocation category, String skill) {
        if (InnateSpellGrants.INSTANCE.grantingSkills().contains(new SkillKey(category, skill))) {
            syncIfChanged(player);
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        List<ServerPlayer> relevantPlayers = event.getPlayer() != null
                ? List.of(event.getPlayer())
                : event.getPlayerList().getPlayers();
        relevantPlayers.forEach(InnateSyncBootstrap::syncIfChanged);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncIfChanged(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSentPoolByPlayer.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        lastSentPoolByPlayer.clear();
    }

    private static void syncIfChanged(ServerPlayer player) {
        List<InnateSpellGrant> currentPool = InnatePool.currentPool(player);
        List<InnateSpellGrant> lastSentPool = lastSentPoolByPlayer.get(player.getUUID());
        if (currentPool.equals(lastSentPool)) return;
        lastSentPoolByPlayer.put(player.getUUID(), currentPool);
        CompatPayloads.sendToPlayer(player, new InnatePoolPayload(currentPool));
    }
}
