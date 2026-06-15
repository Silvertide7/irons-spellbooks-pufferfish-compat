package net.silvertide.irons_spellbooks_pufferfish_compat.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.client.ClientInnateState;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.ClientConfig;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.InnateHudDisplay;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpells;

import java.util.Optional;

public final class InnateSelectedSpellOverlay implements LayeredDraw.Layer {
    public static final InnateSelectedSpellOverlay INSTANCE = new InnateSelectedSpellOverlay();
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IronsSpellbooksPufferfishCompat.MODID, "textures/gui/icons.png");

    public static final int CONTEXTUAL_FADE_WAIT_TICKS = 80;

    private static final int SPRITE_SIZE = 22;
    private static final int ICON_INNER_OFFSET = 3;
    private static final int ICON_SIZE = 16;
    private static final int SLOT_BORDER_U = 66;
    private static final int SLOT_BORDER_V = 84;
    private static final int SELECTED_OUTLINE_U = 0;
    private static final int SELECTED_OUTLINE_V = 84;

    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int OFFHAND_SLOT_WIDTH = 29;
    private static final int HOTBAR_PIXEL_HEIGHT = 22;
    private static final int GAP_FROM_OFFHAND_SLOT = 2;
    private static final float ALPHA_FADE_DENOMINATOR = 20f;

    private int contextualFadeoutTicksRemaining;
    private int lastTickSeen;
    private float alpha = 1f;

    private InnateSelectedSpellOverlay() {}

    public static void reveal() {
        if (ClientConfig.HUD_DISPLAY.get() == InnateHudDisplay.Contextual) {
            INSTANCE.contextualFadeoutTicksRemaining = CONTEXTUAL_FADE_WAIT_TICKS;
        }
    }

    public static void revealForTicks(int ticks) {
        if (ClientConfig.HUD_DISPLAY.get() == InnateHudDisplay.Contextual) {
            INSTANCE.contextualFadeoutTicksRemaining = ticks;
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.player.isSpectator()) return;

        InnateHudDisplay displayMode = ClientConfig.HUD_DISPLAY.get();
        if (displayMode == InnateHudDisplay.Never) return;

        if (displayMode == InnateHudDisplay.Contextual) {
            advanceFadeTimer(minecraft.player);
            if (contextualFadeoutTicksRemaining <= 0) return;
        } else {
            alpha = 1f;
        }

        Optional<InnateSpellGrant> selectedGrant = ClientInnateState.selectedGrant();
        if (selectedGrant.isEmpty()) return;

        Optional<AbstractSpell> resolvedSpell = InnateSpells.resolve(selectedGrant.get().spell());
        if (resolvedSpell.isEmpty()) return;
        AbstractSpell spell = resolvedSpell.get();

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int offhandSlotLeftEdge = screenWidth / 2 - HOTBAR_HALF_WIDTH - OFFHAND_SLOT_WIDTH;
        int defaultX = offhandSlotLeftEdge - SPRITE_SIZE - GAP_FROM_OFFHAND_SLOT;
        int defaultY = screenHeight - HOTBAR_PIXEL_HEIGHT;
        int x = defaultX + ClientConfig.SELECTED_X_OFFSET.get();
        int y = defaultY + ClientConfig.SELECTED_Y_OFFSET.get();

        prepTranslucency();
        graphics.blit(TEXTURE, x, y, SLOT_BORDER_U, SLOT_BORDER_V, SPRITE_SIZE, SPRITE_SIZE);
        graphics.blit(spell.getSpellIconResource(),
                x + ICON_INNER_OFFSET, y + ICON_INNER_OFFSET,
                0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.blit(TEXTURE, x, y, SELECTED_OUTLINE_U, SELECTED_OUTLINE_V, SPRITE_SIZE, SPRITE_SIZE);
        flushTranslucency();
    }

    private void advanceFadeTimer(Player player) {
        if (lastTickSeen != player.tickCount) {
            lastTickSeen = player.tickCount;
            if (contextualFadeoutTicksRemaining > 0) contextualFadeoutTicksRemaining--;
        }
        alpha = Mth.clamp(contextualFadeoutTicksRemaining / ALPHA_FADE_DENOMINATOR, 0f, 1f);
    }

    private void prepTranslucency() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
    }

    private void flushTranslucency() {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
