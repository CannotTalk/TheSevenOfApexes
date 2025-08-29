package net.ardcameg.thesevenofapexes.abilities.common;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.Random;

public final class RainbowShardAbility {
    private RainbowShardAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * "虹の欠片"の効果を適用する
     * @param player プレイヤー
     * @param shardCount "虹の欠片"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void apply(Player player, int shardCount, int prideMultiplier) {
        if (shardCount <= 0) return;

        // サーバーサイドでのみ実行
        if (player.level() instanceof ServerLevel serverLevel) {
            // 1秒間に約2回、パーティクルを発生させる (ランダム性を持たせる)
            if (serverLevel.random.nextInt(10) == 0) {
                // "傲慢"の効果でパーティクルの数が増える
                int particleCount = 2 + (shardCount * prideMultiplier - 1);

                // ランダムな色を生成
                float red = RANDOM.nextFloat();
                float green = RANDOM.nextFloat();
                float blue = RANDOM.nextFloat();

                // ポーションのようなパーティクルを生成
                DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(red, green, blue), 1.0F);

                serverLevel.sendParticles(
                        particleOptions,
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