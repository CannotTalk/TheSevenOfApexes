package net.ardcameg.thesevenofapexes.util;

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

    // --- 各レアリティのアイテムリスト ---
    // 新しいアイテムを追加したら、ここのリストに追加するだけでOK
    private static final List<Item> LEGENDARY_ITEMS = List.of(
            ModItems.LEGENDARY_PRIDE.get(), ModItems.LEGENDARY_ENVY.get(), ModItems.LEGENDARY_WRATH.get(),
            ModItems.LEGENDARY_SLOTH.get(), ModItems.LEGENDARY_GREED.get(), ModItems.LEGENDARY_GLUTTONY.get(),
            ModItems.LEGENDARY_LUST.get(), ModItems.LEGENDARY_SUNLIGHT_SACRED_SEAL.get(), ModItems.LEGENDARY_MOONLIGHT_SACRED_SEAL.get()
    );

    private static final List<Item> EPIC_ITEMS = List.of(
            ModItems.EPIC_LIGHTNING_FIST.get(), ModItems.EPIC_PHOENIX_FEATHER.get(), ModItems.EPIC_LIFE_STEEL_STICK.get(),
            ModItems.EPIC_BERSERKERS_DRAG.get(), ModItems.EPIC_SHADOW_BIND_GLOVES.get(), ModItems.EPIC_ARRIVAL_OF_REVIVAL.get(),
            ModItems.EPIC_WALKING_ANATHEMA.get(), ModItems.EPIC_SCARRED_GRAIL.get(), ModItems.EPIC_VOID_MANTLE.get(),
            ModItems.EPIC_STEEL_CLAWS.get(), ModItems.EPIC_FIENDS_BARGAIN.get(), ModItems.EPIC_REVERSAL_HOURGLASS.get(),
            ModItems.EPIC_GOLIATHS_GAVEL.get()
    );

    private static final List<Item> RARE_ITEMS = List.of(
            ModItems.RARE_NIGHT_OWL_EYES.get(), ModItems.RARE_GUARDIANS_CREST.get(), ModItems.RARE_GILLS_CHARM.get(),
            ModItems.RARE_SNIPERS_MONOCLE.get(), ModItems.RARE_LAST_STAND.get(), ModItems.RARE_BLADEMASTERS_PROWESS.get(),
            ModItems.RARE_DEADEYE_GLASS.get(), ModItems.RARE_REAPERS_SCYTHE.get(), ModItems.RARE_ARCHITECTS_HASTE.get(),
            ModItems.RARE_VITAL_CONVERSION_RING.get(), ModItems.RARE_BOUNTY_TOTEM.get()
    );

    // TODO: アンコモンとコモンのアイテムリストを後で追加
    private static final List<Item> UNCOMMON_ITEMS = List.of();
    private static final List<Item> COMMON_ITEMS = List.of();


    /**
     * ランダムパックを開封する
     * @param player 開封したプレイヤー
     */
    public static void openRandomPack(Player player) {
        // ★★★ ここからが修正箇所 ★★★
        // --- 最初に、特殊なパックが出現するかどうかを判定 ---
        double specialPackRoll = RANDOM.nextDouble();

        if (specialPackRoll < 0.05) { // 5%の確率でエピックパック
            giveRandomItem(player, List.of(ModItems.EPIC_PACK.get())); // EPIC_PACKを出す！
        } else if (specialPackRoll < 0.05 + 0.10) { // 10%の確率でレアパック
            giveRandomItem(player, List.of(ModItems.RARE_PACK.get()));
        } else {
            // --- 特殊なパックが出なかった場合、通常通りアイテムを抽選 ---
            double itemRoll = RANDOM.nextDouble();

            if (itemRoll < 0.001) {
                giveRandomItem(player, LEGENDARY_ITEMS);
            } else if (itemRoll < 0.001 + 0.05) {
                giveRandomItem(player, EPIC_ITEMS);
            } else if (itemRoll < 0.001 + 0.05 + 0.15) {
                giveRandomItem(player, RARE_ITEMS);
            } else if (itemRoll < 0.001 + 0.05 + 0.15 + 0.30) {
                giveRandomItem(player, UNCOMMON_ITEMS);
            } else {
                giveRandomItem(player, COMMON_ITEMS);
            }
        }
    }

    /**
     * レアパックを開封する
     * @param player 開封したプレイヤー
     */
    public static void openRarePack(Player player) {
        double roll = RANDOM.nextDouble(); // 0.0 ~ 1.0の乱数を生成

        if (roll < 0.005) { // 0.5%
            giveRandomItem(player, LEGENDARY_ITEMS);
        } else if (roll < 0.005 + 0.25) { // 25%
            giveRandomItem(player, EPIC_ITEMS);
        } else { // 残り (約74.5%)
            giveRandomItem(player, RARE_ITEMS);
        }
    }

    /**
     * エピックパックを開封する
     */
    public static void openEpicPack(Player player) {
        double specialPackRoll = RANDOM.nextDouble();

        if (specialPackRoll < 0.10) { // 10%の確率でレジェンドパック
            giveRandomItem(player, List.of(ModItems.LEGENDARY_PACK.get()));
        } else {
            // --- レジェンドパックが出なかった場合、通常通りアイテムを抽選 ---
            double itemRoll = RANDOM.nextDouble();

            if (itemRoll < 0.01) {
                giveRandomItem(player, LEGENDARY_ITEMS);
            } else {
                giveRandomItem(player, EPIC_ITEMS);
            }
        }
    }

    /**
     * レジェンドパックを開封する
     * @param player 開封したプレイヤー
     */
    public static void openLegendaryPack(Player player) {
        // 100%の確率で、レジェンドアイテムリストから1つを与える
        giveRandomItem(player, LEGENDARY_ITEMS);
    }

    /**
     * 指定されたリストからランダムなアイテムを1つプレイヤーに与える
     * @param player 対象のプレイヤー
     * @param itemList アイテムのリスト
     */
    private static void giveRandomItem(Player player, List<Item> itemList) {
        // リストが空（未実装）の場合
        if (itemList.isEmpty()) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.CHEST_CLOSE, SoundSource.PLAYERS, 0.5f, 1.5f);
            return;
        }

        // リストからランダムなアイテムを1つ選ぶ
        Item chosenItem = itemList.get(RANDOM.nextInt(itemList.size()));
        ItemStack itemStack = new ItemStack(chosenItem);

        // プレイヤーにアイテムを与える
        // もしインベントリがいっぱいなら、足元にドロップさせる
        if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
        }

        // 成功の音を鳴らす
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }
}