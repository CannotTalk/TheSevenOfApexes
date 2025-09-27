package net.ardcameg.thesevenofapexes.item;

import net.ardcameg.thesevenofapexes.abilities.forbidden.ReversalArtifactAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForbiddenItem extends Item {
    public ForbiddenItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
            return;
        }

        boolean isReversed = ReversalArtifactAbility.checkForbiddenReversed(player);

        if (isReversed && !(
                pStack.getItem() == ModItems.FORBIDDEN_REVERSAL_ARTIFACT.get()
                || pStack.getItem() == ModItems.FORBIDDEN_HEART_OF_THE_ABYSS.get()
        )) {
            // 反転時はレアリティ表示の色を変え、状態を示すテキストを追加
            pTooltipComponents.add(Component.translatable("tooltip.seven_apexes.forbidden_rarity_reversed").withStyle(ChatFormatting.GOLD));
            pTooltipComponents.add(Component.translatable("tooltip.seven_apexes.status_reversed").withStyle(ChatFormatting.GREEN));
        } else {
            // 通常時 または 「反転のアーティファクト」自身のとき
            pTooltipComponents.add(Component.translatable("tooltip.seven_apexes.forbidden_rarity").withStyle(ChatFormatting.DARK_PURPLE));
        }

        pTooltipComponents.add(Component.translatable("tooltip.seven_apexes.forbidden_warning").withStyle(ChatFormatting.RED));

        // super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag); // アイテム固有のツールチップは一旦保留
    }
}