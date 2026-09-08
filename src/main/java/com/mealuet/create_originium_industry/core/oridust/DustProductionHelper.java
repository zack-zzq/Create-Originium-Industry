package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * Recipe/item-aware facade over {@link IOridustProducer}.
 * <p>
 * Amounts resolve as:
 * <ol>
 *   <li>Frozen recipe ids — {@link COIConfig} {@code dust_production.*} overrides
 *       (existing server.toml keys keep working).</li>
 *   <li>Datapack {@code coi_dust_emission} recipe entry.</li>
 *   <li>Otherwise 0.</li>
 * </ol>
 * Item processing (future machines without a mapped recipe) uses datapack
 * item / item_tag entries, then {@link COIConfig#DUST_FROM_TAGGED_ITEM} for
 * {@code create_originium_industry:dust_producing}.
 */
public final class DustProductionHelper {

    private static final String MOD_ID = CreateOriginiumIndustry.MODID;

    private DustProductionHelper() {}

    /**
     * Resolves the dust production amount for a given recipe ID.
     * Returns 0 if the recipe is not an originium dust-producing recipe.
     */
    public static int getDustForRecipe(ResourceLocation recipeId) {
        if (recipeId == null) {
            return 0;
        }

        int config = configOverride(recipeId);
        if (config >= 0) {
            return config;
        }

        int data = DustEmissionIndex.getRecipeAmount(recipeId);
        return Math.max(0, data);
    }

    /**
     * Dust emitted when processing {@code stack} without a mapped recipe id.
     */
    public static int getDustForItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int data = DustEmissionIndex.getItemAmount(stack);
        if (data >= 0) {
            return data;
        }
        if (DustEmissionIndex.isDustProducing(stack)) {
            return COIConfig.DUST_FROM_TAGGED_ITEM.get();
        }
        return 0;
    }

    /**
     * Config override for frozen recipe ids documented in {@code docs/REGISTRY.md}.
     *
     * @return amount, or {@code -1} if this id is not a frozen production recipe
     */
    public static int configOverride(ResourceLocation recipeId) {
        if (recipeId == null || !MOD_ID.equals(recipeId.getNamespace())) {
            return -1;
        }
        return switch (recipeId.getPath()) {
            case "milling/raw_originium_milling" -> COIConfig.DUST_FROM_MILLING.get();
            case "crushing/raw_originium_crushing" -> COIConfig.DUST_FROM_CRUSHING.get();
            case "mixing/originium_shard_mixing" -> COIConfig.DUST_FROM_SHARD_MIXING.get();
            case "mixing/originium_mixing" -> COIConfig.DUST_FROM_ORIGINIUM_MELTING.get();
            case "mixing/molten_originium_iron_ingot_mixing" -> COIConfig.DUST_FROM_ALLOY_MIXING.get();
            default -> -1;
        };
    }

    /**
     * Emits dust at a block position if the recipe produces originium dust.
     * Called by Mixins after a Create machine completes a recipe.
     * Goes through {@link DustSubmission} ({@link IOridustProducer}).
     *
     * @param level    the server level
     * @param pos      the machine's block position
     * @param recipeId the completed recipe's ID
     */
    public static void emitDustFromRecipe(ServerLevel level, BlockPos pos, ResourceLocation recipeId) {
        int dustAmount = getDustForRecipe(recipeId);
        if (dustAmount <= 0) {
            return;
        }

        int deposited = DustSubmission.submit(level, pos, dustAmount, DustReason.MACHINE_PROCESSING);

        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] Machine at [{}, {}, {}] recipe {} expected {} deposited {}",
                    pos.getX(), pos.getY(), pos.getZ(), recipeId, dustAmount, deposited
            );
        }
    }

    /**
     * Emits dust for an item being processed when no recipe mapping applies.
     */
    public static void emitDustFromItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        int dustAmount = getDustForItem(stack);
        if (dustAmount <= 0) {
            return;
        }
        DustSubmission.submit(level, pos, dustAmount, DustReason.MACHINE_PROCESSING);
    }
}
