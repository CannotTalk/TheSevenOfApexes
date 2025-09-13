package net.ardcameg.thesevenofapexes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.event.AdvancementTriggers;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
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
    private void Mixin$CombinedHeadInject(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        // --- サーバーサイドでのみ実行 ---
        if (player.level().isClientSide()) {
            return;
        }

        // --- 禁忌級アイテムの移動制限 ---
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
                    return; // ここで処理を終了
                }
            }
        }

        // スロットIDが有効な範囲にあるか確認
        if (slotId < 0 || slotId >= this.slots.size()) {
            return;
        }

        Slot slot = this.slots.get(slotId);
        // プレイヤーインベントリのスロット、かつ、バフ列（スロット9-17）に対する操作かチェック
        if (slot.container == player.getInventory() && slot.getSlotIndex() >= 9 && slot.getSlotIndex() <= 17) {
            ItemStack cursorStack = getCarried();

            if (!cursorStack.isEmpty()) {
                Item item = cursorStack.getItem();
                // アイテムが我々のModのものであり、かつ禁忌級ではないことを確認
                if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(TheSevenOfApexes.MOD_ID)
                        && !(item instanceof ForbiddenItem)) {

                    AdvancementTriggers.grantAdvancement((ServerPlayer) player, "the_first_sin");
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
        // (このメソッドは変更なし)
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
        // (このメソッドは変更なし)
        if (slotId < 0) return;

        // シンプル捨て
        if (clickType == ClickType.THROW && slot3.getItem().getItem() instanceof ForbiddenItem) {
            BuffItemUtils.sendForbiddenCantBeDiscarded(player);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z",
                    ordinal = 0))
    private boolean Mixin$ModifyCondOnSwap(boolean original, @Local(name = "itemstack7")ItemStack itemstack7, @Local(name = "inventory")Inventory inventory, @Local(argsOnly = true)Player player) {
        // (このメソッドは変更なし)
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