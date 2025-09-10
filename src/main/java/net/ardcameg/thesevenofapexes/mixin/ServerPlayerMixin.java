package net.ardcameg.thesevenofapexes.mixin;

import com.mojang.datafixers.util.Either;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "startSleepInBed", at = @At("RETURN"), cancellable = true)
    private void onStartSleepInBed(BlockPos bedPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (cir.getReturnValue().right().isPresent()) {
            if (BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_ENDLESS_INSOMNIA.get()) > 0) {
                player.sendSystemMessage(Component.translatable("message.seven_apexes.endless_insomnia_cant_sleep"));
                cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
            }
        }
    }
}