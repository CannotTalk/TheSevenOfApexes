package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class WalkingAnathemaAbility {
    private WalkingAnathemaAbility() {}

    /**
     * 任務1："忌み嫌われるもの"の継続的なオーラ効果を適用する
     */
    public static void applyAura(Player player, int anathemaCount, int prideMultiplier) {
        if (anathemaCount <= 0 || player.level().getGameTime() % 10 != 0) {
            // 0.5秒に1回だけ処理
            return;
        }

        int finalCount = anathemaCount * prideMultiplier;

        // --- 1. オーラの範囲とダメージを計算 ---
        float baseRadius = Config.walkingAnathemaBaseRadius.get().floatValue();
        float radius = baseRadius + (baseRadius / 2) * (finalCount - 1);
        float basePerDamage = Config.walkingAnathemaDamagePerSecond.get().floatValue();
        float processDamage = (basePerDamage * finalCount) / 2;

        // --- 2. 範囲内の敵性Mobを探す ---
        AABB searchArea = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity ->
                entity instanceof Enemy && entity.isAlive() // 敵性Mobであり、生きている
        );

        // --- 3. ターゲットにダメージを与える ---
        for (LivingEntity target : targets) {
            // ダメージソースを「魔法」や「間接的なもの」にすることで、
            // プレイヤーが直接攻撃したと見なされないようにする
            float maxDamage = target.getHealth() - 1.0F; // 瀕死どまり
            float actualDamage = Math.min(maxDamage, processDamage);
            target.hurt(player.damageSources().magic(), actualDamage);
        }
    }
}