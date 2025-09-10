package net.ardcameg.thesevenofapexes.abilities.rare;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class HungryBanquetAbility {
    private HungryBanquetAbility() {}

    public static void convertDamageToHunger(LivingDamageEvent.Pre event, Player player, int banquetCount, int prideMultiplier) {

        // 回避不能なダメージは肩代わりできない
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        if (banquetCount <= 0) return;

        FoodData foodData = player.getFoodData();
        int currentFoodLevel = foodData.getFoodLevel();
        if (currentFoodLevel <= 0) return;

        float incomingDamage = event.getNewDamage();

        // --- 1. 満腹度で肩代わりできるダメージ量を計算 ---
        // 1満腹度ポイント = 1ダメージ (ハート半個分) に換算
        // 複数所持と傲慢で、肩代わり効率が上がる (1個で1:1, 2個で1:1.5,...)
        int finalCount = banquetCount * prideMultiplier;
        float damageCanBeAbsorbed = currentFoodLevel * (1 + (finalCount - 1) * 0.5f);

        // --- 2. 実際に肩代わりするダメージ量を決定 ---
        float damageToAbsorb = Math.min(incomingDamage, damageCanBeAbsorbed);

        if (damageToAbsorb > 0) {
            // --- 3. 消費する満腹度を計算 ---
            // 肩代わりしたダメージと同じ量の満腹度を消費する
            int foodToConsume = (int) Math.ceil(damageToAbsorb / finalCount);

            // --- 4. 満腹度を消費し、ダメージを軽減する ---
            foodData.setFoodLevel(currentFoodLevel - foodToConsume);
            event.setNewDamage(incomingDamage - damageToAbsorb);

            // --- 5. 演出 ---
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, 2.0f);
        }
    }
}