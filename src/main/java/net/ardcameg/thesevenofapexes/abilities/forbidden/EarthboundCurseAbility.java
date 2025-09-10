package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EarthboundCurseAbility {
    private EarthboundCurseAbility() {}

    /**
     * エンティティのジャンプによる上昇を打ち消す。
     * @param entity ジャンプしたエンティティ
     */
    public static void suppressJump(LivingEntity entity) {
        // 現在の移動量を取得
        Vec3 currentMotion = entity.getDeltaMovement();
        // Y軸（垂直方向）の移動量のみを0にして、水平方向の移動は維持する
        entity.setDeltaMovement(currentMotion.x, 0, currentMotion.z);
    }
}