# Credits

## Third-party assets and code

This mod includes assets and ports rendering code from Iron's Spells 'n Spellbooks by **Iron431**.

- Source: https://github.com/iron431/Irons-Spells-n-Spellbooks
- Code license: MIT
- Asset license: CC-BY-4.0

### Assets

- `assets/irons_spellbooks_pufferfish_compat/textures/gui/icons.png` — copied verbatim from Iron's Spells 'n Spellbooks (`assets/irons_spellbooks/textures/gui/icons.png`). Used under CC-BY-4.0 with attribution to Iron431.

### Ported code

The following classes are visual/behavioral ports of Iron's Spellbooks source, adapted to our innate-spell data layer. Under the MIT license terms, with attribution:

- `client/hud/InnateSpellBarOverlay.java` ← `io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay`
- `client/hud/InnateSpellWheelOverlay.java` ← `io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay`
- `client/hud/InnateBarLayout.java` ← `io.redspace.ironsspellbooks.player.ClientRenderCache` (layout algorithm)

Where original behavior could not be reproduced via the public API (e.g. live cooldown progress, which reads from `ClientMagicData` outside the `:api` classifier), the feature is omitted with a TODO. The visual presentation otherwise matches the upstream overlays.
