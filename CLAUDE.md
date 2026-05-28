# Iron's Spellbooks ↔ Pufferfish's Skills compat

NeoForge 1.21.1 mod that bridges Iron's Spells 'n Spellbooks and Pufferfish's Skills.

## Features

### 1. Skill-gated casting + inscribing (`spell_skill_requirements/`)

Block a spell cast OR inscription when the player has not unlocked one or more configured Pufferfish skills. The gate fires for every cast source (SPELLBOOK, SCROLL, SWORD, COMMAND, …) since all funnel through `AbstractSpell.attemptInitiateCast`, and separately for `InscribeSpellEvent` at the inscription table.

**One authoring shape — a rule that groups spells with the skills they require.** Drop `data/<pack>/spell_skill_requirements/<arbitrary>.json`:

```json
{
  "spells": ["irons_spellbooks:fire_arrow", "irons_spellbooks:fire_breath"],
  "requires": [
    {"category": "<cat>", "skill": "<skill_id>"},
    {"category": "<cat>", "skill": "<skill_id_2>"}
  ]
}
```

Semantics: *every spell in `spells` requires every skill in `requires`* (AND). The file path is organizational; the spell IDs live inside the JSON. Multiple rules that name the same spell are unioned, so a spell ends up requiring the union of every rule's `requires`. No rule names a spell → no gate for it (opt-in).

This one format covers both directions: "one skill gates many spells" (many `spells`, one entry in `requires`) and "one spell needs many skills" (one spell, many `requires`).

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

A small badge to the left of the hotbar shows only the *currently selected* innate spell (slot frame + spell icon, drawn from the mod's `icons.png` atlas). Position is set by `selectedXOffset` / `selectedYOffset` in `config/irons_spellbooks_pufferfish_compat-client.toml`; display mode is `Always` / `Contextual` (fades in on selection-change/cast) / `Never`. The wheel keeps the multi-spell radial layout — only the always-on indicator changed.

### Planned (not implemented yet)

- Skill node reward that grants `+N` to a skill when a spell is cast.

## How feature 1 (the gate) works

1. `SpellPreCastEvent` (cancellable, all cast sources) and `InscribeSpellEvent` (cancellable, inscription table) fire server-side.
2. Both gates call `SkillGateEvaluator.collectRequiredSkills(spellId)`, which returns `SpellSkillRequirements.INSTANCE.requiredSkillsFor(spellId)` — the union of every rule's `requires` for that spell.
3. For each `SkillKey` in the set, `PuffishSkillsLookup.hasUnlocked(player, skillKey)` asks Pufferfish.
4. First unmet skill → `event.setCanceled(true)` + action-bar message. All satisfied → cast/inscribe proceeds.

## How feature 2 (innate spells) works

Server side:

1. `InnateSpellGrants` (datapack reload listener) holds a `Map<SkillKey, InnateSpellGrant>` parsed from `innate_spells/*.json`.
2. `InnateSyncBootstrap` recomputes a player's pool and pushes `InnatePoolPayload` only when the pool changes (value equality against the last snapshot sent). It recomputes on two triggers: Pufferfish's `SkillsAPI` unlock/lock events — targeted to the affected player (the event carries the `ServerPlayer`) and gated to skills that actually grant an innate spell (`InnateSpellGrants.grantingSkills().contains(skillKey)`), so unrelated skill changes cost nothing; and `OnDatapackSyncEvent`, which fires on login (syncs the joining player) and on `/reload` (resyncs all online players, catching innate-grant datapack edits). A respec is covered because Pufferfish's `resetSkills` now emits a lock event per skill it locks.
3. The client press of the cast keybind sends a `CastInnatePayload(spellId)`. The server re-derives the pool (authoritative), confirms the requested spell is in it, and calls `AbstractSpell.attemptInitiateCast(ItemStack.EMPTY, level, world, player, CastSource.SPELLBOOK, true, "innate")` with the level clamped to the spell's range.

Client side:

1. `InnatePayloadHandler` updates `ClientInnateState` on every pool sync.
2. `InnateKeybinds` registers four `KeyMapping`s (`OPEN_WHEEL` hold, `TOGGLE_WHEEL`, `CAST_INNATE`, `SCROLL_CYCLE_MODIFIER` hold).
3. `InnateInputHandler` (`ClientTickEvent.Post` + `InputEvent.MouseScrollingEvent`) handles all of it: drains cast clicks → sends packet, hold-to-open + release-to-commit on the wheel key, scroll-while-modifier cycles selection, toggle key flips the wheel.
4. `InnateHudLayers` (`RegisterGuiLayersEvent`) registers two overlays above all vanilla layers:
   - `InnateSelectedSpellOverlay` — single-slot badge for the currently selected innate spell (slot frame + spell icon + selected-outline ring, blitted from `icons.png`). Default position is the slot immediately left of the hotbar's first item slot; configurable via `selectedXOffset` / `selectedYOffset`.
   - `InnateSpellWheelOverlay` — radial menu with mouse-cursor wedge selection, spell info text (name, level, mana, unique effects). Ported from `SpellWheelOverlay`. Mouse is released on open and re-grabbed on close.

The atlas at `assets/irons_spellbooks_pufferfish_compat/textures/gui/icons.png` is a verbatim copy of ISS's `icons.png` (CC-BY-4.0, see CREDITS.md). The cooldown overlay on the wheel is **not** ported because read APIs for `ClientMagicData.getCooldownPercent` live outside the `:api` classifier.

Why purely event-driven (no poll)? Pufferfish's `SkillUnlock`/`SkillLock` events now fire `(ServerPlayer player, ResourceLocation category, String skill)` and `resetSkills` emits a lock event per skill it locks, so the events are both player-targeted and complete — a respec arrives as a burst of lock events, each correctly attributed. We react only to the affected player, and only when the changed skill grants an innate spell, so the per-event cost is O(1)-ish (a `Set.contains`) and recompute happens just for the one player. `OnDatapackSyncEvent` covers login and `/reload`. The earlier 1Hz/10s tick poll was a workaround for the old eventless reset path and the missing player; both are gone now, so the poll was removed. (Requires the Pufferfish release that carries these event changes — see "Pending upstream" below.)

**Pending upstream:** this builds against a local pre-release Pufferfish jar (`claude_reference/puffish_skills-*.jar`) wired via `files(...)` in `build.gradle`. When that release publishes: (1) restore the `curse.maven` `compileOnly`/`localRuntime` lines in `build.gradle` with the new file id, and (2) bump the `puffish_skills` `versionRange` floor in `neoforge.mods.toml` to that release — the new event signatures are not present in 0.17.3, so loading against an older Pufferfish would fail at runtime.

## Architecture

```
IronsSpellbooksPufferfishCompat   bootstrap; registers reload listeners

requirement/SpellSkillRequirement      record: (spells: List<ResourceLocation>, requiredSkills: List<SkillKey>) + Codec
requirement/SpellSkillRequirements     SimpleJsonResourceReloadListener at spell_skill_requirements/; fans each rule out into Map<spell, Set<SkillKey>> (union across rules)

skills/SkillKey                  record: (category: ResourceLocation, skill: String) + Codec
innate/InnateSpellGrant          record: (spell, level) + Codec + StreamCodec
innate/InnateSpellGrants         SimpleJsonResourceReloadListener at innate_spells/, keyed by SkillKey
innate/InnatePool                composes a ServerPlayer's current pool from grants + Pufferfish state; dedupes by spell, keeps highest level

skills/PuffishSkillsLookup       isolates Pufferfish API: hasUnlocked(player, SkillKey)
skills/InnateSyncBootstrap       pool sync to clients: player-targeted SkillsAPI unlock/lock events (gated to granting skills) + OnDatapackSyncEvent (login + /reload), diff-gated

cast/SkillGateEvaluator          shared gate logic; owns collectRequiredSkills + blockIfMissingRequiredSkill
cast/SpellCastGate               SpellPreCastEvent handler (the cast gate); delegates to SkillGateEvaluator
cast/SpellInscribeGate           InscribeSpellEvent handler (the inscribe gate); delegates to SkillGateEvaluator
cast/InnateSpellCaster           server-side: attemptInitiateCast with ItemStack.EMPTY; clamps grant level to the spell's range

network/CastInnatePayload        C→S: cast the named spell (server re-derives the authoritative pool)
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

If a spell is BOTH in `spell_skill_requirements/` AND granted by an entry in `innate_spells/`, the gate still runs. A player who innately unlocked the spell via skill A but the gate requires skill B will still be blocked. If you want innate-access to imply gate-satisfied, either point the requirement rule at the same skill that grants it, or rely solely on the gate (the absence of a grant just means the spell needs a spellbook to ignite).

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
