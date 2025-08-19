package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SunlightSacredSealAbility {
    private SunlightSacredSealAbility() {}

    // --- このアビリティが管理するAttribute ModifierのID ---
    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "sun_seal_attack_damage");
    private static final ResourceLocation MOVEMENT_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "sun_seal_movement_speed");
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "sun_seal_attack_speed");

    /**
     * "日輪の聖印"の継続効果を適用、または解除する
     * @param player プレイヤー
     * @param sunSealCount "日輪"の所持数
     * @param hasMoonSeal "月光"を持っているかどうか
     */
    public static void updateEffect(Player player, int sunSealCount, int prideMultiplier,boolean hasMoonSeal) {
        // --- 1. 効果が発動する条件を判定 ---
        boolean isDay = player.level().isDay();
        boolean canSeeSky = player.level().canSeeSky(player.blockPosition());
        boolean isSunActive = sunSealCount > 0 && (hasMoonSeal || (isDay && canSeeSky));

        // --- 2. 属性(Attribute)を取得 ---
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackDamage == null || movementSpeed == null || attackSpeed == null) return;

        int actualLevel = sunSealCount * prideMultiplier;

        // --- 3. 条件に応じて効果を付与または解除 ---
        if (isSunActive) {
            // --- 効果を付与 ---
            // 攻撃力 +10%
            addModifier(attackDamage, ATTACK_DAMAGE_ID, actualLevel * 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 移動速度 +25%
            addModifier(movementSpeed, MOVEMENT_SPEED_ID, actualLevel * 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 攻撃速度 +5%
            addModifier(attackSpeed, ATTACK_SPEED_ID, actualLevel * 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            // 回復速度 +100% (Regeneration II)
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false, false));

            // 周囲の植物の成長を促進 (40tickに1回)
            if (player.level().getGameTime() % 40 == 0 && player.level() instanceof ServerLevel serverLevel) {
                promotePlantGrowth(serverLevel, player.blockPosition(), sunSealCount, prideMultiplier);
            }

        } else {
            // --- 効果を解除 ---
            attackDamage.removeModifier(ATTACK_DAMAGE_ID);
            movementSpeed.removeModifier(MOVEMENT_SPEED_ID);
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
        }
    }

    // --- ヘルパーメソッド群 ---

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.removeModifier(id); // 念のため、古いものを削除してから追加する
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }

    private static void promotePlantGrowth(ServerLevel level, BlockPos center, int sunSealCount, int prideMultiplier) {
        int radius = 2 + sunSealCount;
        // 処理を軽くするため、範囲内のブロックを全てチェックするのではなく、
        // 聖印の数に応じて、ランダムな座標を数回チェックする方式に変更
        int attempts = sunSealCount * prideMultiplier * 2; // 1個なら2回、2個なら4回試行

        for (int i = 0; i < attempts; i++) {
            // 中心座標から、ランダムにずれた座標を取得
            BlockPos targetPos = center.offset(
                    level.random.nextInt(radius * 2 + 1) - radius, // x
                    level.random.nextInt(3) - 1,                      // y (-1, 0, 1)
                    level.random.nextInt(radius * 2 + 1) - radius  // z
            );

            BlockState targetState = level.getBlockState(targetPos);
            Block targetBlock = targetState.getBlock();

            // 1. 対象ブロックが作物を成長させられるか (Bonemealable)
            if (targetBlock instanceof BonemealableBlock bonemealable) {
                // 2. その下のブロックが耕地(Farmland)であるか
                BlockState groundState = level.getBlockState(targetPos.below());
                if (groundState.is(net.minecraft.world.level.block.Blocks.FARMLAND)) {
                    // 3. 骨粉を与えられる状態か (成長しきっていないか)
                    if (bonemealable.isValidBonemealTarget(level, targetPos, targetState)) {
                        // 成長させる
                        bonemealable.performBonemeal(level, level.random, targetPos.immutable(), targetState);
                        // 一度成功したら、このtickでの処理は終了 (負荷軽減)
                        return;
                    }
                }
            }
        }
    }
}