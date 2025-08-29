// net/ardcameg/thesevenofapexes/abilities/uncommon/HealingLinensAbility.java

package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.ardcameg.thesevenofapexes.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class HealingLinensAbility {
    private HealingLinensAbility() {}

    /**
     * "あったかふとん"の効果を適用する (新仕様)
     * @param player プレイヤー
     * @param linensCount "あったかふとん"の所持数
     */
    public static void apply(Player player, int linensCount) {
        if (linensCount <= 0) return;

        // 体力をチェック
        float healingHealthLine = Config.healingLinensHealThreshold.get().floatValue();
        if (player.getHealth() < healingHealthLine) {
            float regenerateHealth = Math.min(healingHealthLine, player.getMaxHealth());
            player.setHealth(regenerateHealth);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }
    }
}