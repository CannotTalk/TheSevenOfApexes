package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public final class SteelClawsAbility {
    private SteelClawsAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * "鋼鉄の爪"の即死効果を適用する
     */
    public static void apply(Player player, LivingEntity target, int clawCount, int prideMultiplier) {
        if (clawCount <= 0) return;

        int finalCount = clawCount * prideMultiplier;
        float procChanceBase = Config.steelClawsProcChanceBase.get().floatValue();
        float selfKillChance = Config.steelClawsSelfKillChance.get().floatValue();
        float procChance = procChanceBase + (finalCount - 1) * (procChanceBase / 2);
        float bossDamageRatio = Config.steelClawsBossDamageRatio.get().floatValue();

        if (RANDOM.nextFloat() < procChance) {
            if (RANDOM.nextFloat() < selfKillChance) {
                player.kill(); // 自爆はkill()で問題ない
                player.sendSystemMessage(Component.translatable("message.seven_apexes.claws_activate"));
            } else {
                // ターゲットがボスかどうかを判定
                if (target instanceof EnderDragon || target instanceof WitherBoss) {
                    // ボスの場合、最大HPの25%ダメージ
                    float bossDamage = target.getMaxHealth() * bossDamageRatio * finalCount;
                    target.hurt(player.damageSources().playerAttack(player), bossDamage);
                } else {
                    // ボス以外の場合、即死級の大ダメージを与える
                    target.hurt(player.damageSources().playerAttack(player), Float.MAX_VALUE);
                }
                player.sendSystemMessage(Component.translatable("message.seven_apexes.claws_activate"));
                BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_STEEL_CLAWS.get());
            }
        }
    }
}