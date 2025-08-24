package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SlothAbility {
    private SlothAbility() {}

    /**
     * "怠惰"の効果を適用する
     * @param player プレイヤー
     * @param slothCount "怠惰"の所持数
     * @param prideMultiplier "傲慢"の効果倍率("怠惰"は"傲慢"によってコピーされない)
     */
    public static void apply(Player player, int slothCount, int prideMultiplier) {
        if (slothCount <= 0) return;

        // 1. 体力と満腹度を回復
        // 基本の10%に、追加の1個あたり2.5%のボーナスを加える
        float healthBonus = 0.025f * (slothCount - 1);
        float totalHealthMultiplier = 0.1f + healthBonus;
        player.heal(player.getMaxHealth() * totalHealthMultiplier);

        // 満腹度は2個ごとに1ポイント(5%)追加ボーナス
        int hungerBonus = (slothCount - 1) / 2;
        player.getFoodData().eat(2 + hungerBonus, 0.6f);

        // 2. 経験値を使ってアイテムを修復
        if (player.experienceLevel > 0) {
            List<ItemStack> repairableItems = new ArrayList<>();
            for (ItemStack stack : player.getInventory().items) {
                if (stack.isDamaged()) repairableItems.add(stack);
            }
            for (ItemStack stack : player.getInventory().armor) {
                if (stack.isDamaged()) repairableItems.add(stack);
            }
            for (ItemStack stack : player.getInventory().offhand) {
                if (stack.isDamaged()) repairableItems.add(stack);
            }

            if (!repairableItems.isEmpty()) {
                ItemStack itemToRepair = repairableItems.get(player.getRandom().nextInt(repairableItems.size()));

                int repairAmount = 2 * slothCount;
                int currentDamage = itemToRepair.getDamageValue();
                int actualRepair = Math.min(repairAmount, currentDamage);
                itemToRepair.setDamageValue(itemToRepair.getDamageValue() - actualRepair);

                player.giveExperienceLevels(-1);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.5f);
                return;
            }
        }
    }

    /**
     * "怠惰"の数に応じて、発動に必要な静止時間を計算する
     * @param count "怠惰"の最終的な所持数ww
     * @return 必要な静止時間 (tick)
     */
    public static int getRequiredStandingTicks(int count) {
        if (count <= 0) {
            return 300; // 0個ならデフォルトの15秒
        }
        // 1個で15秒、1個増えるごとに2.5秒 (50 ticks) 短縮される。ただし最低でも2秒(40ticks)は必要。
        int baseTicks = 300;
        int reductionPerItem = 50;
        int minTicks = 40;

        int requiredTicks = baseTicks - (reductionPerItem * (count - 1));

        return Math.max(requiredTicks, minTicks);
    }
}