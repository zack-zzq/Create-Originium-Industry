/**
 * Originium dust simulation — public API and persist contract.
 *
 * <h2>Stable surface</h2>
 * Call sites outside this package should use:
 * <ul>
 *   <li>{@link com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager}
 *       — chunk dust get / set / clear</li>
 *   <li>{@link com.mealuet.create_originium_industry.core.oridust.IOridustProducer}
 *       and {@link com.mealuet.create_originium_industry.core.oridust.DustSubmission}
 *       — machine emission</li>
 *   <li>{@link com.mealuet.create_originium_industry.core.oridust.IDustPurifier}
 *       and {@link com.mealuet.create_originium_industry.core.oridust.DustPurification}
 *       — capture / absorb</li>
 *   <li>{@link com.mealuet.create_originium_industry.core.oridust.PlayerExposure}
 *       — exposure / infection accessors</li>
 *   <li>{@link com.mealuet.create_originium_industry.core.oridust.VisibleDust}
 *       — side-aware reads for HUD, goggles, and tooltips</li>
 *   <li>{@link com.mealuet.create_originium_industry.core.oridust.Oridust}
 *       — game-bus registration and debug / GameTest hooks</li>
 * </ul>
 * Value types used with that surface ({@code DustReason}, {@code DustLevel},
 * {@code InfectionStage}, {@code PurificationResult}, {@code SieveKind},
 * {@code ByproductBuffer}) are also stable.
 *
 * <h2>Persist contract</h2>
 * {@link com.mealuet.create_originium_industry.core.oridust.OriDustSavedData},
 * {@link com.mealuet.create_originium_industry.core.oridust.OriDustData}, and
 * {@link com.mealuet.create_originium_industry.core.oridust.PlayerExposureData}
 * own NBT keys and {@link com.mealuet.create_originium_industry.core.PersistSchema}
 * versions. Do not rename keys or skip migrate steps.
 *
 * <h2>Internals</h2>
 * Tick engines, client caches, and sync planners live in
 * {@code core.oridust.internal}. They stay public so GameTests and the
 * network layer can reach them; they are not a stability contract.
 */
package com.mealuet.create_originium_industry.core.oridust;
