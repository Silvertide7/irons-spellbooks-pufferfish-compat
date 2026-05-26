package net.silvertide.irons_spellbooks_pufferfish_compat.client;

import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;

import java.util.List;
import java.util.Optional;

public final class ClientInnateState {
    private static volatile List<InnateSpellGrant> pool = List.of();
    private static volatile int selectedIndex = 0;

    private ClientInnateState() {}

    public static List<InnateSpellGrant> pool() {
        return pool;
    }

    public static int selectedIndex() {
        return selectedIndex;
    }

    public static Optional<InnateSpellGrant> selectedGrant() {
        List<InnateSpellGrant> snapshot = pool;
        int snapshotIndex = selectedIndex;
        if (snapshotIndex < 0 || snapshotIndex >= snapshot.size()) return Optional.empty();
        return Optional.of(snapshot.get(snapshotIndex));
    }

    public static void replacePool(List<InnateSpellGrant> next) {
        ResourceLocation previouslySelectedSpell = currentlySelectedSpellId();
        pool = List.copyOf(next);
        selectedIndex = indexOfSpell(previouslySelectedSpell).orElse(0);
    }

    private static ResourceLocation currentlySelectedSpellId() {
        return selectedGrant().map(InnateSpellGrant::spell).orElse(null);
    }

    private static Optional<Integer> indexOfSpell(ResourceLocation spellId) {
        if (spellId == null) return Optional.empty();
        for (int i = 0; i < pool.size(); i++) {
            if (pool.get(i).spell().equals(spellId)) return Optional.of(i);
        }
        return Optional.empty();
    }

    public static void cycleNext() {
        int size = pool.size();
        if (size <= 1) return;
        selectedIndex = (selectedIndex + 1) % size;
    }

    public static void cyclePrevious() {
        int size = pool.size();
        if (size <= 1) return;
        selectedIndex = (selectedIndex - 1 + size) % size;
    }

    public static void setSelectedIndex(int index) {
        int size = pool.size();
        if (size == 0) {
            selectedIndex = 0;
        } else if (index < 0) {
            selectedIndex = 0;
        } else if (index >= size) {
            selectedIndex = size - 1;
        } else {
            selectedIndex = index;
        }
    }
}
