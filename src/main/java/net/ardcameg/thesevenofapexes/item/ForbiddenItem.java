package net.ardcameg.thesevenofapexes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForbiddenItem extends Item {
    public ForbiddenItem(Properties pProperties) {
        super(pProperties);
    }

    // アイテムがエンチャントされているかのような輝きを放つようにする
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    // アイテムにカスタムツールチップ（説明文）を追加する
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("tooltip.seven_apexes.forbidden_rarity").withStyle(ChatFormatting.DARK_PURPLE));
        pTooltipComponents.add(Component.translatable("tooltip.seven_apexes.forbidden_warning").withStyle(ChatFormatting.RED));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}