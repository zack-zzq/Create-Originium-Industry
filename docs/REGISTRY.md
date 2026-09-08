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
| `raw_originium` | 粗制源石 | Raw Originium | No worldgen yet |
| `originium_shard` | 源石碎片 | Originium Shard | |
| `originium` | 源石 | Originium | |
| `originium_dust` | 源石尘 | Originium Dust | Item form; chunk pollution is separate |
| `originium_alloy_ingot` | 源石合金锭 | Originium Alloy Ingot | Also tagged under `c:ingots` |
| `purest_originium` | 至纯源石 | Purest Originium | Item exists; no survival recipe yet |
| `originium_dust_sieve` | 源石尘滤网 | Originium Dust Sieve | Consumable for the filter block |
| `originium_dust_nozzle` | 源石尘分散滤网 | Originium Dust Nozzle | Placeholder item; not a block yet |
| `originium_debug_wand` | 源石调试器 | Originium Debug Wand | Debug-only; do not add survival recipes |
| `molten_originium_bucket` | 熔融源石桶 | Molten Originium Bucket | Generated with the fluid |
| `purest_molten_originium_bucket` | 至纯熔融源石桶 | Purest Molten Originium Bucket | Generated with the fluid |
| `originium_catalyst_bucket` | 培养液桶 | Originium Catalyst Bucket | Display = 培养液; id stays catalyst |

### Blocks and block entities

| Registry id | Notes |
|---|---|
| `originium_dust_filter` | Kinetic dust absorber (block + block entity share this path) |

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

### Creative tab

| Registry id | lang key |
|---|---|
| `main` | `itemGroup.create_originium_industry.main` |

### Attachments

| Attachment id | Holder | Current NBT keys | Version field |
|---|---|---|---|
| `chunk_oridust_data` | chunk | `DustLevel` (int) | **not present — add on next schema change** |
| `player_exposure_data` | player (`copyOnDeath`) | `Exposure`, `Infection` (ints) | **not present — add on next schema change** |

`copyOnDeath` on player exposure is intentional: infection survives respawn.

### Config file

Common config spec is `COIConfig.COMMON_SPEC`. Top-level keys:

- `dust_diffusion`
- `dust_production`
- `player_exposure`
- `feature_toggles`
- `dust_filter`
- `debug`

Do not rename these sections once a release has shipped. New sections are fine.
Client / accessibility config does not exist yet; add `COIConfig` client spec
rather than stuffing render settings into common.

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

Dust production currently keys off these paths in Java. If that mapping moves
to tags or recipe JSON, **keep these ids working** (or data-gen aliases).

## Tags

### Published (`c` namespace)

| Tag | Values | Status |
|---|---|---|
| `c:ingots/originium_alloy_ingot` | `originium_alloy_ingot` | Frozen (non-standard path, already used) |
| `c:ingots/originium_alloy` | `originium_alloy_ingot` | Conventional alias; keep both |
| `c:fluid/molten_originium` | `molten_originium` | Frozen |
| `c:fluid/purest_molten_originium` | `purest_molten_originium` | Frozen |
| `c:fluid/originium_catalyst` | `originium_catalyst` | Frozen |

### Declared in `COITags` (`create_originium_industry` namespace)

| Tag | Purpose | JSON |
|---|---|---|
| `item/originium_materials` | raw, shard, originium, dust, purest | present |
| `item/dust_producing` | items whose processing emits chunk dust | **not shipped — dust API work** |
| `block/dust_sources` | blocks that emit dust | **not shipped — dust API work** |
| `block/dust_filters` | blocks that remove/modify dust | **not shipped — dust API work** |
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
| `dust_level` | `dust_level.create_originium_industry.safe` |

`en_us` and `zh_cn` must stay in lockstep.

### Known missing keys (do not delete; still referenced in Java)

These filter / goggle strings are used by the current dust-filter block but are
absent from lang files. Fill them or stop referencing them — do not leave raw
keys in game:

- `block.create_originium_industry.originium_dust_filter.status`
- `block.create_originium_industry.originium_dust_filter.no_sieve`
- `block.create_originium_industry.originium_dust_filter.speed`
- `block.create_originium_industry.originium_dust_filter.no_power`
- `block.create_originium_industry.originium_dust_filter.goggle.sieve`
- `block.create_originium_industry.originium_dust_filter.goggle.no_sieve`
- `block.create_originium_industry.originium_dust_filter.goggle.rate`
- `block.create_originium_industry.originium_dust_filter.sieve_inserted`
- `block.create_originium_industry.originium_dust_filter.already_has_sieve`
- `block.create_originium_industry.originium_dust_filter.sieve_removed`

If the filter is redesigned, update this list in the same change.

## Allowed to add later

New ids are fine. Do not reuse a frozen id for a different object.

Expected (not frozen until registered):

- Originium ore / worldgen features
- Dust meter block
- Encased-fan nozzle block (the item id `originium_dust_nozzle` is already taken)
- Protection equipment
- Purification intermediates for the purest line
- Reactor blocks / block entities
- Ponder / JEI lang keys
- Client config spec

## Debug surface

- Command: `/coi_debug` (OP 2, gated by `debug.enableDebugCommands`)
- Item: `originium_debug_wand`
- Reactor subcommands are stubs until the reactor exists; keep the command
  names (`reactor status`, `reactor stabilize`) if possible so docs and packs
  do not churn.
