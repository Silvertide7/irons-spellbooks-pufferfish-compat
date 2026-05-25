package net.silvertide.irons_spellbooks_pufferfish_compat.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.client.ClientInnateState;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.ClientConfig;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpells;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

public final class InnateSpellWheelOverlay implements LayeredDraw.Layer {
    public static final InnateSpellWheelOverlay INSTANCE = new InnateSpellWheelOverlay();
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IronsSpellbooksPufferfishCompat.MODID, "textures/gui/icons.png");

    private static final Vector4f LINE_COLOR = new Vector4f(1f, .85f, .7f, 1f);
    private static final Vector4f RADIAL_BUTTON_COLOR = new Vector4f(.04f, .03f, .01f, .6f);
    private static final Vector4f HIGHLIGHT_COLOR = new Vector4f(.8f, .7f, .55f, .7f);
    private static final float RING_INNER_EDGE = 20f;
    private static final float RING_OUTER_EDGE = 80f;
    private static final float RING_DEAD_ZONE_RADIUS = 65f;
    private static final float CATEGORY_LINE_EXTENSION = 2f;

    private static final int ICON_SIZE = 16;
    private static final int ICON_HALF = ICON_SIZE / 2;
    private static final int BORDER_SIZE = 32;
    private static final int BORDER_HALF = BORDER_SIZE / 2;
    private static final int UNSELECTED_BORDER_U = 0;
    private static final int SELECTED_BORDER_U = 32;
    private static final int BORDER_V = 106;

    private static final int LABEL_TEXT_MARGIN = 5;
    private static final int LABEL_TITLE_MARGIN = 5;
    private static final int LABEL_TEXT_COLOR = 0xFFFFFF;
    private static final int LABEL_INNATE_TAG_COLOR = 0xFFAAAAFF;
    private static final int LABEL_UNIQUE_INFO_COLOR = 0x3BE33B;

    private boolean active;
    private int wheelSelection;

    private InnateSpellWheelOverlay() {}

    public boolean isActive() {
        return active;
    }

    public void open() {
        if (ClientInnateState.pool().isEmpty()) return;
        active = true;
        wheelSelection = -1;
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    public void close() {
        active = false;
        if (wheelSelection >= 0) {
            ClientInnateState.setSelectedIndex(wheelSelection);
            InnateSelectedSpellOverlay.reveal();
        }
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.player.isSpectator() || !active) {
            return;
        }
        if (minecraft.screen != null || minecraft.mouseHandler.isMouseGrabbed()) {
            close();
            return;
        }

        List<InnateSpellGrant> pool = ClientInnateState.pool();
        int totalSpells = pool.size();
        if (totalSpells <= 0) {
            close();
            return;
        }

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        float guiScale = 1f;
        if (ClientConfig.WHEEL_CONSISTENT_SIZE.get()) {
            float invertedGuiScaleFactor = (float) (1.0 / minecraft.getWindow().getGuiScale());
            float physicalScaleFactor = Math.min(
                    minecraft.getWindow().getScreenWidth() / 1920f,
                    minecraft.getWindow().getScreenHeight() / 1080f);
            guiScale = invertedGuiScaleFactor * physicalScaleFactor * 3f
                    * ClientConfig.WHEEL_SCALE.get().floatValue();
        }
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(guiScale, guiScale, 1);
        centerX = 0;
        centerY = 0;

        Vec2 screenCenterPhysical = new Vec2(
                minecraft.getWindow().getScreenWidth() * .5f,
                minecraft.getWindow().getScreenHeight() * .5f);
        Vec2 mousePos = new Vec2(
                (float) minecraft.mouseHandler.xpos(),
                (float) minecraft.mouseHandler.ypos());
        double radiansPerSpell = Math.toRadians(360f / (float) totalSpells);
        float mouseRotation = (Utils.getAngle(mousePos, screenCenterPhysical)
                + Mth.HALF_PI + (float) radiansPerSpell * .5f) % Mth.TWO_PI;

        wheelSelection = (int) Mth.clamp(mouseRotation / radiansPerSpell, 0, totalSpells - 1);
        if (mousePos.distanceToSqr(screenCenterPhysical) < RING_DEAD_ZONE_RADIUS * RING_DEAD_ZONE_RADIUS) {
            wheelSelection = Mth.clamp(ClientInnateState.selectedIndex(), 0, totalSpells - 1);
        }

        graphics.fill(0, 0, screenWidth, screenHeight, 0);

        drawRadialBackgrounds(graphics, centerX, centerY, wheelSelection, totalSpells);
        drawDividingLines(graphics, centerX, centerY, totalSpells);

        renderInfoText(graphics, minecraft.font, minecraft.player, pool.get(wheelSelection), centerX, centerY);
        renderSpellIcons(graphics, pool, centerX, centerY, totalSpells, radiansPerSpell);

        poseStack.popPose();
    }

    private void renderSpellIcons(GuiGraphics graphics, List<InnateSpellGrant> pool,
                                  int centerX, int centerY, int totalSpells, double radiansPerSpell) {
        float iconScale = Mth.lerp(totalSpells / 15f, 2f, 1.25f) * .65f;
        double radius = 3f / iconScale * (RING_INNER_EDGE + RING_INNER_EDGE) * .5
                * (.85f + .25f * (totalSpells / 15f));

        Vec2[] iconPositions = new Vec2[totalSpells];
        for (int i = 0; i < iconPositions.length; i++) {
            iconPositions[i] = new Vec2(
                    (float) (Math.sin(radiansPerSpell * i) * radius),
                    (float) (-Math.cos(radiansPerSpell * i) * radius));
        }

        int selectedIndex = ClientInnateState.selectedIndex();
        PoseStack poseStack = graphics.pose();
        for (int i = 0; i < iconPositions.length; i++) {
            InnateSpellGrant grant = pool.get(i);
            Optional<AbstractSpell> resolved = InnateSpells.resolve(grant.spell());
            if (resolved.isEmpty()) continue;
            AbstractSpell spell = resolved.get();

            poseStack.pushPose();
            poseStack.translate(centerX, centerY, 0);
            poseStack.scale(iconScale, iconScale, iconScale);

            graphics.blit(spell.getSpellIconResource(),
                    (int) iconPositions[i].x - ICON_HALF,
                    (int) iconPositions[i].y - ICON_HALF,
                    0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            graphics.blit(TEXTURE,
                    (int) iconPositions[i].x - BORDER_HALF,
                    (int) iconPositions[i].y - BORDER_HALF,
                    selectedIndex == i ? SELECTED_BORDER_U : UNSELECTED_BORDER_U,
                    BORDER_V, BORDER_SIZE, BORDER_SIZE);

            poseStack.popPose();
        }
    }

    private void renderInfoText(GuiGraphics graphics, Font font, Player player,
                                InnateSpellGrant grant, int centerX, int centerY) {
        Optional<AbstractSpell> resolved = InnateSpells.resolve(grant.spell());
        if (resolved.isEmpty()) return;
        AbstractSpell spell = resolved.get();

        int effectiveLevel = spell.getLevelFor(grant.level(), player);
        List<MutableComponent> uniqueInfo = spell.getUniqueInfo(effectiveLevel, player);
        int infoLineCount = Math.max(3, uniqueInfo.size());
        int textBlockHeight = infoLineCount * font.lineHeight + 5;

        MutableComponent title = spell.getDisplayName(player)
                .copy()
                .withStyle(Style.EMPTY.withUnderlined(true));
        Component innateTag = Component.translatable("hud." + IronsSpellbooksPufferfishCompat.MODID + ".innate_tag");
        Component levelLine = Component.translatable("hud." + IronsSpellbooksPufferfishCompat.MODID + ".wheel_level", effectiveLevel);
        Component manaLine = Component.translatable("hud." + IronsSpellbooksPufferfishCompat.MODID + ".wheel_mana", spell.getManaCost(effectiveLevel))
                .copy().withStyle(ChatFormatting.AQUA);

        int titleY = centerY - (int) (RING_OUTER_EDGE + textBlockHeight);
        graphics.drawString(font, title, centerX - font.width(title) / 2, titleY, LABEL_TEXT_COLOR, true);
        graphics.drawString(font, innateTag,
                centerX - font.width(innateTag) / 2,
                titleY - font.lineHeight - 1,
                LABEL_INNATE_TAG_COLOR, true);

        int infoStartY = titleY + font.lineHeight + LABEL_TITLE_MARGIN;
        graphics.drawString(font, levelLine,
                centerX - font.width(levelLine) - LABEL_TEXT_MARGIN,
                infoStartY, LABEL_TEXT_COLOR, true);
        if (spell.getManaCost(effectiveLevel) > 0) {
            infoStartY += font.lineHeight;
            graphics.drawString(font, manaLine,
                    centerX - font.width(manaLine) - LABEL_TEXT_MARGIN,
                    infoStartY, LABEL_TEXT_COLOR, true);
        }

        for (int i = 0; i < uniqueInfo.size(); i++) {
            int lineY = (int) (centerY - (RING_OUTER_EDGE + textBlockHeight)
                    + font.lineHeight * (i + 1) + LABEL_TITLE_MARGIN);
            graphics.drawString(font, uniqueInfo.get(i),
                    centerX + LABEL_TEXT_MARGIN, lineY,
                    LABEL_UNIQUE_INFO_COLOR, true);
        }
    }

    private void drawRadialBackgrounds(GuiGraphics graphics, float centerX, float centerY,
                                       int highlightedSpellIndex, int totalSpells) {
        float quarterCircle = Mth.HALF_PI;
        int segments = totalSpells < 6
                ? (totalSpells % 2 == 1 ? 15 : 12)
                : totalSpells * 2;
        float radiansPerSegment = 2f * Mth.PI / segments;
        float radiansPerSpell = 2f * Mth.PI / totalSpells;
        VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f pose = graphics.pose().last().pose();

        for (int i = 0; i < segments; i++) {
            float beginRadians = i * radiansPerSegment - (quarterCircle + (radiansPerSpell / 2f));
            float endRadians = (i + 1) * radiansPerSegment - (quarterCircle + (radiansPerSpell / 2f));

            float x1Inner = Mth.cos(beginRadians) * RING_INNER_EDGE;
            float x2Inner = Mth.cos(endRadians) * RING_INNER_EDGE;
            float y1Inner = Mth.sin(beginRadians) * RING_INNER_EDGE;
            float y2Inner = Mth.sin(endRadians) * RING_INNER_EDGE;
            float x1Outer = Mth.cos(beginRadians) * RING_OUTER_EDGE;
            float x2Outer = Mth.cos(endRadians) * RING_OUTER_EDGE;
            float y1Outer = Mth.sin(beginRadians) * RING_OUTER_EDGE;
            float y2Outer = Mth.sin(endRadians) * RING_OUTER_EDGE;

            boolean isHighlighted = (i * totalSpells) / segments == highlightedSpellIndex;
            Vector4f buttonColor = isHighlighted ? HIGHLIGHT_COLOR : RADIAL_BUTTON_COLOR;

            vertexConsumer.addVertex(pose, centerX + x1Inner, centerY + y1Inner, 0)
                    .setColor(buttonColor.x(), buttonColor.y(), buttonColor.z(), buttonColor.w());
            vertexConsumer.addVertex(pose, centerX + x2Inner, centerY + y2Inner, 0)
                    .setColor(buttonColor.x(), buttonColor.y(), buttonColor.z(), buttonColor.w());
            vertexConsumer.addVertex(pose, centerX + x2Outer, centerY + y2Outer, 0)
                    .setColor(buttonColor.x(), buttonColor.y(), buttonColor.z(), 0);
            vertexConsumer.addVertex(pose, centerX + x1Outer, centerY + y1Outer, 0)
                    .setColor(buttonColor.x(), buttonColor.y(), buttonColor.z(), 0);

            float categoryLineEdge = RING_INNER_EDGE + CATEGORY_LINE_EXTENSION;
            float x1CategoryOuter = Mth.cos(beginRadians) * categoryLineEdge;
            float x2CategoryOuter = Mth.cos(endRadians) * categoryLineEdge;
            float y1CategoryOuter = Mth.sin(beginRadians) * categoryLineEdge;
            float y2CategoryOuter = Mth.sin(endRadians) * categoryLineEdge;

            vertexConsumer.addVertex(pose, centerX + x1Inner, centerY + y1Inner, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), LINE_COLOR.w());
            vertexConsumer.addVertex(pose, centerX + x2Inner, centerY + y2Inner, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), LINE_COLOR.w());
            vertexConsumer.addVertex(pose, centerX + x2CategoryOuter, centerY + y2CategoryOuter, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), LINE_COLOR.w());
            vertexConsumer.addVertex(pose, centerX + x1CategoryOuter, centerY + y1CategoryOuter, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), LINE_COLOR.w());
        }
    }

    private void drawDividingLines(GuiGraphics graphics, float centerX, float centerY, int totalSpells) {
        if (totalSpells <= 1) return;

        float quarterCircle = Mth.HALF_PI;
        float radiansPerSpell = 2f * Mth.PI / totalSpells;
        VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f pose = graphics.pose().last().pose();

        for (int i = 0; i < totalSpells; i++) {
            float closeWidth = 8f * Mth.DEG_TO_RAD;
            float farWidth = closeWidth / 4f;
            float beginCloseRadians = i * radiansPerSpell - (quarterCircle + (radiansPerSpell / 2f)) - (closeWidth / 4f);
            float endCloseRadians = beginCloseRadians + closeWidth;
            float beginFarRadians = i * radiansPerSpell - (quarterCircle + (radiansPerSpell / 2f)) - (farWidth / 4f);
            float endFarRadians = beginCloseRadians + farWidth;

            float x1Inner = Mth.cos(beginCloseRadians) * RING_INNER_EDGE;
            float x2Inner = Mth.cos(endCloseRadians) * RING_INNER_EDGE;
            float y1Inner = Mth.sin(beginCloseRadians) * RING_INNER_EDGE;
            float y2Inner = Mth.sin(endCloseRadians) * RING_INNER_EDGE;
            float x1Outer = Mth.cos(beginFarRadians) * RING_OUTER_EDGE * 1.4f;
            float x2Outer = Mth.cos(endFarRadians) * RING_OUTER_EDGE * 1.4f;
            float y1Outer = Mth.sin(beginFarRadians) * RING_OUTER_EDGE * 1.4f;
            float y2Outer = Mth.sin(endFarRadians) * RING_OUTER_EDGE * 1.4f;

            vertexConsumer.addVertex(pose, centerX + x1Inner, centerY + y1Inner, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), LINE_COLOR.w());
            vertexConsumer.addVertex(pose, centerX + x2Inner, centerY + y2Inner, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), LINE_COLOR.w());
            vertexConsumer.addVertex(pose, centerX + x2Outer, centerY + y2Outer, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), 0);
            vertexConsumer.addVertex(pose, centerX + x1Outer, centerY + y1Outer, 0)
                    .setColor(LINE_COLOR.x(), LINE_COLOR.y(), LINE_COLOR.z(), 0);
        }
    }
}
