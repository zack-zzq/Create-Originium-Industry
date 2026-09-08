# Registry, lang keys, and save compatibility

This document freezes identifiers for Create: Originium Industry so later features
(dust refactor, purification, reactor) do not break existing worlds, recipes, or
translations.

**Rule:** registry ids are compatibility contracts. Display names are not.

Inspired-by-Arknights flavour belongs in lang, Ponder, and tooltips — never in
registry ids, recipe paths, or NBT keys.

## Compatibility principles

1. **Do not rename** a published item, block, fluid, effect, recipe, tag, or
   attachment id. Add a new id and deprecate the old one if the design changes.
2. **Do not change** the namespace `create_originium_industry`.
3. **Display names may change.** `originium_catalyst` stays the registry id;
   players see 培养液 / Originium Catalyst.
4. **NBT and attachments** must keep reading old keys. New fields are additive.
   When a schema actually changes, write a `version` int (start at `1`) and
   migrate in `deserializeNBT`.
5. **Lang keys** follow `category.create_originium_industry.path`. Missing keys
   are a bug; deleting a key that is still referenced is a bug.
6. **Common tags (`c:`)** already published stay. Conventional aliases may be
   added beside them.

The dust / pollution simulation is being refactored separately. Treat
`core/oridust` as unstable **implementation**, but keep the ids in this file
stable unless a migration is documented here first.

## Frozen registry ids

### Items

| Registry id | zh_cn | en_us | Notes |
|---|---|---|---|
| `raw_originium` | 粗制源石 | Raw Originium | Mined from Overworld ore |
| `originium_shard` | 源石碎片 | Originium Shard | |
| `originium` | 源石 | Originium | |
| `originium_dust` | 源石尘 | Originium Dust | Item form; chunk pollution is separate |
| `originium_alloy_ingot` | 源石合金锭 | Originium Alloy Ingot | Also tagged under `c:ingots` |
| `purest_originium` | 至纯源石 | Purest Originium | Item exists; no survival recipe yet |
| `originium_dust_sieve` | 源石尘滤网 | Originium Dust Sieve | Placeable Basin/process attachment; also inserted into the kinetic filter |
| `originium_dust_nozzle` | 源石尘分散滤网 | Originium Dust Nozzle | Encased Fan attachment (block + BlockItem, same id) |
| `originium_debug_wand` | 源石调试器 | Originium Debug Wand | Debug-only; do not add survival recipes |
| `originium_respirator` | 源石防护面罩 | Originium Respirator | Head-slot Equipable; tagged `originium_protection` |
| `originium_filter_canister` | 源石滤毒罐 | Originium Filter Canister | Chest-slot Equipable; tagged `originium_protection` |
| `molten_originium_bucket` | 熔融源石桶 | Molten Originium Bucket | Generated with the fluid |
| `purest_molten_originium_bucket` | 至纯熔融源石桶 | Purest Molten Originium Bucket | Generated with the fluid |
| `originium_catalyst_bucket` | 培养液桶 | Originium Catalyst Bucket | Display = 培养液; id stays catalyst |

### Blocks and block entities

| Registry id | Notes |
|---|---|
| `originium_dust_filter` | Kinetic dust absorber (block + block entity share this path). BE NBT: `HasSieve`, `SieveDurability`, additive `CapturedDust` |
| `originium_dust_sieve` | Process sieve attachment (block + BE share this path with the item). BE NBT: `SieveDurability`, additive `CapturedDust` |
| `originium_dust_nozzle` | Encased Fan nozzle (block + BE share this path with the item). BE NBT: `LastMoved`, `HasFlow` |
| `originium_dust_meter` | Dust gauge. BE NBT: `Dust`, `Risk`, `ProtectionPercent` (client packet snapshot; goggles prefer nearby `VisibleDust` cache) |
| `raw_originium_ore` | Overworld stone ore. Drops frozen item `raw_originium` (silk touch keeps the block). Iron pickaxe. |
| `deepslate_raw_originium_ore` | Deepslate variant of the same ore / drops |

### Fluids

| Registry id | zh_cn | en_us | Notes |
|---|---|---|---|
| `molten_originium` | 熔融源石 | Molten Originium | Density fluid |
| `purest_molten_originium` | 至纯熔融源石 | Purest Molten Originium | No production recipe yet |
| `originium_catalyst` | 培养液 | Originium Catalyst | **Never rename.** Display name is 培养液 |

Still / flowing textures live at `textures/fluid/<id>_still.png` and
`textures/fluid/<id>_flow.png`.

### Effect

| Registry id | zh_cn | en_us |
|---|---|---|
| `ori_dust_sickness` | 源石暴露症状 | Originium Exposure Sickness |

Exposure-layer effect. Long-term infection uses vanilla effects via `InfectionStage` (no extra effect id).

### Creative tab

| Registry id | lang key |
|---|---|
| `main` | `itemGroup.create_originium_industry.main` |

### Attachments

| Attachment id | Holder | Current NBT keys | Version field |
|---|---|---|---|
| `chunk_oridust_data` | chunk | `DustLevel` (int) | **not present — add on next schema change** |
| `player_exposure_data` | player (`copyOnDeath`) | `Exposure`, `Infection` (ints) | **not present — add on next schema change** |

`copyOnDeath` still copies the attachment on respawn. Additive knobs `player_exposure.deathExposureRetain` (default 0) and `deathInfectionRetain` (default 0.25) then scale the clone so singleplayer deaths are not a spiral. Set both to `1.0` to keep the old full-retain behaviour.

### Config file

Common config spec is `COIConfig.COMMON_SPEC`. Top-level keys:

- `dust_diffusion`
- `dust_production`
- `player_exposure`
- `feature_toggles`
- `dust_filter`
- `dust_nozzle` *(additive)*
- `dust_meter` *(additive)*
- `worldgen` *(additive; raw originium ore frequency / height)*
- `protection` *(additive; respirator + canister)*
- `infection` *(additive; stage thresholds)*
- `reactor` *(additive; M3 stub knobs)*
- `multiplayer` *(additive; dedicated-server spread policy + nearby client dust sync)*
- `debug`

Do not rename these sections once a release has shipped. New sections are fine.

Client spec is `COIConfig.CLIENT_SPEC` (`ModConfig.Type.CLIENT`). Top-level key:

- `client` — accessibility: `reduceFlicker`, `simplifyParticles`, `particleDensity`, `highContrastIndicators`, `uiDetailLevel`, `debugOverlayDetail`, `showSicknessHud`

Read client values through `COIClientOptions` so a dedicated server (where the client spec is not loaded) never calls `.get()` on an unloaded spec.

### Worldgen (raw originium ore)

Shipped datapack ids (overridable):

| Path | Role |
|---|---|
| `worldgen/configured_feature/raw_originium_ore` | Feature type `create_originium_industry:raw_originium_ore` (vein size / air-discard from config) |
| `worldgen/placed_feature/raw_originium_ore` | Count + triangle height from config (`raw_originium_count` / `raw_originium_height`) |
| `neoforge/biome_modifier/add_raw_originium_ore` | `neoforge:add_features` into `#minecraft:is_overworld`, step `underground_ores` |

`worldgen` config keys: `enableRawOriginiumOre`, `veinSize` (4), `veinsPerChunk` (4), `minY` (-64), `maxY` (16), `discardChanceOnAirExposure` (0.7). Scarcer than vanilla diamond small (7 veins). Existing chunks are not rewritten; only new generation is affected.

To disable via datapack without touching config, replace the biome modifier with `{ "type": "neoforge:none" }`. Replacing the placed feature JSON drops the config knobs unless you keep the `raw_originium_count` / `raw_originium_height` placement types.

## Frozen recipe ids

Datapack path is the recipe id (`create_originium_industry:<path>`).

| Recipe id | Type | Result |
|---|---|---|
| `milling/raw_originium_milling` | `create:milling` | shards from raw |
| `crushing/raw_originium_crushing` | `create:crushing` | shards from raw |
| `mixing/originium_shard_mixing` | `create:mixing` + heated | originium from 4 shards |
| `mixing/originium_mixing` | `create:mixing` + superheated | molten originium |
| `mixing/molten_originium_iron_ingot_mixing` | `create:mixing` | alloy ingot |
| `mixing/catalyst_mixing` | `create:mixing` + heated | originium catalyst (培养液) |
| `crafting/originium_dust_sieve` | `minecraft:crafting_shaped` | process sieve (also kinetic-filter consumable) |
| `crafting/originium_dust_nozzle` | `minecraft:crafting_shaped` | Encased Fan nozzle |
| `crafting/originium_dust_meter` | `minecraft:crafting_shaped` | dust meter |
| `crafting/originium_dust_filter` | `minecraft:crafting_shaped` | kinetic filter |
| `crafting/originium_respirator` | `minecraft:crafting_shaped` | head-slot dust mask |
| `crafting/originium_filter_canister` | `minecraft:crafting_shaped` | chest-slot filter tank |

Dust production for frozen recipe ids is keyed in datapack JSON under
`data/create_originium_industry/coi_dust_emission/` (recipe / item / item_tag +
`amount`). `COIConfig` `dust_production.*` values **override** the five
processing ids (mill / crush / heated shard mix / superheated melt / alloy mix)
so existing server.toml knobs keep working. Other recipe ids use the datapack
amount only. **Keep these ids working** (or data-gen aliases).

Heat tiers (intentional):

| Recipe | Heat | Default dust |
|---|---|---|
| `milling/raw_originium_milling` | none | 80 |
| `crushing/raw_originium_crushing` | none | 100 |
| `mixing/originium_shard_mixing` | heated | 120 |
| `mixing/originium_mixing` | superheated | 200 |
| `mixing/molten_originium_iron_ingot_mixing` | none (already molten) | 60 |
| `mixing/catalyst_mixing` | heated | **0** |

**Intentional omissions**

- `mixing/catalyst_mixing` ships `amount: 0`. It is heated, but the feedstock is
  redstone / sugar / lapis / water — no originium — so it must not pollute.
- No furnace, smoker, blast-furnace, or encased-fan processing recipes exist
  for originium; there are no extra mixins for those machines.
- Create 6.0.4 stores the *recipe type* on `ProcessingRecipe.id` (`create:mixing`,
  `create:milling`, `create:crushing`), not the datapack id. Machine mixins pass
  the live `Recipe` and `DustProductionHelper` resolves `RecipeHolder.id()`.
- Unmapped processing recipes that still consume originium items/fluids emit via
  a heat-aware fallback (superheated → melting amount, heated → shard-mix amount,
  originium fluid → alloy amount, else tagged-item amount).

JSON files live at `data/<namespace>/coi_dust_emission/*.json`:

```json
{ "recipe": "create_originium_industry:milling/raw_originium_milling", "amount": 80 }
{ "item": "modid:some_item", "amount": 40 }
{ "item_tag": "create_originium_industry:dust_producing", "amount": 40 }
```

Machines submit through `IOridustProducer` (`DustSubmission`). Devices that
reduce emission or absorb chunk dust implement `IDustPurifier` (the kinetic
filter and the Basin/process sieve). A spinning filter with a sieve, or a
placed `originium_dust_sieve` on the emit block / a neighbouring face / a
Basin beside a mixer, captures a configured fraction and converts it to
`originium_dust` (remainder stays in `ByproductBuffer`).

`originium_dust_nozzle` attaches to an Encased Fan (`IAirCurrentSource`) and
redirects chunk dust into the downwind neighbouring chunk (`DustReason.DIFFUSER`).
It never voids dust.

The dust meter copies server chunk dust onto the block entity (goggles /
comparator). Nearby players also receive a low-frequency dirty-set / on-demand
window of chunk dust (`multiplayer.syncDustToClients`, default on). Meter
goggles, debug overlay, and debug tooltips read `VisibleDust` so two
clients looking at the same chunk agree after a short delay. Exposure and
infection sync only to the local player.

Protection gear (`originium_respirator` head, `originium_filter_canister` chest)
is tagged `originium_protection`. A full set is **2** pieces (`protection.protectionFullSetPieces`;
older configs that still have `4` from the M0 skeleton should retune). Broken gear
drops `originium_dust` back into the factory loop.

Infection stages (additive `infection` section) apply vanilla effects from stored
infection. `ori_dust_sickness` stays the exposure-layer effect.

| Stage | Default infection | Effects |
|---|---|---|
| none | below 200 | — |
| weakness | 200 | Weakness I |
| restricted | 800 | Weakness I + Mining Fatigue I |
| growth | 2500 | Mining Fatigue II + Slowness I + Hunger I |
| bargain | 6000 | Haste I + Strength I + Hunger II |

`create_originium_industry:example/datapack_only` is a shipped mapping (amount
33) for GameTests / pack authors. It is **not** a real recipe.

## Tags

### Published (`c` namespace)

| Tag | Values | Status |
|---|---|---|
| `c:ingots/originium_alloy_ingot` | `originium_alloy_ingot` | Frozen (non-standard path, already used) |
| `c:ingots/originium_alloy` | `originium_alloy_ingot` | Conventional alias; keep both |
| `c:ores` / `c:ores/originium` | `raw_originium_ore`, `deepslate_raw_originium_ore` | Additive |
| `c:ores_in_ground/stone` | `raw_originium_ore` | Additive |
| `c:ores_in_ground/deepslate` | `deepslate_raw_originium_ore` | Additive |
| `c:ore_rates/singular` | both ore blocks | Additive |
| `c:raw_materials` / `c:raw_materials/originium` | `raw_originium` | Additive |
| `c:fluid/molten_originium` | `molten_originium` | Frozen |
| `c:fluid/purest_molten_originium` | `purest_molten_originium` | Frozen |
| `c:fluid/originium_catalyst` | `originium_catalyst` | Frozen |

### Declared in `COITags` (`create_originium_industry` namespace)

| Tag | Purpose | JSON |
|---|---|---|
| `item/originium_materials` | raw, shard, originium, dust, purest | present |
| `item/dust_producing` | items whose processing emits chunk dust | present (raw, shard, originium, dust, purest) |
| `item/originium_protection` | protection gear that reduces exposure/infection | present (`originium_respirator`, `originium_filter_canister`) |
| `block/dust_sources` | blocks that emit dust | present (empty; future COI machines) |
| `block/dust_filters` | blocks that remove/modify dust | present (`originium_dust_filter`, `originium_dust_sieve`, `originium_dust_nozzle`) |
| `block/raw_originium_ores` | stone + deepslate raw originium ore | present |
| `fluid/originium_fluids` | all originium fluids | present |

Do not put `originium_debug_wand` in material tags. Alloy ingot is an ingot,
not an `originium_materials` member (see `COITags` comment).

## Lang key conventions

Pattern: `<category>.create_originium_industry.<path>`

| Category | Example |
|---|---|
| `item` | `item.create_originium_industry.originium` |
| `block` | `block.create_originium_industry.originium_dust_filter` |
| `fluid` | `fluid.create_originium_industry.molten_originium` |
| `effect` | `effect.create_originium_industry.ori_dust_sickness` |
| `itemGroup` | `itemGroup.create_originium_industry.main` |
| `commands` | `commands.coi_debug.dust.get` |
| `hud` | `hud.create_originium_industry.sickness` |
| `infection_stage` | `infection_stage.create_originium_industry.weakness` |

`en_us` and `zh_cn` must stay in lockstep.

### Known missing keys (do not delete; still referenced in Java)

None at the moment. Filter / goggle / HUD / debug tooltip strings live in
`en_us.json` and `zh_cn.json`. If you add a `Component.translatable` call,
add both language keys in the same change.

## Allowed to add later

New ids are fine. Do not reuse a frozen id for a different object.

Expected (not frozen until registered):

- Purification intermediates for the purest line
- Reactor blocks / block entities
- Ponder / JEI lang keys

## Debug surface

- Command: `/coi_debug` (OP 2, gated by `debug.enableDebugCommands`)
- Item: `originium_debug_wand`
- Reactor subcommands are stubs until the reactor exists; keep the command
  names (`reactor status`, `reactor stabilize`) if possible so docs and packs
  do not churn.
