# Upstream API reference

Just the parts this mod actually touches. Full extracted jars sit alongside this file under `extracted/`.

## Iron's Spells 'n Spellbooks

- Maven: `https://code.redspace.io/releases`
- Group / artifact: `io.redspace:irons_spellbooks`
- Latest 1.21.1: `1.21.1-3.15.6`
- API classifier: `api` (compileOnly). Full jar (runtime/localRuntime) has no transitive POM deps — extra mods (GeckoLib, Player Animator, Curios, Iron's Lib) need to be sourced separately.
- Mod id: `irons_spellbooks`
- **API package boundary:** only `io.redspace.ironsspellbooks.api.**` is stable. Classes outside that package (e.g. `io.redspace.ironsspellbooks.spells.NoneSpell`) are NOT in the API jar — referencing them from compileOnly code will fail. Detect "unknown spell" by `null` plus an ID mismatch against `getSpellResource()` rather than comparing to `SpellRegistry.none()`.

### `SpellPreCastEvent` — the gate hook

`io.redspace.ironsspellbooks.api.events.SpellPreCastEvent`

```java
public class SpellPreCastEvent
    extends net.neoforged.neoforge.event.entity.player.PlayerEvent
    implements net.neoforged.bus.api.ICancellableEvent {

    public SpellPreCastEvent(Player player, String spellId, int spellLevel,
                             SchoolType schoolType, CastSource castSource);

    public String getSpellId();        // e.g. "irons_spellbooks:fireball"
    public SchoolType getSchoolType();
    public int getSpellLevel();
    public CastSource getCastSource();
}
```

- Fired on the **game** event bus (`NeoForge.EVENT_BUS`), not the mod bus.
- Setting `event.setCanceled(true)` aborts the cast cleanly (mod's own cast logic checks this).
- `getEntity()` (inherited from `PlayerEvent`) returns the `Player`.
- Companion `SpellOnCastEvent` fires after, and is not cancellable.

Source kept at `extracted/irons_spellbooks-1.21.1-3.15.6-api/io/redspace/ironsspellbooks/api/events/SpellPreCastEvent.java`.

### `AbstractSpell.attemptInitiateCast` — the cast entry point

`io.redspace.ironsspellbooks.api.spells.AbstractSpell`

```java
public boolean attemptInitiateCast(
    ItemStack itemStack,        // ItemStack.EMPTY when there is no item (innate cast)
    int spellLevel,
    Level level,
    Player player,
    CastSource castSource,      // SPELLBOOK | SCROLL | SWORD | MOB | COMMAND | NONE
    boolean triggerCooldown,
    String castingEquipmentSlot // free-form label, used by ISS for cooldown bookkeeping
);
```

- This is the same method ISS calls when a player ignites a cast from a spellbook. It runs pre-cast checks, fires `SpellPreCastEvent` (so the gate composes naturally), charges mana, fires `SpellOnCastEvent`, plays the animation, executes the spell.
- `CastSource` is an enum with `consumesMana()` and `respectsCooldown()` — use `SPELLBOOK` for normal cost, `NONE` to bypass both.
- Resolve the `AbstractSpell` via `SpellRegistry.getSpell(ResourceLocation)`. A successfully-resolved spell satisfies `grant.spell().equals(spell.getSpellResource())`; anything else means the registry returned a fallback.

## Pufferfish's Skills

- Maven: `https://maven.puffish.net`
- Group / artifact: `net.puffish:skillsmod`
- 1.21.1 build: `0.17.3+1.21` with classifier `neoforge`
- Mod id: `puffish_skills`

### Lookup chain

`net.puffish.skillsmod.api.SkillsAPI`

```java
SkillsAPI.getCategory(ResourceLocation)    // -> Optional<Category>
    .flatMap(c -> c.getSkill(String))      // -> Optional<Skill>
    .map(s -> s.getState(serverPlayer))    // -> Optional<Skill.State>
```

`Skill.State` enum values: `LOCKED`, `AVAILABLE`, `AFFORDABLE`, `UNLOCKED`, `EXCLUDED`. Treat anything other than `UNLOCKED` as "skill not learned".

- `getState(ServerPlayer)` only — Pufferfish state lives on the server. Don't try to call this on a logical-client `Player`.
- Categories are addressed by `ResourceLocation` (datapack id of the category).
- Skills within a category are addressed by `String` (the per-category id).

### Iterating a player's unlocked skills

```java
SkillsAPI.streamCategories().forEach(category ->
    category.streamUnlockedSkills(serverPlayer).forEach(skill -> {
        // (category.getId(), skill.getId())
    }));
```

### Caveat: `SKILL_UNLOCK` / `SKILL_LOCK` events don't carry the player

`SkillsAPI.registerSkillUnlockEvent(Events.SkillUnlock)` invokes the callback with `(ResourceLocation category, String skillId)` only — there is no `ServerPlayer` parameter. That means you cannot push a targeted update to "the player who just unlocked" from within the callback.

This mod works around that by polling on `ServerTickEvent.Post` once per second and diffing each player's pool against a cached hash. The cost is bounded (≤ N players × M categories × K skills, all O(1) lookups), and the worst-case latency between unlock and pool sync is ~1 second.

## NeoForge 1.21.1 networking

- `RegisterPayloadHandlersEvent` fires on the **MOD bus**. Get a registrar via `event.registrar("1")` and call `playToServer`/`playToClient`.
- Payload type: `CustomPacketPayload.Type<T> TYPE = new CustomPacketPayload.Type<>(rl)`.
- Stream codec: `StreamCodec.composite(...)`; wrap an item codec into a list codec with `ITEM_CODEC.apply(ByteBufCodecs.list())`.
- Send to one player: `PacketDistributor.sendToPlayer(serverPlayer, payload)`. Send to server: `PacketDistributor.sendToServer(payload)`.
- Handler signature: `(payload, IPayloadContext ctx)`. Hop to the main thread with `ctx.enqueueWork(...)`.
- For client-bound handlers that touch client-only classes, guard registration / dispatch with `FMLEnvironment.dist == Dist.CLIENT` so the server JVM never loads the client class.

## NeoForge 1.21.1 client extras

- HUD overlay: `RegisterGuiLayersEvent` on the **MOD bus** (client-only). `event.registerAboveAll(rl, layer)` where `layer` is a `LayeredDraw.Layer` — i.e. `(GuiGraphics, DeltaTracker) -> void`.
- Keybinds: `RegisterKeyMappingsEvent` on the **MOD bus** (client-only). `new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, key, category)` + `event.register(mapping)`. `KeyConflictContext.IN_GAME` is what tells MC to ignore the binding when a screen is open.
- Client tick: `net.neoforged.neoforge.client.event.ClientTickEvent.Post` on the **GAME bus**. Drain key presses with `while (mapping.consumeClick()) { ... }`.
- Mouse scroll: `net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent` on the **GAME bus**. `event.getScrollDeltaY()` returns the vertical scroll amount; `event.setCanceled(true)` swallows it from the hotbar.
- Releasing / grabbing the mouse cursor: `Minecraft.getInstance().mouseHandler.releaseMouse()` / `grabMouse()` — needed to let a radial wheel be cursor-driven instead of camera-driven.

## Iron's Spellbooks UI internals (the parts we port)

These classes live OUTSIDE the `:api` classifier, so we re-implement against the published source rather than depending on them:

- `io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay` — the always-visible horizontal spell bar. Atlas: `irons_spellbooks:textures/gui/icons.png`. Sprite sheet coords used by our port:
  - Slot border: `(66, 84, 22, 22)`
  - Selected outline: `(0, 84, 22, 22)`
  - Innate/non-spellbook overlay: `(132, 84, 22, 22)` (called "the second border" in ISS — applied to all non-selected slots in our port since every innate slot is the same kind)
  - Cooldown fill: `(47, 87, 16, h)` — we don't currently render this; needs non-API `ClientMagicData.getCooldownPercent`.
- `io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay` — radial menu. Hit-tests via mouse angle around screen center; `Utils.getAngle(mousePos, screenCenter) + 1.570 + radiansPerSpell/2`. Uses `RenderType.gui()` for vertex-consumer-based shaded wedges. The wheel border sprite is at `(0, 106, 32, 32)` for unselected / `(32, 106, 32, 32)` for selected.
- `io.redspace.ironsspellbooks.player.ClientRenderCache.generateRelativeLocations` — packs N spells into a 1-, 2-, or 3-row centered grid. Our `InnateBarLayout` is a direct port.

We pin to ISS 1.21.1-3.15.6 sprite coordinates. If ISS reshuffles the atlas, our texture (a verbatim copy bundled under our own namespace) keeps showing the old layout — visual stays stable across upstream sprite changes, at the cost of not auto-updating to new ISS sprite tweaks.

## Datapack formats (this mod)

### Skill gate — per-spell file (`spell_skill_requirements/`)

```
data/<datapack_namespace>/spell_skill_requirements/<arbitrary_path>.json
```

```json
{
  "spell": "<spell_namespace>:<spell_id>",
  "category": "<datapack_ns>:<category_id>",
  "skill": "<skill_id_within_category>"
}
```

The file path is organizational only — the spell ID comes from the JSON `spell` field. This avoids the `SimpleJsonResourceReloadListener` quirk where the path-derived `ResourceLocation` key uses the *datapack* namespace, not the spell's. One file = one (spell → required-skill) entry.

### Skill gate — per-skill file (`skill_blocks/`)

```
data/<datapack_namespace>/skill_blocks/<arbitrary_path>.json
```

```json
{
  "category": "<datapack_ns>:<category_id>",
  "skill": "<skill_id_within_category>",
  "blocks": [
    "<spell_ns>:<spell_a>",
    "<spell_ns>:<spell_b>"
  ]
}
```

One file = one (skill → many blocked spells) entry. The path is organizational only; `category` and `skill` come from inside the JSON. Inverted at load time into a `Map<spell, Set<SkillKey>>` for O(1) lookup.

**Combined semantics:** for a given spell, the gate unions skills from both files. The player must have unlocked **every** skill in the union (AND semantics). If neither store mentions the spell, casting is unaffected.

### Innate spells

```
data/<datapack_namespace>/innate_spells/<arbitrary_path>.json
```

```json
{
  "category": "<datapack_ns>:<category_id>",
  "skill": "<skill_id>",
  "spell": "irons_spellbooks:<spell_id>",
  "level": 1
}
```

The file path is organizational only; the `(category, skill)` pair from the JSON is the primary key. Duplicate keys log a warning and the last-loaded entry wins. `level` defaults to 1.

Reload at runtime with `/reload`.
