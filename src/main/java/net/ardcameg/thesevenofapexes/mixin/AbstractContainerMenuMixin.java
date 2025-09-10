package net.ardcameg.thesevenofapexes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow
    public abstract ItemStack getCarried();

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void Mixin$PreventPlaceAndDrop(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (clickType == ClickType.QUICK_MOVE) { //人任せ
            return;
        }

        if (getCarried().getItem() instanceof ForbiddenItem) {
            if (slotId == -999) { // スロット外
                BuffItemUtils.sendForbiddenCantBeDiscarded(player);
                ci.cancel();
                return;
            }

            if (slotId >= 0) { // 外部インベントリのスロット
                Slot destinationSlot = this.slots.get(slotId);
                if (!(destinationSlot.container instanceof Inventory)) {
                    BuffItemUtils.sendForbiddenCantBeDiscarded(player);
                    ci.cancel();
                }
            }
        }
    }


    @Inject(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0),
            cancellable = true)
    private void Mixin$PreventQuickMove(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotId < 0 || slotId >= this.slots.size()) return;

        Slot sourceSlot = this.slots.get(slotId);

        if (sourceSlot.hasItem()
                && sourceSlot.getItem().getItem() instanceof ForbiddenItem
                && sourceSlot.container instanceof Inventory) {

            String currentMenuName = this.getClass().getName();

            // Server & Client の両方を考慮
            boolean isPlayerInventoryOnly =
                    currentMenuName.equals("net.minecraft.world.inventory.PlayerMenu")
                            || currentMenuName.equals("net.minecraft.world.inventory.InventoryMenu");

            if (!isPlayerInventoryOnly) {
                BuffItemUtils.sendForbiddenCantBeDiscarded(player);
                ci.cancel();
            }
        }
    }


    @Inject(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;safeTake(IILnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0
            ), cancellable = true)
    private void Mixin$BeforeSafeTake(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci, @Local(name = "slot3")Slot slot3) {
        if (slotId < 0) return;

        // シンプル捨て
        if (clickType == ClickType.THROW && slot3.getItem().getItem() instanceof ForbiddenItem) {
            BuffItemUtils.sendForbiddenCantBeDiscarded(player);
            ci.cancel();
        }
    }

    /*

    @ModifyExpressionValue(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 2))
    private boolean Mixin$ModifyCondOnPickUp(boolean original, @Local(argsOnly = true)Player player) {
        boolean canPickUp = original || getCarried().getItem() instanceof ForbiddenItem;
        if (canPickUp) {
            BuffItemUtils.sendForbiddenCantBeDiscarded(player);
        }
        return canPickUp;
    }

     */

    @ModifyExpressionValue(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z",
                    ordinal = 0))
    private boolean Mixin$ModifyCondOnSwap(boolean original, @Local(name = "itemstack7")ItemStack itemstack7, @Local(name = "inventory")Inventory inventory, @Local(argsOnly = true)Player player) {
        if(!original && itemstack7.getItem() instanceof ForbiddenItem){
            for(int i=0;i<inventory.items.size();i++){
                int slotNum = inventory.items.size() - 1 - i;
                ItemStack s = inventory.getItem(slotNum);
                if(!(s.getItem() instanceof ForbiddenItem)){
                    player.drop(s, true);
                    inventory.setItem(slotNum, s);
                    BuffItemUtils.sendForbiddenCantBeDiscarded(player);
                    return true;
                }
            }
        }
        return original;
    }

}
