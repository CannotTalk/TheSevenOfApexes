package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GluttonyAbility {

    // レベルアップに必要な基本ゲージ量
    private static final int BASE_GAUGE_THRESHOLD = Config.gluttonyBaseGauge.getAsInt();
    // 1レベルごとに、しきい値がどれだけ増えるか
    private static final int GAUGE_THRESHOLD_INCREASE_PER_LEVEL = Config.gluttonyGaugeModifier.getAsInt();

    // --- 内部データ用の名前 ---
    private static final String GLUTTONY_GAUGE_TAG = "GluttonyGauge";
    private static final String GLUTTONY_LEVEL_TAG = "GluttonyLevel";
    private static final ResourceLocation GLUTTONY_HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "gluttony_health_bonus");

    /**
     * "暴食"の効果を適用する
     * @param player プレイヤー
     * @param gluttonyCount "暴食"の素の所持数
     * @param prideMultiplier "傲慢"によって計算された効果倍率
     */
    public static void apply(Player player, int gluttonyCount, int prideMultiplier) {
        // --- 1. 食べるブロックを取得 ---
        BlockHitResult rayTraceResult = getPlayerLookingAtBlock(player);
        if (rayTraceResult.getType() == BlockHitResult.Type.MISS) return;

        BlockPos blockPos = rayTraceResult.getBlockPos();
        BlockState blockState = player.level().getBlockState(blockPos);

        if (blockState.isAir() || blockState.getDestroySpeed(player.level(), blockPos) < 0) return;

        // --- 2. ゲージ量を計算し、ブロックを食べる ---
        int baseGaugeValue = getGaugeValueForBlock(blockState.getBlock());
        if (baseGaugeValue <= 0) return;

        int finalCount = gluttonyCount * prideMultiplier;
        int finalGaugeValue = baseGaugeValue + (int)Math.floor(baseGaugeValue * (finalCount - 1) * 0.5);

        player.level().removeBlock(blockPos, false);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0f, 1.25f);
        player.swing(player.getUsedItemHand());

        // --- 3. データを読み込み、ゲージを加算 ---
        int currentGauge = player.getPersistentData().getInt(GLUTTONY_GAUGE_TAG);
        int currentLevel = player.getPersistentData().getInt(GLUTTONY_LEVEL_TAG);

        currentGauge += finalGaugeValue;

        // --- 4. 表示 ---
        StringBuilder gaugeIncrement = new StringBuilder();
        gaugeIncrement.append(ChatFormatting.GOLD).append("+");
        gaugeIncrement.append(ChatFormatting.GOLD).append(finalGaugeValue);
        // player.sendSystemMessage(Component.literal(gaugeIncrement.toString()));
        Minecraft.getInstance().player.displayClientMessage(Component.literal(gaugeIncrement.toString()), true);

        // --- 5. レベルアップ判定 (ループで複数レベルアップに対応) ---
        while (true) {
            int requiredGauge = BASE_GAUGE_THRESHOLD + (currentLevel * GAUGE_THRESHOLD_INCREASE_PER_LEVEL);

            if (currentGauge >= requiredGauge) {
                currentGauge -= requiredGauge;
                currentLevel++;
                updatePlayerMaxHealth(player, currentLevel);
            } else {
                // ゲージが足りなくなったらループを抜ける
                break;
            }
        }

        // --- 6. データを保存 ---
        player.getPersistentData().putInt(GLUTTONY_GAUGE_TAG, currentGauge);
        player.getPersistentData().putInt(GLUTTONY_LEVEL_TAG, currentLevel);
    }

    private static BlockHitResult getPlayerLookingAtBlock(Player player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 traceEnd = eyePosition.add(lookVector.x * 5, lookVector.y * 5, lookVector.z * 5);
        return player.level().clip(new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    // Add: Some blocks to increase the gauge for “Gluttony.”
    private static int getGaugeValueForBlock(Block block) {
        if (block.equals(Blocks.DIRT) || block.equals(Blocks.GRASS_BLOCK)
                ||block.equals(Blocks.SAND) || block.equals(Blocks.GRAVEL))
            return 1;
        if (block.equals(Blocks.STONE) || block.equals(Blocks.COBBLESTONE) || block.equals(Blocks.SMOOTH_STONE))
            return 2;
        if (block.equals(Blocks.OAK_LOG) || block.equals(Blocks.SPRUCE_LOG) || block.equals(Blocks.BIRCH_LOG))
            return 3;
        if (block.equals(Blocks.OAK_WOOD) || block.equals(Blocks.SPRUCE_WOOD) || block.equals(Blocks.BIRCH_WOOD))
            return 8;
        if (block.equals(Blocks.IRON_ORE) || block.equals(Blocks.DEEPSLATE_IRON_ORE))
            return 10;
        if (block.equals(Blocks.DIAMOND_ORE) || block.equals(Blocks.DEEPSLATE_DIAMOND_ORE))
            return 20;
        if (block.equals(Blocks.REDSTONE_ORE) || block.equals(Blocks.DEEPSLATE_REDSTONE_ORE))
            return 25;
        if (block.equals(Blocks.REDSTONE_BLOCK))
            return 50;
        if (block.equals(Blocks.AMETHYST_BLOCK))
            return 100;
        if(block.equals(ModBlocks.ALTAR_OF_BANISHMENT.get()))
            return 5000;
        return 0;
    }

    private static void updatePlayerMaxHealth(Player player, int level) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        int healthIncrease = Config.gluttonyHealthModifier.getAsInt();

        healthAttribute.removeModifier(GLUTTONY_HEALTH_MODIFIER_ID);

        AttributeModifier healthModifier = new AttributeModifier(
                GLUTTONY_HEALTH_MODIFIER_ID,
                level * healthIncrease,
                AttributeModifier.Operation.ADD_VALUE
        );
        healthAttribute.addPermanentModifier(healthModifier);

        player.heal(player.getMaxHealth());
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.75f, 1.0f);
    }
}