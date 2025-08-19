package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.minecraft.core.Holder; // ★★★ これをimport ★★★
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List; // ★★★ これをimport ★★★
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EnvyAbility {
    private EnvyAbility() {}

    public static void apply(Player player, LivingEntity target, Map<UUID, Set<UUID>> copiedEntitiesMap, int finalCount, int multiplier) {
        if (finalCount <= 0) return;

        UUID playerUUID = player.getUUID();
        UUID targetUUID = target.getUUID();

        Set<UUID> copiedSet = copiedEntitiesMap.computeIfAbsent(playerUUID, k -> new java.util.HashSet<>());
        if (copiedSet.contains(targetUUID)) return;

        ItemStack weapon = target.getMainHandItem();

        if (!weapon.isEmpty()) {
            // 1. データ部品から、属性修飾子(アタッシュケース)を取得
            ItemAttributeModifiers modifierWrapper = weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            // 2. アタッシュケースから、中身である「書類の束(List<Entry>)」を取り出す
            List<ItemAttributeModifiers.Entry> modifierList = modifierWrapper.modifiers();

            // 3. 書類の束を、一枚一枚ループして確認する
            for (ItemAttributeModifiers.Entry entry : modifierList) {
                // 4. 書類に書かれている属性の種類を取得する
                Holder<Attribute> attributeHolder = entry.attribute();
                // 5. その種類が「攻撃力」であるか、確認する
                if (attributeHolder.is(Attributes.ATTACK_DAMAGE)) {
                    // ★★★ 発見！これぞ武器なり！ ★★★
                    copiedSet.add(targetUUID);

                    if (player.getInventory().getFreeSlot() != -1) {
                        player.getInventory().add(weapon.copy());
                        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                    } else {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.5f, 1.0f);
                    }
                    // 目的は達成されたので、これ以上のループも処理も不要。この場を去る。
                    return;
                }
            }
        }
    }
}