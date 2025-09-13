package net.ardcameg.thesevenofapexes.mixin;

import net.ardcameg.thesevenofapexes.abilities.util.WorldsRejectionAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystal.class)
public abstract class EndCrystalMixin {

    @Inject(method = "hurt", at = @At("RETURN"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // hurtメソッドがtrueを返し、クリスタルが破壊された場合のみ実行
        if (cir.getReturnValue()) {
            EndCrystal crystal = (EndCrystal) (Object) this;

            // サーバーサイドであり、ダメージの原因がプレイヤーである場合
            if (!crystal.level().isClientSide() && source.getEntity() instanceof ServerPlayer player) {
                // WorldsRejectionAbilityに処理を委任
                WorldsRejectionAbility.onCrystalDestroyed(crystal, player);
            }
        }
    }
}