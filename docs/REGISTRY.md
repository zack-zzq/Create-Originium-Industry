# Registry, lang keys, and save compatibility

This document freezes identifiers for Create: Originium Industry so later
changes do not break existing worlds, recipes, or translations. The v1 loop
(dust, purification, reactor) is shipped in `0.0.10-dev`; new ids stay additive.

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

Treat `core/oridust` as unstable **implementation** (helpers and class names
may still move). Keep the ids in this file stable unless a migration is
documented here first. Gameplay contracts — chunk dust, `IOridustProducer` /
`IDustPurifier`, and the published datapack shapes — stay.

## Frozen registry ids

### Items

| Registry id | zh_cn | en_us | Notes |
|---|---|---|---|
| `raw_originium` | 粗制源石 | Raw Originium | Mined from Overworld ore |
| `originium_shard` | 源石碎片 | Originium Shard | |
| `originium` | 源石 | Originium | |
| `originium_dust` | 源石尘 | Originium Dust | Item form; chunk pollution is separate |
| `originium_alloy_ingot` | 源石合金锭 | Originium Alloy Ingot | Also tagged under `c:ingots` |
| `purest_originium` | 至纯源石 | Purest Originium | Survival: filter → 培养液 culture → supercool |
| `originium_dust_sieve` | 源石尘滤网 | Originium Dust Sieve | Placeable Basin/process attachment; also inserted into the kinetic filter |
| `originium_dust_nozzle` | 源石尘分散滤网 | Originium Dust Nozzle | Encased Fan attachment (block + BlockItem, same id) |
| `originium_cooling_chamber` | 源石冷却室 | Originium Cooling Chamber | Basin supercooling + reactor cooling attachment |
| `originium_super_cooling_chamber` | 源石超级冷却室 | Originium Super Cooling Chamber | Super snow-golem cooling attachment |
| `originium_power_core` | 源石动力核心 | Originium Power Core | M3 kinetic generator (BlockItem) |
| `originium_debug_wand` | 源石调试器 | Originium Debug Wand | Debug-only; do not add survival recipes |
| `originium_respirator` | 源石防护面罩 | Originium Respirator | Head-slot Equipable; tagged `originium_protection` |
| `originium_filter_canister` | 源石滤毒罐 | Originium Filter Canister | Chest-slot Equipable; tagged `originium_protection` |
| `originium_sealed_canister` | 源石密封滤毒罐 | Originium Sealed Canister | Alloy chest upgrade; also tagged `originium_reinforced_protection` |
| `originium_alloy_casing` | 源石合金壳体 | Originium Alloy Casing | Pollution-resistant casing (BlockItem) |
| `originium_alloy_sieve` | 源石合金滤网 | Originium Alloy Sieve | Filter upgrade attachment; also kinetic-filter insert |
| `originium_core_housing` | 源石核心外壳 | Originium Core Housing | M3 reactor shell precursor (BlockItem) |
| `molten_originium_bucket` | 熔融源石桶 | Molten Originium Bucket | Generated with the fluid |
| `filtered_molten_originium_bucket` | 过滤熔融源石桶 | Filtered Molten Originium Bucket | Generated with the fluid |
| `cultured_originium_bucket` | 培养源石液桶 | Cultured Originium Bucket | Filtered molten + 培养液 |
| `purest_molten_originium_bucket` | 至纯熔融源石桶 | Purest Molten Originium Bucket | Generated with the fluid |
| `originium_catalyst_bucket` | 培养液桶 | Originium Catalyst Bucket | Display = 培养液; id stays catalyst |
| `originium_coolant_bucket` | 源石冷却液桶 | Originium Coolant Bucket | Generated with the fluid |
| `hot_water_bucket` | 热水桶 | Hot Water Bucket | Generated with the fluid |

### Blocks and block entities

| Registry id | Notes |
|---|---|
| `originium_dust_filter` | Kinetic dust absorber (block + block entity share this path). BE NBT: `HasSieve`, `SieveDurability`, additive `CapturedDust`, additive `SieveKind` (`standard` / `alloy`; missing = standard) |
| `originium_dust_sieve` | Process sieve attachment (block + BE share this path with the item). BE NBT: `SieveDurability`, additive `CapturedDust` |
| `originium_alloy_sieve` | Alloy process sieve. Shares the `originium_dust_sieve` block entity type. New id. |
| `originium_alloy_casing` | Pollution-resistant casing. Tagged `pollution_resistant` + `reactor_housing`. No BE. |
| `originium_core_housing` | Denser M3 shell. Same tags as the casing; stronger per-face emission seal. No BE. |
| `originium_dust_nozzle` | Encased Fan nozzle (block + BE share this path with the item). BE NBT: `LastMoved`, `HasFlow` |
| `originium_dust_meter` | Dust gauge. BE NBT: `Dust`, `Risk`, `ProtectionPercent` (client packet snapshot; goggles prefer nearby `VisibleDust` cache) |
| `originium_cooling_chamber` | Basin supercooling + reactor cooling attachment (block + BE). BE NBT: `ChamberDurability`, additive `version` (1) |
| `originium_super_cooling_chamber` | Super snow-golem cooling. Shares `originium_cooling_chamber` BE type. New id. |
| `originium_power_core` | Kinetic generator (block + BE). BE NBT: `version` (1), `FuelCount`, `FuelTicks`, `Instability`, `Shutdown`, `Heat`/`Capacity`/`Cooling`/`Stability`, `Coolant`/`Water`/`HotWater` tanks |
| `raw_originium_ore` | Overworld stone ore. Drops frozen item `raw_originium` (silk touch keeps the block). Iron pickaxe. |
| `deepslate_raw_originium_ore` | Deepslate variant of the same ore / drops |

### Fluids

| Registry id | zh_cn | en_us | Notes |
|---|---|---|---|
| `molten_originium` | 熔融源石 | Molten Originium | Density fluid |
| `filtered_molten_originium` | 过滤熔融源石 | Filtered Molten Originium | Sieve-gated mixing from molten |
| `cultured_originium` | 培养源石液 | Cultured Originium | Heated mix of filtered molten + 培养液 |
| `purest_molten_originium` | 至纯熔融源石 | Purest Molten Originium | **Late intermediate / accident state** — not the clean-route output. Superheating cultured originium (or remelting the purest item) produces this fluid. Recover with a cooling chamber + blue ice. M3 reactor fuel/accident dumps use this; **meltdown does not explode**. |
| `originium_catalyst` | 培养液 | Originium Catalyst | **Never rename.** Display name is 培养液 |
| `originium_coolant` | 源石冷却液 | Originium Coolant | Reactor heat-capacity fluid (C). Mix water + 培养液 + packed ice |
| `hot_water` | 热水 | Hot Water | Unstable conversion / meltdown heat dump. Not an originium fluid |

Client fluid assets (required for each id in `COIFluids.ALL`):

- Still / flowing textures: `textures/fluid/<id>_still.png` and
  `textures/fluid/<id>_flow.png`
- LiquidBlock blockstates: `blockstates/<id>.json` with `level=0..15` (Registrate
  `defaultBlock()` registers the block)
- Particle models: `models/block/<id>.json`
- Atlas: `assets/minecraft/atlases/blocks.json` lists every still/flow sprite

Without those files, world leaks (including meltdown `purest_molten_originium`)
and placed fluid blocks show the missing-model texture.

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
- `reactor` *(additive; power-core knobs. Frozen keys keep names; coolant/chamber/RPM keys added beside them. Additive `meltdownMoltenSources` caps the world leak of `purest_molten_originium`. Meltdown never explodes.)*
- `multiplayer` *(additive; dedicated-server spread policy + nearby client dust sync)*
- `purest_line` *(additive; cooling-chamber durability)*
- `alloy_parts` *(additive; housing seal, alloy sieve, sealed canister bonus)*
- `debug`

Do not rename these sections once a release has shipped. New sections are fine.

Client spec is `COIConfig.CLIENT_SPEC` (`ModConfig.Type.CLIENT`). Top-level key:

- `client` — accessibility: `reduceFlicker`, `simplifyParticles`, `particleDensity`, `highContrastIndicators`, `uiDetailLevel`, `debugOverlayDetail`, `showSicknessHud`; additive SFX: `enableIndustrialSounds`, `soundDensity`

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
| `crafting/originium_alloy_casing` | `minecraft:crafting_shapeless` | andesite casing + alloy ingot |
| `item_application/originium_alloy_casing` | `create:item_application` | same casing (deployer / JEI) |
| `crafting/originium_alloy_sieve` | `minecraft:crafting_shapeless` | dust sieve + alloy ingot |
| `item_application/originium_alloy_sieve` | `create:item_application` | same sieve upgrade |
| `crafting/originium_sealed_canister` | `minecraft:crafting_shaped` | filter canister + alloy casing + alloy ingot |
| `crafting/originium_core_housing` | `minecraft:crafting_shaped` | 4 alloy casings + cooling chamber |
| `crafting/originium_power_core` | `minecraft:crafting_shaped` | 3 core housing + purest originium + shaft |
| `crafting/originium_super_cooling_chamber` | `minecraft:crafting_shaped` | cooling chamber + snow blocks + pumpkin + packed ice |
| `crafting/originium_cooling_chamber` | `minecraft:crafting_shaped` | basin cooling chamber (alloy + blue ice + copper casing) |
| `mixing/filtered_molten_originium` | `create:mixing` + basin sieve | filtered molten from molten |
| `mixing/cultured_originium` | `create:mixing` + heated | cultured originium from filtered + 培养液 |
| `mixing/purest_originium_supercooling` | `create:mixing` + cooling chamber, **no blaze heat**, packed ice | purest originium item |
| `mixing/purest_molten_accident` | `create:mixing` + superheated | accident: cultured → purest molten |
| `mixing/purest_originium_melting` | `create:mixing` + superheated | late remelt: purest item → purest molten |
| `mixing/purest_molten_supercooling` | `create:mixing` + cooling chamber, **no blaze heat**, blue ice | recover purest item from accident fluid |
| `mixing/originium_coolant` | `create:mixing` | water + 培养液 + packed ice → originium coolant |
| `mixing/originium_dust_recycling` | `create:mixing` + heated | 4 captured dust → 1 shard (recovery, not a 1:1 compact) |

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
| `mixing/filtered_molten_originium` | none (sieve-gated) | 80 |
| `mixing/cultured_originium` | heated | 40 |
| `mixing/purest_originium_supercooling` | none (chamber, reject heat) | 30 |
| `mixing/purest_molten_accident` | superheated | 250 |
| `mixing/purest_originium_melting` | superheated | 200 |
| `mixing/purest_molten_supercooling` | none (chamber, reject heat) | 80 |
| `mixing/originium_dust_recycling` | heated | 80 (datapack only; no `dust_production.*` override) |

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
drops `originium_dust` back into the factory loop. Filters and process sieves
do the same. Recycle that item with `mixing/originium_dust_recycling` (heated
mix, 4 dust → 1 shard). Emission is datapack `80` only — not a
`dust_production.*` override — and stays below `FILTER_BYPRODUCT_DUST_PER_ITEM`
(100) so one recycle cannot mint a dust item even at full capture.

`originium_sealed_canister` is an alloy chest upgrade (same slot). It stays in
`originium_protection` and is also tagged `originium_reinforced_protection`, which
applies `alloy_parts.sealedProtectionBonus` (default 0.10) to whatever gain the
piece-count formula left. Iron canister behaviour is unchanged.

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

### Purest line (basin attachments)

Clean route: **basin sieve** filters molten originium → heated mix with 培养液
(`originium_catalyst`, never renamed) → **cooling chamber** + packed ice and
**no blaze heat** → `purest_originium`. Dust still emits on every step
(sieve converts captured emission to `originium_dust`).

`purest_molten_originium` is **not** that output. Superheating the cultured
fluid (or remelting the purest item) yields the accident / late intermediate
fluid. Recover it with a cooling chamber + blue ice. M3 meltdown dumps
this fluid and chunk dust — never explode blocks or spawn TNT
(`MeltdownPolicy.explodesBlocks()` is false). The power core follows the
same contract: meltdown dumps `meltdownDustBurst` into the chunk,
`MeltdownPolicy.leakMolten` places up to `meltdownMoltenSources` world
source blocks of `purest_molten_originium` (remaining `purest_originium`
remelts into that spray), and leftover coolant / water become hot water.

### Power core (M3)

`originium_power_core` is a Create kinetic generator. Adjacent
`#create_originium_industry:reactor_housing` is required to run. Insert
`purest_originium` by right-click (no GUI). Internal tanks hold coolant /
water / hot water; pipes and buckets work through a fluid capability.

Stability: **S = C × M − H**.

- **H** = `coreHeatValue` × (`purestHeatCapacity` when fueled)
- **C** = Σ tank mB × per-mB heat capacity (`coolantHeatPerMb` / `waterHeatPerMb` / `hotWaterHeatPerMb`)
- **M** = `coolingMultiplier` × attached chamber cooling (`normalChamberCooling` / `superChamberCooling`)
- **S > 0** stable: fluids convert toward coolant
- **S ≈ 0** (`|S| ≤ stabilityEpsilon`) borderline
- **S < 0** unstable: fluids convert toward hot water; dust leaks into the chunk
- Instability accumulates via `instabilityGainPerTick` / `instabilityDecayPerTick`. At `meltdownThreshold` (if `enableReactorMeltdown`) the core shuts down, dumps `meltdownDustBurst` dust, leaks remaining fuel as world `purest_molten_originium` (`meltdownMoltenSources` cap), and converts leftover tanks to hot water. Never explodes.

The basin `originium_cooling_chamber` also attaches to the core (normal
tier). `originium_super_cooling_chamber` is the snow-golem super tier.

Create mixing JSON cannot express the sieve / chamber / no-heat gates. Extra
matching lives at `data/<namespace>/coi_basin_process/*.json`:

```json
{ "recipe": "create_originium_industry:mixing/filtered_molten_originium", "require_sieve": true }
{ "recipe": "create_originium_industry:mixing/purest_originium_supercooling", "require_cooling_chamber": true, "reject_heat": true }
```

`reject_heat` fails when the block under the Basin is a fading/kindled/seething
blaze burner. Unlock timing: superheat (melt) + alloy ingot + blue ice for the
chamber craft; packed ice per supercool.

### Alloy housing

`originium_alloy_ingot` crafts into factory parts, not a dead-end ingot:

- **Casing** — andesite casing + alloy (crafting table or Create item application).
  Adjacent faces cut process emission by 10% each (cap 50%).
- **Alloy sieve** — upgrade of `originium_dust_sieve`. 70% process capture / 75%
  kinetic-filter capture; 1500 durability. Same attachment rules; insertable
  into the existing filter (`SieveKind` is additive NBT).
- **Sealed canister** — alloy chest protection (see protection tags above).
- **Core housing** — 4 casings + cooling chamber. 20% seal per face. Tagged
  `reactor_housing` for M3.

Housing runs in `DustSubmission` on `MACHINE_PROCESSING` **before** purifiers.

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
| `c:fluid/originium_coolant` | `originium_coolant` | Additive |
| `c:fluid/hot_water` | `hot_water` | Additive |
| `c:fluid/filtered_molten_originium` | `filtered_molten_originium` | Additive |
| `c:fluid/cultured_originium` | `cultured_originium` | Additive |

### Declared in `COITags` (`create_originium_industry` namespace)

| Tag | Purpose | JSON |
|---|---|---|
| `item/originium_materials` | raw, shard, originium, dust, purest | present |
| `item/dust_producing` | items whose processing emits chunk dust | present (raw, shard, originium, dust, purest) |
| `item/originium_protection` | protection gear that reduces exposure/infection | present (`originium_respirator`, `originium_filter_canister`, `originium_sealed_canister`) |
| `item/originium_reinforced_protection` | alloy-grade extra protection bonus | present (`originium_sealed_canister`) |
| `block/dust_sources` | blocks that emit dust | present (empty; future COI machines) |
| `block/dust_filters` | blocks that remove/modify dust | present (`originium_dust_filter`, `originium_dust_sieve`, `originium_alloy_sieve`, `originium_dust_nozzle`) |
| `block/pollution_resistant` | adjacent housing that seals process emission | present (`originium_alloy_casing`, `originium_core_housing`) |
| `block/reactor_housing` | M3 shell contract | present (same as pollution_resistant) |
| `block/reactor_cooling` | Power-core cooling attachments | present (cooling chamber + super chamber) |
| `block/raw_originium_ores` | stone + deepslate raw originium ore | present |
| `fluid/originium_fluids` | all originium fluids | present (includes filtered + cultured + coolant; not hot water) |

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
| `advancements` | `advancements.create_originium_industry.obtain_raw_originium.title` |
| `ponder` | `create_originium_industry.ponder.<sceneId>.header` / `.text_N` and `.ponder.tag.<id>` |
| `subtitles` | `subtitles.create_originium_industry.filter_work` |
| `jei` | `jei.create_originium_industry.basin_process` / `.dust_emission` / `.reactor` / `.info.*` |

Ponder copy is registered through `Component.translatable`. Keep `en_us` and `zh_cn` in lockstep; do not rename scene ids once shipped.

`en_us` and `zh_cn` must stay in lockstep.

### Known missing keys (do not delete; still referenced in Java)

None at the moment. Filter / goggle / HUD / debug tooltip strings live in
`en_us.json` and `zh_cn.json`. If you add a `Component.translatable` call,
add both language keys in the same change.

## Additive ids (do not rename once shipped)

New ids are fine. Do not reuse a frozen id for a different object.

### Optional compat

| Mod | `mods.toml` | Behaviour |
|---|---|---|
| JEI (`jei`, 19.8+) | `optional`, client | Loads `compat.jei.COIJeiPlugin` when present. Categories: basin process gates, dust emission, reactor info, plus ingredient info pages. `compileOnly` API; the published jar does **not** package JEI. |
| Create: Aeronautics / Sable (`aeronautics`, `sable`) | `optional`, both | `compat.WorldSpace` remaps plot-grid positions to the sublevel `logicalPose()` so dust is keyed by logical overworld chunks and does not travel with the ship. Identity mapping if Sable is absent or the bridge fails. Neither Sable nor Sable Companion is packaged in this jar. |

There is **no EMI plugin**. `compat.recipeviewer` is a JEI-free data layer (GameTests + JEI format against the same records).

JEI category and info keys (additive):

| Key | Role |
|---|---|
| `jei.create_originium_industry.basin_process` | Basin sieve / cooling-chamber / no-heat gates |
| `jei.create_originium_industry.dust_emission` | `coi_dust_emission` recipe / item / tag amounts |
| `jei.create_originium_industry.reactor` | Power-core fuel, coolant conversion, housing |
| `jei.create_originium_industry.info.*` | JEI ingredient info pages (`power_core`, `purest_originium`, `originium_coolant`, `dust_sieve`, `originium_dust`) |

Ponder scene ids (additive; do not rename once shipped):

| Scene id | Schematic | Attached to |
|---|---|---|
| `dust_generation` | `ponder/dust_generation.nbt` | dust meter, raw originium, originium dust, millstone |
| `dust_filter` | `ponder/dust_filter.nbt` | kinetic filter, sieves, nozzle |
| `purest_supercooling` | `ponder/purest_supercooling.nbt` | cooling chamber, process sieve, purest originium |
| `power_core` | `ponder/power_core.nbt` | power core, super chamber, housing, cooling chamber |

Tag id: `originium_industry`.

Schematics are gzip vanilla structure NBTs at
`assets/create_originium_industry/ponder/<id>.nbt`. Regenerate with
`python3 scripts/gen_ponder_schematics.py`. Compound list elements must be
anonymous (no per-element `0x0A 0x00 0x00` type+name prefix) or Ponder's
`NbtIo.read` throws `EOFException` when holding W on a scene item.

GameTests parse each schematic the same way `PonderSceneRegistry.loadSchematic`
does. They cannot open client `PonderUI`; after a schematic change, hold W on a
COI item in creative (e.g. dust meter or power core) and confirm the scene
opens without a client crash.

### Advancements (additive)

Datapack path is the advancement id (`create_originium_industry:<path>`).

| Advancement id | Trigger | Notes |
|---|---|---|
| `root` | `minecraft:tick` | Tab root |
| `obtain_raw_originium` | `minecraft:inventory_changed` | Raw item **or** either ore block |
| `dust_exposure` | `create_originium_industry:dust_exposure` | First dust-fog exposure gain |
| `dust_purified` | `create_originium_industry:dust_purified` | Kinetic filter ambient absorb or nearby sieve/filter capture |
| `start_power_core` | `create_originium_industry:power_core_started` | Power core begins generating |
| `obtain_alloy` | `minecraft:inventory_changed` | First `originium_alloy_ingot` |
| `obtain_protection` | `minecraft:inventory_changed` | Any item tagged `originium_protection` |
| `infection_stage` | `create_originium_industry:infection_stage` | First symptomatic infection stage (weakness+) |
| `power_core_meltdown` | `create_originium_industry:power_core_meltdown` | Nearby players when a core actually melts down |

Criterion trigger ids (`dust_exposure`, `dust_purified`, `power_core_started`, `infection_stage`, `power_core_meltdown`) are additive registry ids. Do not rename them.

### Sound events (additive)

`sounds.json` keys match registry paths. Placeholder OGGs are industrial (low metal / steam / grit), not magic.

| Sound id | Role | Subtitle key |
|---|---|---|
| `filter_work` | Kinetic filter loop + process-sieve capture one-shot | `subtitles.create_originium_industry.filter_work` |
| `high_dust` | Client ambience at HIGH / CRITICAL chunk dust | `subtitles.create_originium_industry.high_dust` |
| `reactor_steady` | Power core generating with S≥0 and instability below warning | `subtitles.create_originium_industry.reactor_steady` |
| `reactor_alarm` | Power core generating while S<0 or instability ≥ warning | `subtitles.create_originium_industry.reactor_alarm` |

High-dust volume follows `simplifyParticles` / `particleDensity`. `reduceFlicker` holds alarm volume steady instead of pulsing. `enableIndustrialSounds` / `soundDensity` mute or scale the set.

## Debug surface

- Command: `/coi_debug` (OP 2, gated by `debug.enableDebugCommands`)
- Item: `originium_debug_wand`
- Reactor subcommands read a targeted power core (`reactor status`, `reactor stabilize`).
  Command names stay frozen.
- Perf subcommands (`perf`, `perf run`, `perf seed`, `perf clear`) are additive.
  They print `PerfProbe` timings and can seed a 10×10 dusty grid. See [PERF.md](PERF.md).
