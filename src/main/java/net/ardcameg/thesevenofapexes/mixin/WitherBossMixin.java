package net.ardcameg.thesevenofapexes.mixin;

import net.ardcameg.thesevenofapexes.abilities.util.WorldsRejectionAbility;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        // thisをWitherBossインスタンスとしてキャストする
        WitherBoss wither = (WitherBoss) (Object) this;

        // 1秒に1回だけ処理を実行する
        if (wither.level().getGameTime() % 20 == 0) {
            // 我々の専門家（WorldsRejectionAbility）を呼び出す
            WorldsRejectionAbility.updateBossBehavior(wither);
        }
    }
}