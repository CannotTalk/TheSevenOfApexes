package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class PearlEyeAbility {
    private PearlEyeAbility() {}

    /**
     * 任務：エンダーパールによるダメージを軽減する
     */
    public static void reduceEnderPearlDamage(LivingDamageEvent.Pre event, int eyeCount, int prideMultiplier) {
        // --- 1. このダメージが、エンダーパールによるものか判定する ---
        // is(DamageTypes.FALL) は、エンダーパールのテレポートダメージの正しい型です
        if (!event.getSource().is(DamageTypes.FALL)) {
            return;
        }

        int finalCount = eyeCount * prideMultiplier;

        // --- 2. ダメージを軽減する ---
        // 1個あたり1ダメージ軽減
        float reductionAmount = 1.0f * finalCount;
        float originalDamage = event.getNewDamage();
        float newDamage = Math.max(0, originalDamage - reductionAmount);

        event.setNewDamage(newDamage);
    }
}