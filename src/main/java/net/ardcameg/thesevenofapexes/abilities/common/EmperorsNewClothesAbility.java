// net/ardcameg/thesevenofapexes/abilities/common/EmperorsNewClothesAbility.java

package net.ardcameg.thesevenofapexes.abilities.common;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class EmperorsNewClothesAbility {
    private EmperorsNewClothesAbility() {}

    /**
     * "王様の新しい服"の効果を適用する
     * @param player プレイヤー
     * @param clothesCount "王様の新しい服"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void apply(Player player, int clothesCount, int prideMultiplier) {
        if (clothesCount <= 0) return;

        // --- 条件判定：プレイヤーが防具を一つも装備していないか ---
        boolean isNaked = true;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!armorStack.isEmpty()) {
                isNaked = false;
                break; // 一つでも装備していたらループを抜ける
            }
        }

        // もし裸なら、パーティクルを発生させる
        if (isNaked) {
            if (player.level() instanceof ServerLevel serverLevel) {
                // "傲慢"の効果でパーティクルの数が増える
                int particleCount = 1 + (clothesCount * prideMultiplier - 1);

                // プレイヤーの周りにきらめくパーティクルをスポーンさせる
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD, // キラキラしたパーティクル
                        player.getX(),
                        player.getY() + 1.0, // プレイヤーの中心あたり
                        player.getZ(),
                        particleCount,     // パーティクルの数
                        0.5, 0.5, 0.5, // 散開範囲 (XYZ)
                        0.02           // 速度
                );
            }
        }
    }
}