package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Map;

public class ReversalArtifactAbility {
    private ReversalArtifactAbility() {}

    public static boolean checkForbiddenReversed(Player player){
        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);

        return baseCounts.getOrDefault(ModItems.FORBIDDEN_REVERSAL_ARTIFACT.get(), 0) > 0;
    }
}
