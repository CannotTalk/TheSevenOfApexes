package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class TauntingBeaconAbility {
    private TauntingBeaconAbility() {}

    private static final double RADIUS = 32.0;

    /**
     * プレイヤーの周囲にいる敵対・中立Mobを強制的に敵対させるオーラを発生させる。
     * @param player オーラの中心となるプレイヤー
     */
    public static void applyAura(ServerPlayer player) {
        Level level = player.level();
        AABB searchArea = new AABB(player.blockPosition()).inflate(RADIUS);

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea);

        for (LivingEntity entity : nearbyEntities) {
            if (entity instanceof Mob mob && (mob instanceof Enemy || mob instanceof NeutralMob)) {
                // 既にプレイヤーをターゲットにしている場合は処理をスキップする。
                if (mob.getTarget() == player) {
                    continue;
                }

                mob.setTarget(player);
            }
        }
    }
}