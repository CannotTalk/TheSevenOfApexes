package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.item.component.ModDataComponents;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ScarredGrailAbility {
    private ScarredGrailAbility() {}
    private static final Random RANDOM = new Random();

    public static void onPlayerDamaged(LivingDamageEvent.Pre event, Player player) {
        List<ItemStack> grails = findGrails(player);
        if (grails.isEmpty()) return;

        float originalDamage = event.getNewDamage();
        event.setNewDamage(0);

        storeDamageInGrails(grails, originalDamage);
        player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5f, 1.5f);

        boolean hasExploded = false;
        List<ItemStack> explodedGrails = new ArrayList<>();

        for (ItemStack grail : grails) {
            if (RANDOM.nextFloat() < 0.2f) {
                hasExploded = true;
                explodedGrails.add(grail);
            }
        }

        if (hasExploded) {
            float totalDamageToTake = 0;
            for (ItemStack explodedGrail : explodedGrails) {
                totalDamageToTake += getTotalStoredDamage(explodedGrail);
                explodedGrail.shrink(1);
            }

            BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_SCARRED_GRAIL.get());
            player.getPersistentData().putBoolean("TAKING_GRAIL_DAMAGE", true);
            player.hurt(player.damageSources().magic(), totalDamageToTake);
            player.getPersistentData().remove("TAKING_GRAIL_DAMAGE");
        }
    }

    // --- ヘルパーメソッド群 ---

    private static List<ItemStack> findGrails(Player player) {
        List<ItemStack> foundGrails = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.EPIC_SCARRED_GRAIL.get())) {
                foundGrails.add(stack);
            }
        }
        return foundGrails;
    }

    private static void storeDamageInGrails(List<ItemStack> grails, float damage) {
        for (ItemStack grail : grails) {
            // 1. 現在のダメージリストを取得、なければ新しいリストを作成
            List<Float> currentDamage = new ArrayList<>(grail.getOrDefault(ModDataComponents.STORED_DAMAGE.get(), List.of()));
            // 2. 新しいダメージを追加
            currentDamage.add(damage);
            // 3. 更新したリストをデータ部品としてアイテムにセット
            grail.set(ModDataComponents.STORED_DAMAGE.get(), currentDamage);
        }
    }

    //Data Component を使った、データの読み込み
    private static float getTotalStoredDamage(ItemStack grail) {
        List<Float> damageList = grail.get(ModDataComponents.STORED_DAMAGE.get());
        if (damageList == null) return 0;

        float total = 0;
        for (Float damage : damageList) {
            total += damage;
        }
        return total;
    }
}