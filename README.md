<p align="center"><img src="./src/main/resources/logo.png" alt="Create: Originium Industry" width="200"></p>
<h1 align="center">Create: Originium Industry</h1>
<p align="center">机械动力：源石工业</p>

<div align="center">
    <a href="https://github.com/zack-zzq/Create-Originium-Industry/actions/workflows/build.yml"><img src="https://github.com/zack-zzq/Create-Originium-Industry/actions/workflows/build.yml/badge.svg" alt="Build"></a>
    <a href="https://github.com/zack-zzq/Create-Originium-Industry/releases"><img src="https://img.shields.io/github/v/tag/zack-zzq/Create-Originium-Industry?sort=date&label=Version" alt="Version"></a>
    <a href="LICENSE"><img src="https://img.shields.io/github/license/zack-zzq/Create-Originium-Industry" alt="License"></a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/create-originium-industry"><img src="http://cf.way2muchnoise.eu/1247319.svg" alt="Curseforge"></a>
    <a href="https://modrinth.com/mod/create-originium-industry"><img src="https://img.shields.io/modrinth/dt/create-originium-industry?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
</div>

A [Create](https://github.com/Creators-of-Create/Create) addon about a new industrial ore — Originium. Gameplay is **inspired by** Arknights; assets, names, and systems in this repository are original.

v1 is a closed loop, not a pile of extra materials: **dust pollution → purification → reactor**.

⚠ Still **WIP**. Survival processing includes the purest-originium line; the reactor Alpha (power core + cooling + stability) is in.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.x**
- Create **6.0.4+**

## v1 pillar

Players mine and process Originium with Create machines. Processing pollutes the surrounding chunks with originium dust. Dust spreads, makes players sick, and can become lasting infection. Filters and (later) protection make factories livable. A cleaner, more expensive **purest** line feeds a late-game **reactor** that is powerful and unstable.

Two industrial routes:

1. **Alloy route** — molten originium + iron → originium alloy ingot (safer structural material).
2. **Purest route** — basin sieve filters molten originium, heated mix with culture solution (培养液) produces cultured originium, then a cooling-chamber attachment + packed ice (no blaze heat) supercools it to purest originium. Superheating the culture by accident (or remelting the item) yields `purest_molten_originium` — a late intermediate / accident fluid, not the clean-route output. Meltdown will dump dust and that fluid; it does not explode.

## What's in this version (`0.0.8-dev`)

### Items

| Id | English | 中文 |
|---|---|---|
| `raw_originium` | Raw Originium | 粗制源石 |
| `originium_shard` | Originium Shard | 源石碎片 |
| `originium` | Originium | 源石 |
| `originium_dust` | Originium Dust | 源石尘 |
| `originium_alloy_ingot` | Originium Alloy Ingot | 源石合金锭 |
| `purest_originium` | Purest Originium | 至纯源石 |
| `originium_dust_sieve` | Originium Dust Sieve | 源石尘滤网 (Basin/process attachment; also kinetic-filter insert) |
| `originium_dust_nozzle` | Originium Dust Nozzle | 源石尘分散滤网 (Encased Fan attachment) |
| `originium_cooling_chamber` | Originium Cooling Chamber | 源石冷却室 (Basin supercooling + reactor cooling attachment) |
| `originium_super_cooling_chamber` | Originium Super Cooling Chamber | 源石超级冷却室 (snow-golem super cooling attachment) |
| `originium_respirator` | Originium Respirator | 源石防护面罩 (head slot) |
| `originium_filter_canister` | Originium Filter Canister | 源石滤毒罐 (chest slot) |
| `originium_sealed_canister` | Originium Sealed Canister | 源石密封滤毒罐 (alloy chest upgrade) |
| `originium_debug_wand` | Originium Debug Wand | 源石调试器 (creative / OP) |

### Blocks

| Id | English | 中文 |
|---|---|---|
| `originium_dust_filter` | Originium Dust Filter | 源石尘滤网机 |
| `originium_dust_sieve` | Originium Dust Sieve | 源石尘滤网 |
| `originium_dust_nozzle` | Originium Dust Nozzle | 源石尘分散滤网 |
| `originium_dust_meter` | Originium Dust Meter | 源石尘计 |
| `originium_cooling_chamber` | Originium Cooling Chamber | 源石冷却室 |
| `originium_super_cooling_chamber` | Originium Super Cooling Chamber | 源石超级冷却室 |
| `originium_power_core` | Originium Power Core | 源石动力核心 |
| `originium_alloy_casing` | Originium Alloy Casing | 源石合金壳体 |
| `originium_alloy_sieve` | Originium Alloy Sieve | 源石合金滤网 |
| `originium_core_housing` | Originium Core Housing | 源石核心外壳 |
| `raw_originium_ore` | Raw Originium Ore | 粗制源石矿 |
| `deepslate_raw_originium_ore` | Deepslate Raw Originium Ore | 深板岩粗制源石矿 |

### Fluids

| Id | English | 中文 |
|---|---|---|
| `molten_originium` | Molten Originium | 熔融源石 |
| `filtered_molten_originium` | Filtered Molten Originium | 过滤熔融源石 |
| `cultured_originium` | Cultured Originium | 培养源石液 |
| `purest_molten_originium` | Purest Molten Originium | 至纯熔融源石 (accident / late remelt, not the clean output) |
| `originium_catalyst` | Originium Catalyst | **培养液** (id stays `originium_catalyst`) |
| `originium_coolant` | Originium Coolant | 源石冷却液 (reactor C; mix water + 培养液 + packed ice) |
| `hot_water` | Hot Water | 热水 (unstable conversion / meltdown heat dump) |

### Processing (Create)

```
raw originium (mined from uncommon Overworld ore, iron pickaxe)
  ├─ milling / crushing → originium shards
  └─ (heated mix 4 shards) → originium
        └─ superheated mix → molten originium
              ├─ mix with iron → originium alloy ingot
              └─ basin sieve mix → filtered molten originium
                    └─ heated mix + 培养液 → cultured originium
                          ├─ cooling chamber + packed ice (no blaze heat) → purest originium
                          └─ superheated mix (accident) → purest molten originium
                                └─ cooling chamber + blue ice (no blaze heat) → purest originium

purest originium (superheated mix) → purest molten originium (late reactor intermediate)

redstone + sugar + lapis + water (heated mix) → originium catalyst (培养液)

originium alloy + blue ice + copper casing → originium cooling chamber
andesite casing + originium alloy → originium alloy casing
originium dust sieve + originium alloy → originium alloy sieve
filter canister + alloy casing + alloy ingot → originium sealed canister
4 alloy casings + cooling chamber → originium core housing
3 core housing + purest originium + shaft → originium power core
cooling chamber + snow blocks + pumpkin + packed ice → originium super cooling chamber
water + 培养液 + packed ice → originium coolant
```

### Systems (in progress)

- Per-chunk originium dust, diffusion, and decay (overworld)
- Uncommon Overworld raw originium ore (`raw_originium_ore` / `deepslate_raw_originium_ore`, config `worldgen.*`)
- Create mill / crush / mix hooks that emit dust via `IOridustProducer` (datapack `coi_dust_emission` JSON; config overrides frozen recipe ids). Heated shard mix and superheated melt emit more than cold milling; `catalyst_mixing` is heated but ships amount 0 (no originium feedstock). No furnace/fan recipes.
- Player exposure, Originium Exposure Sickness, and a 4-stage infection course (weakness → restricted → growth → bargain)
- Protection gear (`originium_respirator` + `originium_filter_canister`) tagged `originium_protection`; wearing a full set halves exposure gain. Spent gear drops `originium_dust`. `originium_sealed_canister` is an alloy chest upgrade with extra durability and a 10% residual-gain bonus.
- Death handling is configurable: singleplayer defaults clear exposure and keep 25% infection (no death spiral)
- Kinetic dust filter implements shared `IDustPurifier` (chunk absorb, nearby emission capture, `originium_dust` byproduct with remainder buffer — no dup/void). Accepts the iron sieve or the alloy sieve upgrade.
- Basin / process sieve (`originium_dust_sieve`) attaches to a Basin, mill, mixer, or crushing controller and captures process emission as byproduct (no GUI, no RPM). Alloy sieve is the same attachment with higher capture/durability.
- Encased Fan nozzle (`originium_dust_nozzle`) redirects chunk dust downwind without voiding it
- Dust meter (`originium_dust_meter`) shows chunk concentration, risk tier, and a protection hint (goggles / right-click / comparator)
- Basin cooling chamber (`originium_cooling_chamber`) attaches to a Basin (supercooling recipes, no blaze heat) **or** a power core (stability M). Super snow-golem chamber (`originium_super_cooling_chamber`) is the higher-M tier.
- Originium power core (`originium_power_core`) is a Create kinetic generator. Adjacent `reactor_housing` is required. Right-click purest originium to fuel; buckets / pipes move coolant, water, and hot water. Stability `S = C * M - H`. `S>=0` converts toward coolant; `S<0` converts toward hot water and leaks dust. Meltdown dumps chunk dust and consumes remaining fuel — **no explosion / TNT**.
- Originium alloy casing / core housing placed beside a processing machine cut process emission (10% / 20% per face, cap 50%) before filters run. Both are tagged `reactor_housing` for the power core.
- Nearby chunk dust and local-player exposure/infection sync to clients at low frequency (dirty set / on-demand window — not the full map)
- Common + client config (`create_originium_industry-common.toml` / `-client.toml`): dust, exposure, infection stages, filter, worldgen, protection gear, alloy parts, reactor stubs, dedicated-server spread policy, accessibility
- `/coi_debug` and the debug wand for inspection
- Backbone advancements: obtain raw originium → first dust exposure → first filter/sieve capture → first power-core start

Dust and pollution are being reworked. Treat `core/oridust` as unstable; **do not rename registry ids** — see [docs/REGISTRY.md](docs/REGISTRY.md).

## Roadmap

| Phase | Name | Status |
|---|---|---|
| **M0** | Tech cleanup: freeze ids, docs, metadata, config skeleton | Done |
| **M1** | Dust loop MVP: data-driven emission, filters, dust meter, survival source | In progress (simulation refactor) |
| **M2** | Purest / alloy expansion: filter + 培养液 + supercooling | Survival path in (`purest_originium`); alloy housing / sieve / sealed canister in |
| **M3** | Reactor: heat, cooling, instability / meltdown | Alpha in (power core + chambers + S; meltdown = dust, not explosion) |
| **M4** | Ponder, GameTests, optional compat (e.g. Create: Aeronautics) | Advancement backbone in; Ponder still open |

M1 must be playable before M3. The reactor depends on dust APIs and the purest fuel chain.

## Documentation for contributors

- [Registry, lang keys, and save compatibility](docs/REGISTRY.md) — what must not be renamed
- Issues: [github.com/zack-zzq/Create-Originium-Industry/issues](https://github.com/zack-zzq/Create-Originium-Industry/issues)

## Inspiration and copyright

This project is **inspired by** Arknights (Hypergryph / Gryphline) and is **not** affiliated with or endorsed by them.

We do **not** distribute:

- Official character art, logos, or UI
- Long passages of official story text
- Official audio, models, or other copyrighted assets

Item names such as Originium are used as original gameplay terms in a Create industrial setting. Code and original textures in this repository are MIT; that license does **not** grant rights to third-party IP.

If you contribute art or text, keep it original.

## License

[MIT](LICENSE). Authors: Zack Zhu, Mimei.
