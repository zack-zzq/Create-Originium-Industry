package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Utility for emitting originium dust from Create machine processing.
 * Maps recipe IDs to configured dust amounts and calls {@link OriginiumDustManager}.
 */
public final class DustProductionHelper {

    private static final String MOD_ID = CreateOriginiumIndustry.MODID;

    private DustProductionHelper() {}

    /**
     * Resolves the dust production amount for a given recipe ID.
     * Returns 0 if the recipe is not an originium dust-producing recipe.
     */
    public static int getDustForRecipe(ResourceLocation recipeId) {
        if (recipeId == null) return 0;
        if (!recipeId.getNamespace().equals(MOD_ID)) return 0;

        return switch (recipeId.getPath()) {
            case "milling/raw_originium_milling" -> COIConfig.DUST_FROM_MILLING.get();
            case "crushing/raw_originium_crushing" -> COIConfig.DUST_FROM_CRUSHING.get();
            case "mixing/originium_shard_mixing" -> COIConfig.DUST_FROM_SHARD_MIXING.get();
            case "mixing/originium_mixing" -> COIConfig.DUST_FROM_ORIGINIUM_MELTING.get();
            case "mixing/molten_originium_iron_ingot_mixing" -> COIConfig.DUST_FROM_ALLOY_MIXING.get();
            default -> 0;
        };
    }

    /**
     * Emits dust at a block position if the recipe produces originium dust.
     * Called by Mixins after a Create machine completes a recipe.
     *
     * @param level    the server level
     * @param pos      the machine's block position
     * @param recipeId the completed recipe's ID
     */
    public static void emitDustFromRecipe(ServerLevel level, BlockPos pos, ResourceLocation recipeId) {
        if (!COIConfig.ENABLE_DUST_PRODUCTION.get()) return;

        int dustAmount = getDustForRecipe(recipeId);
        if (dustAmount <= 0) return;

        ChunkPos chunkPos = new ChunkPos(pos);
        OriginiumDustManager.addDust(level, chunkPos, dustAmount, DustReason.MACHINE_PROCESSING);

        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] Machine at [{}, {}, {}] produced {} dust (recipe: {})",
                    pos.getX(), pos.getY(), pos.getZ(), dustAmount, recipeId
            );
        }
    }
}
