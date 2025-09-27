package net.ardcameg.thesevenofapexes.item;
import net.ardcameg.thesevenofapexes.event.AdvancementTriggers;
import net.ardcameg.thesevenofapexes.util.PackLootManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public class PandorasBoxItem extends Item {
    public PandorasBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PackLootManager.openPandorasBox(player);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
                AdvancementTriggers.grantAdvancement((ServerPlayer) player, "hope_or_despair");
            }
        }

        //実績を達成
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementTriggers.grantAdvancement(serverPlayer, "high_rarity");

            AdvancementTriggers.grantCriterion(serverPlayer, "collector_of_possibilities", "opened_pandoras_box");
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}