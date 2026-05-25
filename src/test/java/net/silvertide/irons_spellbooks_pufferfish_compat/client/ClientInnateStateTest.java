package net.silvertide.irons_spellbooks_pufferfish_compat.client;

import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientInnateStateTest {
    private static final InnateSpellGrant FIREBALL = new InnateSpellGrant(
            ResourceLocation.parse("irons_spellbooks:fireball"), 1);
    private static final InnateSpellGrant ICEBALL = new InnateSpellGrant(
            ResourceLocation.parse("irons_spellbooks:iceball"), 1);
    private static final InnateSpellGrant LIGHTNING = new InnateSpellGrant(
            ResourceLocation.parse("irons_spellbooks:lightning_bolt"), 1);

    @BeforeEach
    void resetClientState() {
        ClientInnateState.replacePool(List.of());
        ClientInnateState.setSelectedIndex(0);
    }

    @Test
    void cycleNextWrapsAroundToZero() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL, LIGHTNING));
        ClientInnateState.setSelectedIndex(2);
        ClientInnateState.cycleNext();
        assertEquals(0, ClientInnateState.selectedIndex());
    }

    @Test
    void cyclePreviousWrapsAroundFromZeroToEnd() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL, LIGHTNING));
        ClientInnateState.setSelectedIndex(0);
        ClientInnateState.cyclePrevious();
        assertEquals(2, ClientInnateState.selectedIndex());
    }

    @Test
    void cycleIsNoopWithSingleSpell() {
        ClientInnateState.replacePool(List.of(FIREBALL));
        ClientInnateState.cycleNext();
        assertEquals(0, ClientInnateState.selectedIndex());
        ClientInnateState.cyclePrevious();
        assertEquals(0, ClientInnateState.selectedIndex());
    }

    @Test
    void replacePoolClampsSelectedIndexWhenPoolShrinks() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL, LIGHTNING));
        ClientInnateState.setSelectedIndex(2);
        ClientInnateState.replacePool(List.of(FIREBALL));
        assertEquals(0, ClientInnateState.selectedIndex());
    }

    @Test
    void replacePoolPreservesSelectedIndexWhenInBounds() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL, LIGHTNING));
        ClientInnateState.setSelectedIndex(1);
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL, LIGHTNING));
        assertEquals(1, ClientInnateState.selectedIndex());
    }

    @Test
    void replacePoolResetsToZeroWhenPoolBecomesEmpty() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL));
        ClientInnateState.setSelectedIndex(1);
        ClientInnateState.replacePool(List.of());
        assertEquals(0, ClientInnateState.selectedIndex());
    }

    @Test
    void setSelectedIndexClampsBelowZero() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL));
        ClientInnateState.setSelectedIndex(-5);
        assertEquals(0, ClientInnateState.selectedIndex());
    }

    @Test
    void setSelectedIndexClampsAboveSize() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL));
        ClientInnateState.setSelectedIndex(99);
        assertEquals(1, ClientInnateState.selectedIndex());
    }

    @Test
    void selectedGrantReturnsCurrentSelection() {
        ClientInnateState.replacePool(List.of(FIREBALL, ICEBALL));
        ClientInnateState.setSelectedIndex(1);
        Optional<InnateSpellGrant> selected = ClientInnateState.selectedGrant();
        assertTrue(selected.isPresent());
        assertEquals(ICEBALL, selected.get());
    }

    @Test
    void selectedGrantIsEmptyWhenPoolIsEmpty() {
        ClientInnateState.replacePool(List.of());
        assertFalse(ClientInnateState.selectedGrant().isPresent());
    }
}
