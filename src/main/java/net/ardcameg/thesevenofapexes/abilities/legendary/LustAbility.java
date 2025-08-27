package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public final class LustAbility {
    private LustAbility() {}

    /**
     * 任務1：継続的な効果を適用する (村の英雄)
     * @param player プレイヤー
     * @param lustCount "色欲"の素の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void applyPassiveEffect(Player player, int lustCount, int prideMultiplier) {
        if (lustCount <= 0) return;

        int effectLevel = Config.lustEffectLevel.getAsInt() - 1; // Lv13は内部的に12

        // 効果時間40tick(2秒)、アンビエント(パーティクルが少ない)、パーティクル非表示
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 40, effectLevel, true, false, false));
    }

    /**
     * 任務2：死亡時に蘇生を試みる
     * @param player 死亡したプレイヤー
     * @param attacker プレイヤーを倒した攻撃者 (いない場合もある)
     * @param lustCount "色欲"の素の所持数
     * @param prideMultiplier "傲慢"の効果倍率
     * @return 蘇生に成功すればtrue、失敗すればfalse
     */
    public static boolean attemptRevive(Player player, Entity attacker, int lustCount, int prideMultiplier) {
        if (lustCount <= 0) return false;

        // --- 0. クリエイティブならキャンセル ---
        if (player.isCreative()) {
            return false;
        }

        // --- 1. 生贄候補を探す ---
        AABB searchArea = player.getBoundingBox().inflate(16.0);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity -> {
            if (entity.equals(attacker)) return false; //攻撃をしてきたMobは除く
            return entity instanceof NeutralMob || entity instanceof Animal;
        });

        // --- 2. 生贄が1体以上いるかチェック ---
        if (candidates.isEmpty()) {
            return false; // 生贄が1体もいないので蘇生失敗
        }

        // --- 3. 生贄を選別し、体力を計算 ---
        // プレイヤーに近い順にソート
        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));

        // まず、蘇生のために最低1体の生贄を確保する
        LivingEntity firstSacrifice = candidates.remove(0); // 最初の1体を取得し、リストから削除
        float totalHealthToRevive = firstSacrifice.getHealth();
        firstSacrifice.kill(); // 最初の1体を生贄に捧げる

        // 追加で必要な生贄の数を計算
        // (合計有効数 - 1) が追加で生贄にできる数
        int additionalSacrificesNeeded = (lustCount * prideMultiplier) - 1;

        if (additionalSacrificesNeeded > 0 && !candidates.isEmpty()) {
            // 追加の生贄を、必要な数、または残っている候補の数だけ選ぶ
            int actualAdditionalSacrifices = Math.min(additionalSacrificesNeeded, candidates.size());
            List<LivingEntity> additionalSacrifices = candidates.subList(0, actualAdditionalSacrifices);

            for (LivingEntity sacrifice : additionalSacrifices) {
                totalHealthToRevive += sacrifice.getHealth();
                sacrifice.kill(); // 追加の生贄を捧げる
            }
        }

        // --- 4. プレイヤーを蘇生させる (変更なし) ---
        float finalHealth = Math.min(totalHealthToRevive, player.getMaxHealth());
        player.setHealth(finalHealth);

        // 回復と同時に、全てのデバフを浄化する
        BuffItemUtils.clearAllDebuffs(player);

        BuffItemUtils.playTotemAnimation(player, ModItems.LEGENDARY_LUST.get());
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 1.0f);

        return true; // 蘇生成功！
    }
}