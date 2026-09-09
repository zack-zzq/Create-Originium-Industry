# Dust / reactor performance baseline

Issue [#24](https://github.com/zack-zzq/Create-Originium-Industry/issues/24). Design-draft budget: about **100 related machines** should add **&lt; 2 ms/tick** of server overhead.

This file is the repeatable measurement method. Numbers below are from
`./gradlew build` → `runGameTestServer` on the machine that last updated
the table. Re-run the GameTests (or F3+L / Spark) before treating a
regression as real.

Audio, Ponder, and particles are out of scope here.

## Stated load

One Overworld player plus a compact factory:

| Piece | Count / shape | When it runs (defaults) |
|---|---|---|
| Dust active set | `initChunkRadius` 8 → 17×17 = **289** logical chunks | every `diffusionInterval` **20** ticks |
| Emitting machines | **100** recently written chunks in a **10×10** grid inside that radius, checkerboard 4000 / 800 dust | seed, then the same diffusion tick |
| Kinetic filters | **100** SavedData absorbs (`filterAbsorptionRate` each) + one live `absorbAmbient` | every `filterAbsorptionInterval` **20** ticks |
| Power cores | **100** `tickReactor` steps on a housed, cooled, fueled core | **every tick** |
| Client dust sync | one dirty-set / window flush | every `dustSyncInterval` **20** ticks |

`PerfLoad` (`core/perf/PerfLoad.java`) and `/coi_debug perf seed` build the 10×10 grid. GameTests use the GameTest structure chunk as the center so the grid sits inside the player radius.

**Aligned extra** (worst tick) = dust cycle + 100 reactor steps + 100 filter writes + sync flush, all on the same tick. That is the number compared to 2 ms.

**Amortized extra** ≈ reactors + (dust + filters + sync) / 20, because only the power core ticks every tick at default intervals.

Caveats:

- GameTests time **COI extra** (SavedData diffusion/decay, reactor simulation, filter writes, sync planner). They do not place 100 physical Create networks, so Spark/F3+L on a survival factory can be higher.
- 100 reactor steps run on **one** placed core (same six neighbors, 100× `tickReactor`). That matches the CPU of the stability/fluid/fuel loop; it under-counts 100 distinct block-entity entries in the world's ticker.
- Filter GameTests write 100 chunks through `OriginiumDustManager` and only call `absorbAmbient` once, so they do not spawn 100 byproduct item entities.

## How to measure

### 1. GameTest timing (CI / this repo)

```bash
./gradlew runGameTestServer
```

Look for log lines:

```
[COI perf] dust_cycle ...
[COI perf] reactor_x100 ...
[COI perf] filter_x100 ...
[COI perf] aligned_extra ... under_2ms=...
```

Tests live in `PerfBaselineGameTests` (batch `perf`). They fail only if a sample median exceeds **50 ms** (`PerfProbe.CI_GUARD_NANOS`) — a catastrophe guard, not the 2 ms target. Shared CI VMs are too noisy to fail the build at 2 ms.

To refresh the table below: run GameTests, copy the median lines, and edit this file in the same change.

### 2. In-game sampler (`/coi_debug perf`)

Requires OP 2 and `debug.enableDebugCommands` (default on).

| Command | What it does |
|---|---|
| `/coi_debug perf` | Prints last dust-cycle cost, active-set size, this-tick reactor/filter sums, last sync flush, aligned extra |
| `/coi_debug perf seed` | Writes the 10×10 dusty grid around the player (same as GameTests) |
| `/coi_debug perf run` | Forces one `Oridust.runActiveSetCycle` + client sync flush, then prints |
| `/coi_debug perf clear` | Clears that seeded grid |

Lightweight hooks (`PerfProbe`) wrap the existing tick paths. They are always-on `System.nanoTime()` around work that already ran; they are not a profiler.

Suggested in-game sequence:

1. Stand in the factory (or `/coi_debug perf seed` for a synthetic 100-machine dust field).
2. `/coi_debug perf run` a few times after the JIT warms (walk around, wait ~10 s).
3. Compare **aligned extra** to 2 ms. If intervals are aligned on that tick, this is the spike; otherwise use F3+L / Spark for a 20-tick average.

### 3. F3+L (vanilla pie chart)

1. Open a world with the stated load (real factory or `perf seed` + at least one running power core).
2. Press **F3+L**, wait for the pie, then **F3+L** again if needed so it writes `debug-report-*.txt` / shows the overlay (version-dependent).
3. On 1.21 the pie is **server tick** vs **specified** vs **unspecified**. Drill into:
   - `tick` → `levels` → `blockEntities` (power cores, filters, sieves, meters)
   - `tick` → `entities` / `blockEntities` if byproduct items piled up
   - NeoForge `LevelTickEvent` work (dust diffusion, exposure, dust sync)
4. Record **MSPT** from F3 (`ms ticks`) over ~20 s. Extra vs an empty superflat next to spawn is the COI delta.

F3+L includes Create kinetics, lighting, and other mods. Subtract an empty-world baseline captured the same way.

### 4. Spark

Use the [Spark](https://spark.lucko.me/) standalone or the Spark mod (not bundled here).

Server sampler (~30 s) with the stated load running:

```
/spark profiler --timeout 30
```

Or the standalone JAR against a dedicated server. Search the output for:

- `DustDiffusionEngine` (`core.oridust.internal`)
- `DustCacheManager.snapshotActive`
- `DustSyncTracker`
- `PowerCoreBlockEntity.tickReactor`
- `DustFilterBlockEntity`
- `OriginiumDustManager`

Export the spark viewer link or a screenshot into the PR that changes this baseline.

## Baseline numbers

Recorded from `./gradlew runGameTestServer` on 8 Sep 2026 (Cloud Agent VM, OpenJDK 21, NeoForge 1.21.1 GameTestServer, no Spark). Log lines: `[COI perf]`.

Active set on this runner was **160** logical chunks (10×10 machine grid + neighbor halo). The GameTest dummy player does not expand the set to the full 17×17 = 289 player disk; that is still a 100-machine factory, not an empty-radius walk.

| Lane | Load | Median | Min | vs 2 ms target |
|---|---|---|---|---|
| `dust_cycle` | 100-machine checkerboard + active set (160 chunks) | **0.260 ms** | 0.223 ms | under (~13%) |
| `reactor_x100` | 100 `tickReactor` on one housed core | **0.291 ms** | 0.260 ms | under (~15%) |
| `filter_x100` | 100 chunk absorbs + 1 live filter | **0.055 ms** | 0.052 ms | under (~3%) |
| `aligned_extra` | dust + 100 reactors + 100 filter writes + sync, same tick | **0.469 ms** | 0.429 ms | **under (~23%)** |

Last aligned trial split (probe, same tick): dust 0.163 ms, reactors 0.253 ms, live filter absorb 0.010 ms, sync 0.010 ms.

Amortized extra at default intervals ≈ reactors + (dust + filters + sync) / 20 ≈ **0.31 ms/tick**.

CI guard: every median above must stay **&lt; 50 ms**.

## Verdict and follow-ups

A second `./gradlew build` on the same VM measured `aligned_extra` at **1.081 ms** (still under 2 ms). Treat **0.5–1.1 ms** as the GameTest band on this host; do not chase sub-millisecond noise.

Keep the follow-ups below as optional work when factories grow past 100 machines, when Spark shows Create kinetic networks dominating, or when a dedicated server enables `AGGRESSIVE` spread (larger active set than this baseline).

Concrete follow-ups (cheapest first):

1. **Reactor `setChanged()` every tick** — `PowerCoreBlockEntity.tickReactorInner` marks the chunk dirty even when fuel/fluids/instability did not change. Skip `setChanged` unless those fields moved; H/C/M/S can be recomputed.
2. **Cache housing / chamber scans** — `hasHousing()` and `attachedChamberCooling()` walk six neighbors every tick. `structureDirty` already exists and `neighborChanged` already sets it; use it to cache `M` and housing until a neighbor update.
3. **Reactor `notifyUpdate` / comparator every 10 ticks while idle** — skip when shutdown and fuel is empty.
4. **Dust snapshot maps** — `snapshotActive` allocates a `HashMap<ChunkPos,Integer>` and new `ChunkPos` objects for every chunk in the radius every 20 ticks. Reuse a long→int map (`Long2IntOpenHashMap`) like SavedData already does.
5. **Shard diffusion** — process 1/N of the active set per interval tick instead of the whole set on the aligned tick (spreads the spike; amortized cost similar).
6. **Dirty-region diffusion** — only re-walk chunks that changed last cycle plus their neighbors, instead of the full player radius when most of the 289 chunks are zero.
7. **Filter byproduct drops** — converting capture into item entities is vanilla cost; batch or keep remainder in the buffer longer under load.
8. **Dedicated-server `AGGRESSIVE` radius** — `dedicatedRadiusBonus` / strategy can grow the active set past this baseline; re-measure before enabling it as the default.

## Manual checklist (non-CI)

- [ ] Superflat Overworld, cheats on, `debug.enableDebugCommands=true`
- [ ] `/coi_debug perf seed` then `/coi_debug perf run` — note aligned extra
- [ ] Place one fueled power core (housing + chamber + coolant) and repeat `perf`
- [ ] Optional: 10–20 real mill/mixer/filter machines, F3 MSPT vs empty
- [ ] Optional: Spark 30 s sampler, search the class names above
- [ ] `/coi_debug perf clear` when done

## Code map

| Piece | Where |
|---|---|
| Budget constants | `core/perf/PerfProbe.java` |
| 10×10 seed | `core/perf/PerfLoad.java` |
| Dust cycle hook | `Oridust.runActiveSetCycle` → `DustDiffusionEngine` |
| Reactor hook | `PowerCoreBlockEntity.tickReactor` |
| Filter hook | `DustFilterBlockEntity.absorbAmbient` |
| Sync hook | `Oridust.flushClientSync` → `DustSyncTracker.flush` |
| GameTests | `gametest/PerfBaselineGameTests.java` |
| Debug command | `/coi_debug perf` |
