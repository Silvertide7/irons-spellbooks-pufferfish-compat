# Iron's Spellbooks ↔ Pufferfish's Skills compat

NeoForge 1.21.1 mod that bridges Iron's Spells 'n Spellbooks and Pufferfish's Skills.

## Features

### 1. Skill-gated casting + inscribing (`spell_skill_requirements/` and `skill_blocks/`)

Block a spell cast OR inscription when the player has not unlocked one or more configured Pufferfish skills. The gate fires for every cast source (SPELLBOOK, SCROLL, SWORD, COMMAND, …) since all funnel through `AbstractSpell.attemptInitiateCast`, and separately for `InscribeSpellEvent` at the inscription table.

**Two authoring shapes feed the same gate** — both contribute to the set of skills the player must have unlocked:

- **Per-spell** (one spell → one required skill): drop `data/<pack>/spell_skill_requirements/<arbitrary>.json` with `{"spell": "<ns>:<id>", "category": "<cat>", "skill": "<skill_id>"}`. The file path is organizational; the spell ID lives inside the JSON.
- **Per-skill** (one skill → many blocked spells): drop `data/<pack>/skill_blocks/<arbitrary>.json` with `{"category": "<cat>", "skill": "<skill_id>", "blocks": ["spell_a", "spell_b", ...]}`.

If a spell appears in either store, the player needs every listed skill (AND semantics) to cast or inscribe it. No mapping → no gate (opt-in).

### 2. Innate spells (`innate_spells/`)

Unlocking a configured Pufferfish skill grants the player innate access to a spell — castable from a keybind with no spellbook, sword, or scroll required. Casts still consume mana and respect cooldown (`CastSource.SPELLBOOK`), and they still flow through `SpellPreCastEvent`, so the gate from feature 1 composes naturally.

**Upgrading via chain-of-nodes.** Pufferfish nodes are binary (taken / not taken) — there is no per-node "rank up" concept. To let a player level up a spell, add multiple skill nodes each granting the same `spell` at progressively higher `level`, chained via `connections.json`:

```
[innate_firebolt   level 1]  →  [innate_firebolt_2  level 2]  →  [innate_firebolt_3  level 3]
```

`InnatePool.compose` deduplicates by spell ID and keeps the **highest** level grant. So a player who has unlocked all three skills sees only one Firebolt entry in their pool — at level 3. The lower-level definitions are still useful: they fence the chain (you have to take level 1 to be able to take level 2) and they specify the unlock cost per rank.

Default keybinds (rebindable, chosen to avoid Iron's Spellbooks defaults):
- **Hold `C`** — opens the innate spell wheel (radial menu); release to commit the highlighted spell as the new selection.
- **`X`** — casts the currently selected spell.
- **Hold `Z` + scroll** — cycle through innate spells.
- Toggle-wheel keybind is registered but unbound by default.

A small badge to the left of the hotbar shows only the *currently selected* innate spell (slot frame + spell icon + translucent purple background so it reads as "innate" vs Iron's Spellbooks' own bar). Position is set by `selectedXOffset` / `selectedYOffset` in `config/irons_spellbooks_pufferfish_compat-client.toml`; display mode is `Always` / `Contextual` (fades in on selection-change/cast) / `Never`. The wheel keeps the multi-spell radial layout — only the always-on indicator changed.

### Planned (not implemented yet)

- Skill node reward that grants `+N` to a skill when a spell is cast.

## How feature 1 (the gate) works

1. `SpellPreCastEvent` (cancellable, all cast sources) and `InscribeSpellEvent` (cancellable, inscription table) fire server-side.
2. Both gates call `SpellCastGate.collectRequiredSkills(spellId)` which unions:
   - the `SkillKey` from `SpellSkillRequirements.findForSpell(spellId)` (if any), and
   - the `Set<SkillKey>` from `SpellSkillBlocks.blockersFor(spellId)` (skills that declared they block this spell).
3. For each `SkillKey` in the set, `PuffishSkillsLookup.hasUnlocked(player, skillKey)` asks Pufferfish.
4. First unmet skill → `event.setCanceled(true)` + action-bar message. All satisfied → cast/inscribe proceeds.

## How feature 2 (innate spells) works

Server side:

1. `InnateSpellGrants` (datapack reload listener) holds a `Map<SkillKey, InnateSpellGrant>` parsed from `innate_spells/*.json`.
2. `InnateSyncBootstrap` recomputes each player's pool on login and once per second (`ServerTickEvent.Post`), pushing `InnatePoolPayload` only when the pool's hash changes.
3. The client press of the cast keybind sends a `CastInnatePayload(poolIndex)`. The server re-derives the pool (authoritative), looks up the grant, and calls `AbstractSpell.attemptInitiateCast(ItemStack.EMPTY, level, world, player, CastSource.SPELLBOOK, true, "innate")`.

Client side:

1. `InnatePayloadHandler` updates `ClientInnateState` on every pool sync.
2. `InnateKeybinds` registers four `KeyMapping`s (`OPEN_WHEEL` hold, `TOGGLE_WHEEL`, `CAST_INNATE`, `SCROLL_CYCLE_MODIFIER` hold).
3. `InnateInputHandler` (`ClientTickEvent.Post` + `InputEvent.MouseScrollingEvent`) handles all of it: drains cast clicks → sends packet, hold-to-open + release-to-commit on the wheel key, scroll-while-modifier cycles selection, toggle key flips the wheel.
4. `InnateHudLayers` (`RegisterGuiLayersEvent`) registers two overlays above all vanilla layers:
   - `InnateSelectedSpellOverlay` — single-slot badge for the currently selected innate spell. Default position is the slot immediately left of the hotbar's first item slot; configurable via `selectedXOffset` / `selectedYOffset`. Draws a translucent purple fill behind the spell icon as the visual cue that this is "innate" (distinguishes from ISS's bar).
   - `InnateSpellWheelOverlay` — radial menu with mouse-cursor wedge selection, spell info text (name, level, mana, unique effects). Ported from `SpellWheelOverlay`. Mouse is released on open and re-grabbed on close.

The atlas at `assets/irons_spellbooks_pufferfish_compat/textures/gui/icons.png` is a verbatim copy of ISS's `icons.png` (CC-BY-4.0, see CREDITS.md). The cooldown overlay on the wheel is **not** ported because read APIs for `ClientMagicData.getCooldownPercent` live outside the `:api` classifier.

Why a 1Hz tick poll and not Pufferfish's `SkillUnlock` event? Pufferfish's API exposes the event signature as `(ResourceLocation category, String skill)` — the player isn't passed, so a targeted push from the callback isn't possible. The tick poll diffs against a per-player hash cache, so it's cheap and catches every change source (GUI clicks, `/puffish_skills` commands, programmatic unlocks).

## Architecture

```
IronsSpellbooksPufferfishCompat   bootstrap; registers reload listeners

requirement/SpellSkillRequirement      record: (category, skill) + Codec
requirement/SpellSkillRequirements     SimpleJsonResourceReloadListener at spell_skill_requirements/
requirement/SkillBlockedSpells         record: (blockingSkill: SkillKey, blockedSpells: List<ResourceLocation>) + Codec
requirement/SpellSkillBlocks           SimpleJsonResourceReloadListener at skill_blocks/; inverts files into Map<spell, Set<SkillKey>>

skills/SkillKey                  record: (category: ResourceLocation, skill: String)
innate/InnateSpellGrant          record: (spell, level) + Codec + StreamCodec
innate/InnateSpellGrants         SimpleJsonResourceReloadListener at innate_spells/, keyed by SkillKey
innate/InnatePool                composes a ServerPlayer's current pool from grants + Pufferfish state; dedupes by spell, keeps highest level

skills/PuffishSkillsLookup       isolates Pufferfish API: hasUnlocked(player, SkillKey)
skills/InnateSyncBootstrap       login + tick-driven pool sync to clients

cast/SpellCastGate               SpellPreCastEvent handler (the cast gate); owns collectRequiredSkills
cast/SpellInscribeGate           InscribeSpellEvent handler (the inscribe gate); reuses collectRequiredSkills
cast/InnateSpellCaster           server-side: attemptInitiateCast with ItemStack.EMPTY

network/CastInnatePayload        C→S: cast at pool index
network/InnatePoolPayload        S→C: full pool snapshot
network/CompatPayloads           payload registration on the MOD bus

client/ClientInnateState         volatile pool + selected index (cycleBy, setSelectedIndex)
client/InnateKeybinds            four KeyMappings (open/toggle wheel, cast, scroll modifier)
client/InnateInputHandler        keybinds + mouse-scroll routing
client/InnatePayloadHandler      client-side pool sync entry point

config/InnateHudDisplay          enum (Always | Contextual | Never)
client/hud/InnateSelectedSpellOverlay  single-slot badge for the currently selected innate spell
client/hud/InnateSpellWheelOverlay  radial wheel (port of SpellWheelOverlay)
client/hud/InnateHudLayers       RegisterGuiLayersEvent — registers both overlays

config/ClientConfig              ModConfigSpec: HUD_DISPLAY, SELECTED_X/Y_OFFSET, WHEEL_SCALE, WHEEL_CONSISTENT_SIZE
```

Asset & code attribution lives in `CREDITS.md`. `assets/.../textures/gui/icons.png` is a verbatim CC-BY-4.0 copy of ISS's atlas; the two overlay classes and `InnateBarLayout` are MIT-licensed ports of ISS source code.

Each cross-mod call lives in exactly one class: Iron's Spellbooks goes through `SpellCastGate` and `InnateSpellCaster`; Pufferfish goes through `PuffishSkillsLookup` and `InnatePool`. The blast radius of upstream API churn is contained.

## Build & run

- `./gradlew build` — compile + test.
- `./gradlew test` — JUnit tests only (codec, requirement store, pool composition).
- `./gradlew runClient` — launches the dev client (requires Iron's Spellbooks + Pufferfish + their deps in `run/mods/` for live testing; the `compileOnly` deps alone don't make a runnable client).

## Project conventions

- **Fail closed when a requirement is set.** If a mapping references a missing skill / category / spell, the cast is blocked and a warning is logged — typos in datapack files surface immediately.
- **One mod boundary, one class.** Iron's Spellbooks calls live in `cast/`, Pufferfish calls in `skills/` and `innate/InnatePool`. Keeps the blast radius of API churn contained.
- **Records for data; codecs live next to the record.**
- **Pure logic is package-private and directly testable** (`SpellSkillRequirements.parseAll`, `InnateSpellGrants.parseAll`, `InnatePool.compose`). The reload listener and Pufferfish/Minecraft-bound parts stay thin shells around them.
- **No backwards-compat shims** for code that's never shipped.

## Composition gotcha

If a spell is BOTH in `spell_skill_requirements/` AND granted by an entry in `innate_spells/`, the gate still runs. A player who innately unlocked the spell via skill A but the gate requires skill B will still be blocked. If you want innate-access to imply gate-satisfied, either point both files at the same skill, or rely solely on the gate (the absence of a grant just means the spell needs a spellbook to ignite).

## Reference

`claude_reference/REFERENCE.md` has the minimum slice of both upstream APIs we actually call (event signature, cast entry point, registry lookups, payload registration, Maven coords). Full extracted jars sit under `claude_reference/extracted/`.

---

# Reusable Engineering Standards

The sections below are project-agnostic. Copy everything from the `---` separator above through the end of this file into any other project's `CLAUDE.md` unchanged to apply the same standards there.

## Code Style

**Never write comments.** No inline `//` comments, no `/* */` blocks, no javadoc, no leading explanatory headers on methods or fields. Code must be self-documenting through naming alone.

- Variable names describe what the value *is* (e.g. `armorCoveragePercent`, not `acp` with a comment).
- Method names describe what they *do* and under what conditions (e.g. `applyMultiplierIfAttackerIsPlayer`, not `applyBonus` with a comment explaining the player check).
- Extract a well-named helper method instead of writing a comment to explain a block.
- Constants get descriptive names that encode their meaning and unit (e.g. `KNIGHTMETAL_BONUS_DAMAGE_AT_FULL_ARMOR`, not `MAX` with a `// 2.0 vs fully-armored target` comment).
- If a name would need a comment to explain it, rename it until it doesn't.

Existing files may still contain comments and javadoc — leave them in place when editing unrelated code, but do not add new ones and prefer to delete obsolete ones when touching the surrounding code.

**Never leave dead code.** No unused methods, fields, classes, parameters, or imports. No "escape hatch" or "just in case" code. No commented-out blocks. If it's not called, delete it — the git history is the archive.

## Code Review

When asked to review code, do a "pass", check for issues, or otherwise audit a recent change, do **two** passes in order:

1. **Self-audit first.** Read the diff yourself. Fix the obvious — dead code, comments, naming, anything that violates the Code Style rules above. Report findings.
2. **Then spawn an independent reviewer** via the `/code-review` skill or a fresh agent. Give it only the diff and the goal, no context about why you made the choices you did. That catches the bugs you would otherwise rationalize away.

Don't skip step 2 because step 1 looked clean — the value of the independent reviewer is exactly that it doesn't share your blind spots.
