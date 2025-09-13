package net.ardcameg.thesevenofapexes.mixin;

import net.ardcameg.thesevenofapexes.abilities.util.WorldsRejectionAbility;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;

        // サーバーサイドでのみ実行
        if (!dragon.level().isClientSide()) {
            // 専門家に処理を委任
            WorldsRejectionAbility.updateEnderDragonBehavior(dragon);
        }
    }
}