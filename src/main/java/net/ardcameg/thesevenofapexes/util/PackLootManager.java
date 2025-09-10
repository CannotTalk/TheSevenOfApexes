// net/ardcameg/thesevenofapexes/util/PackLootManager.java

package net.ardcameg.thesevenofapexes.util;

import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public final class PackLootManager {
    private PackLootManager() {}
    private static final Random RANDOM = new Random();

    /**
     * ランダムパックを開封する
     */
    public static void openRandomPack(Player player) {
        double specialPackRoll = RANDOM.nextDouble();

        if (specialPackRoll < 0.05) {
            giveRandomItem(player, List.of(ModItems.EPIC_PACK.get()));
        } else if (specialPackRoll < 0.05 + 0.10) {
            giveRandomItem(player, List.of(ModItems.RARE_PACK.get()));
        } else {
            double itemRoll = RANDOM.nextDouble();
            if (itemRoll < 0.001) {
                giveRandomItem(player, ModLootTables.getLegendaryItems());
            } else if (itemRoll < 0.001 + 0.05) {
                giveRandomItem(player, ModLootTables.getEpicItems());
            } else if (itemRoll < 0.001 + 0.05 + 0.15) {
                giveRandomItem(player, ModLootTables.getRareItems());
            } else if (itemRoll < 0.001 + 0.05 + 0.15 + 0.30) {
                giveRandomItem(player, ModLootTables.getUncommonItems());
            } else {
                giveRandomItem(player, ModLootTables.getCommonItems());
            }
        }
    }

    /**
     * レアパックを開封する
     */
    public static void openRarePack(Player player) {
        double roll = RANDOM.nextDouble();
        if (roll < 0.005) {
            giveRandomItem(player, ModLootTables.getLegendaryItems());
        } else if (roll < 0.005 + 0.25) {
            giveRandomItem(player, ModLootTables.getEpicItems());
        } else {
            giveRandomItem(player, ModLootTables.getRareItems());
        }
    }

    /**
     * エピックパックを開封する
     */
    public static void openEpicPack(Player player) {
        double specialPackRoll = RANDOM.nextDouble();
        if (specialPackRoll < 0.10) {
            giveRandomItem(player, List.of(ModItems.LEGENDARY_PACK.get()));
        } else {
            double itemRoll = RANDOM.nextDouble();
            if (itemRoll < 0.01) {
                giveRandomItem(player, ModLootTables.getLegendaryItems());
            } else {
                giveRandomItem(player, ModLootTables.getEpicItems());
            }
        }
    }

    /**
     * レジェンドパックを開封する
     */
    public static void openLegendaryPack(Player player) {
        giveRandomItem(player, ModLootTables.getLegendaryItems());
    }

    /**
     * 指定されたリストからランダムなアイテムを1つプレイヤーに与える
     */
    /**
     * 指定されたリストからランダムなアイテムを1つプレイヤーに与える
     */
    private static void giveRandomItem(Player player, List<Item> itemList) {
        if (itemList.isEmpty()) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.CHEST_CLOSE, SoundSource.PLAYERS, 0.5f, 1.5f);
            return;
        }
        Item chosenItem = itemList.get(RANDOM.nextInt(itemList.size()));
        ItemStack itemStack = new ItemStack(chosenItem);

        if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
        }
    }

    /**
     * パンドラの箱を開封する
     */
    public static void openPandorasBox(Player player) {
        // 1. プレイヤーが所持する禁忌級アイテムの数を数える
        int forbiddenItemCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof ForbiddenItem) {
                forbiddenItemCount++;
            }
        }
        // カーソル持ちもチェック
        if (player.containerMenu.getCarried().getItem() instanceof ForbiddenItem) {
            forbiddenItemCount++;
        }

        // 2. 禁忌級アイテムが出る確率を計算
        // 基本確率55%から、所持数 * 5% を引く
        double forbiddenChance = 0.55 - (forbiddenItemCount * 0.05);
        // 確率が0%未満にならないように制御
        forbiddenChance = Math.max(0.0, forbiddenChance);

        // 3. 運命の抽選
        if (RANDOM.nextDouble() < forbiddenChance) {
            // --- 地獄：禁忌級アイテムを排出 ---
            giveRandomItem(player, ModLootTables.getForbiddenItems());
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.7f, 1.5f);
        } else {
            // --- 天国：高レアリティアイテムを排出 ---
            // 5%の確率で伝説級、95%で英雄級
            if (RANDOM.nextDouble() < 0.05) {
                giveRandomItem(player, ModLootTables.getLegendaryItems());
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.5f);
            } else {
                giveRandomItem(player, ModLootTables.getEpicItems());
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
}