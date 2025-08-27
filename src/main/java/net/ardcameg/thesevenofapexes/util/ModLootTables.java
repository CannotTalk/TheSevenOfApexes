// net/ardcameg/thesevenofapexes/util/ModLootTables.java

package net.ardcameg.thesevenofapexes.util;

import net.minecraft.world.item.Item;
import java.util.ArrayList;
import java.util.List;

public final class ModLootTables {
    private ModLootTables() {}

    // リストを書き換え可能にするためにArrayListに変更
    private static List<Item> COMMON_ITEMS = new ArrayList<>();
    private static List<Item> UNCOMMON_ITEMS = new ArrayList<>();
    private static List<Item> RARE_ITEMS = new ArrayList<>();
    private static List<Item> EPIC_ITEMS = new ArrayList<>();
    private static List<Item> LEGENDARY_ITEMS = new ArrayList<>();

    // リスナーがリストをクリアするために使用
    public static void clearAll() {
        COMMON_ITEMS.clear();
        UNCOMMON_ITEMS.clear();
        RARE_ITEMS.clear();
        EPIC_ITEMS.clear();
        LEGENDARY_ITEMS.clear();
    }

    // リスナーが新しいリストを設定するために使用
    public static void setCommonItems(List<Item> items) { COMMON_ITEMS = items; }
    public static void setUncommonItems(List<Item> items) { UNCOMMON_ITEMS = items; }
    public static void setRareItems(List<Item> items) { RARE_ITEMS = items; }
    public static void setEpicItems(List<Item> items) { EPIC_ITEMS = items; }
    public static void setLegendaryItems(List<Item> items) { LEGENDARY_ITEMS = items; }

    // PackLootManagerからアクセスするためのゲッター (変更なし)
    public static List<Item> getCommonItems() { return COMMON_ITEMS; }
    public static List<Item> getUncommonItems() { return UNCOMMON_ITEMS; }
    public static List<Item> getRareItems() { return RARE_ITEMS; }
    public static List<Item> getEpicItems() { return EPIC_ITEMS; }
    public static List<Item> getLegendaryItems() { return LEGENDARY_ITEMS; }
}