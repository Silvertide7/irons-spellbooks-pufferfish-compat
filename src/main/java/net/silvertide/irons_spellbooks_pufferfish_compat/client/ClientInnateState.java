package net.silvertide.irons_spellbooks_pufferfish_compat.client;

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
        pool = List.copyOf(next);
        if (pool.isEmpty()) {
            selectedIndex = 0;
        } else if (selectedIndex >= pool.size()) {
            selectedIndex = pool.size() - 1;
        }
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
