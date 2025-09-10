// net/ardcameg/thesevenofapexes/util/PackLootTableReloadListener.java

package net.ardcameg.thesevenofapexes.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackLootTableReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger(PackLootTableReloadListener.class);
    private static final String DIRECTORY = "loot_tables/packs";

    public PackLootTableReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        LOGGER.info("Starting to load The Seven of Apexes pack loot tables...");
        // 一旦すべてのリストをクリア
        ModLootTables.clearAll();

        // 各JSONファイルを処理
        jsonMap.forEach((location, element) -> {
            try {
                // "seven_apexes:loot_tables/packs/common.json" -> "common"
                String rarity = location.getPath().replace(DIRECTORY + "/", "").replace(".json", "");
                List<Item> items = parseItems(element);

                switch (rarity) {
                    case "common" -> ModLootTables.setCommonItems(items);
                    case "uncommon" -> ModLootTables.setUncommonItems(items);
                    case "rare" -> ModLootTables.setRareItems(items);
                    case "epic" -> ModLootTables.setEpicItems(items);
                    case "legendary" -> ModLootTables.setLegendaryItems(items);
                    case "forbidden" -> ModLootTables.setForbiddenItems(items);
                    default -> LOGGER.warn("Unknown pack loot table rarity: {}", rarity);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse pack loot table: {}", location, e);
            }
        });
        LOGGER.info("Successfully loaded pack loot tables.");
    }

    private List<Item> parseItems(JsonElement element) {
        List<Item> items = new ArrayList<>();
        element.getAsJsonObject()
                .getAsJsonArray("pools")
                .forEach(pool -> pool.getAsJsonObject()
                        .getAsJsonArray("entries")
                        .forEach(entry -> {
                            String itemName = entry.getAsJsonObject().get("name").getAsString();
                            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName));
                            items.add(item);
                        }));
        return items;
    }
}