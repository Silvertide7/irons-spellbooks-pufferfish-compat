package net.silvertide.irons_spellbooks_pufferfish_compat.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID, value = Dist.CLIENT)
public final class InnateKeybinds {
    public static final String KEYBIND_CATEGORY =
            "key.categories." + IronsSpellbooksPufferfishCompat.MODID;

    public static final KeyMapping OPEN_WHEEL = new KeyMapping(
            "key." + IronsSpellbooksPufferfishCompat.MODID + ".open_wheel",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            KEYBIND_CATEGORY);

    public static final KeyMapping TOGGLE_WHEEL = new KeyMapping(
            "key." + IronsSpellbooksPufferfishCompat.MODID + ".toggle_wheel",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KEYBIND_CATEGORY);

    public static final KeyMapping CAST_INNATE = new KeyMapping(
            "key." + IronsSpellbooksPufferfishCompat.MODID + ".cast_innate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            KEYBIND_CATEGORY);

    public static final KeyMapping SCROLL_CYCLE_MODIFIER = new KeyMapping(
            "key." + IronsSpellbooksPufferfishCompat.MODID + ".scroll_cycle_modifier",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            KEYBIND_CATEGORY);

    private InnateKeybinds() {}

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_WHEEL);
        event.register(TOGGLE_WHEEL);
        event.register(CAST_INNATE);
        event.register(SCROLL_CYCLE_MODIFIER);
    }
}
