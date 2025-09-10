package net.ardcameg.thesevenofapexes.mixin;

import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "drop(Z)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true)
    private void dropMixin(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {

        LocalPlayer self = (LocalPlayer) (Object) this;

        if (self.getInventory().getSelected().getItem() instanceof ForbiddenItem) {
            BuffItemUtils.sendForbiddenCantBeDiscarded(self);
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

}
