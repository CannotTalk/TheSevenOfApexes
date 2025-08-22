package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class HealingLinensAbility {
    private HealingLinensAbility() {}

    /**
     * "あったかふとん"の効果を適用する
     * @param player プレイヤー
     * @param linensCount "あったかふとん"の所持数
     * @param prideMultiplier "傲慢"による効果倍率 (このアビリティでは効果を増幅しないため未使用)
     */
    public static void apply(Player player, int linensCount, int prideMultiplier) {
        // 所持数が0以下なら何もしない
        if (linensCount <= 0) return;

        // 体力がハート5個 (10.0f) "未満" かチェック (10.0fちょうどは対象外)
        if (player.getHealth() < 10.0f) {
            // 体力をハート5個 (10.0f) に設定する
            player.setHealth(10.0f);

            // 効果が発動したことをプレイヤーにフィードバックする
            if (player instanceof ServerPlayer serverPlayer) {
                // ポジティブな効果音を再生
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }
    }
}