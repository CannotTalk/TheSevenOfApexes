package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class EarthboundCurseAbility {
    private EarthboundCurseAbility() {}

    public static void onPlayerDamaged(LivingDamageEvent.Pre event, Player player){
        if (!event.getSource().is(DamageTypes.FALL)) return;

        event.setNewDamage(1.0f);
    }

    /**
     * エンティティのジャンプによる上昇を打ち消す。
     * @param entity ジャンプしたエンティティ
     */
    public static void suppressJump(LivingEntity entity, int itemCount, boolean reversed) {
        // 現在の移動量を取得
        Vec3 currentMotion = entity.getDeltaMovement();

        if (!reversed) {
            // Y軸（垂直方向）の移動量のみを0にして、水平方向の移動は維持する
            entity.setDeltaMovement(currentMotion.x, 0, currentMotion.z);
        }else {
            entity.setDeltaMovement(currentMotion.x, currentMotion.y * (itemCount + 1), currentMotion.z);
        }
    }
}