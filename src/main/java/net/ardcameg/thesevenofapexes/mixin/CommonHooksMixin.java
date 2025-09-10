package net.ardcameg.thesevenofapexes.mixin;

import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(CommonHooks.class)
public abstract class CommonHooksMixin {

    @Inject(method = "onPlayerTossEvent",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;captureDrops(Ljava/util/Collection;)Ljava/util/Collection;",
                    ordinal = 0), cancellable = true)
    private static void Mixin$BeforeCaptureDrops(Player player, ItemStack item, boolean includeName, CallbackInfoReturnable<ItemEntity> cir) {
        Inventory inventory = player.getInventory();
        if(item.getItem() instanceof ForbiddenItem && inventory.getFreeSlot() == -1){
            for(int i=0;i<inventory.items.size();i++){
                int slotNum = inventory.items.size() - 1 - i;
                ItemStack s = inventory.getItem(slotNum);
                if(!(s.getItem() instanceof ForbiddenItem)){
                    player.drop(s, true);
                    inventory.setItem(slotNum, item);
                    BuffItemUtils.sendForbiddenCantBeDiscarded(player);
                    cir.cancel();
                    return;
                }
            }
        }
    }

}
