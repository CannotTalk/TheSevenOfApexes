package net.ardcameg.thesevenofapexes.item;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheSevenOfApexes.MOD_ID);

    public static final DeferredItem<Item> LEGENDARY_PRIDE = ITEMS.register("legendary_pride" ,
            () -> new GlintingItem(new Item.Properties()
                    .rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_ENVY = ITEMS.register("legendary_envy",
            () -> new GlintingItem(new Item.Properties()
                    .rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_WRATH = ITEMS.register("legendary_wrath" ,
            () -> new GlintingItem(new Item.Properties()
                    .rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_SLOTH = ITEMS.register("legendary_sloth",
            () -> new GlintingItem(new Item.Properties()
                    .rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_GREED = ITEMS.register("legendary_greed",
            () -> new GlintingItem(new Item.Properties()
                    .rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_GLUTTONY = ITEMS.register("legendary_gluttony",
            () -> new GlintingItem(new Item.Properties()
                    .rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_LUST = ITEMS.register("legendary_lust",
            () -> new GlintingItem(new Item.Properties().
                    rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_SUNLIGHT_SACRED_SEAL = ITEMS.register("legendary_sunlight_sacred_seal",
            () -> new GlintingItem(new Item.Properties().
                    rarity(Rarity.COMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> LEGENDARY_MOONLIGHT_SACRED_SEAL = ITEMS.register("legendary_moonlight_sacred_seal",
            () -> new GlintingItem(new Item.Properties().
                    rarity(Rarity.COMMON)
                    .stacksTo(1)));

    public static final DeferredItem<Item> EPIC_LIGHTNING_FIST = ITEMS.register("epic_lightning_fist",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_PHOENIX_FEATHER = ITEMS.register("epic_phoenix_feather",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_LIFE_STEEL_STICK = ITEMS.register("epic_life_steel_stick",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_BERSERKERS_DRAG = ITEMS.register("epic_berserkers_drag",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_SHADOW_BIND_GLOVES = ITEMS.register("epic_shadow_bind_gloves",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_ARRIVAL_OF_REVIVAL = ITEMS.register("epic_arrival_of_revival",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_WALKING_ANATHEMA = ITEMS.register("epic_walking_anathema",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_SCARRED_GRAIL = ITEMS.register("epic_scarred_grail",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_VOID_MANTLE = ITEMS.register("epic_void_mantle",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_STEEL_CLAWS = ITEMS.register("epic_steel_claws",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_FIENDS_BARGAIN = ITEMS.register("epic_fiends_bargain",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_REVERSAL_HOURGLASS = ITEMS.register("epic_reversal_hourglass",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));
    public static final DeferredItem<Item> EPIC_GOLIATHS_GAVEL = ITEMS.register("epic_goliaths_gavel",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));

    public static final DeferredItem<Item> RARE_NIGHT_OWL_EYES = ITEMS.register("rare_night_owl_eyes",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_GUARDIANS_CREST = ITEMS.register("rare_guardians_crest",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_GILLS_CHARM = ITEMS.register("rare_gills_charm",
            () -> new Item(new Item.Properties().
                    rarity(Rarity.RARE).
                    stacksTo(1)));
    public static final DeferredItem<Item> RARE_SNIPERS_MONOCLE = ITEMS.register("rare_snipers_monocle",
            () -> new Item(new Item.Properties().
                    rarity(Rarity.RARE).
                    stacksTo(1)));
    public static final DeferredItem<Item> RARE_LAST_STAND = ITEMS.register("rare_last_stand",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_BLADEMASTERS_PROWESS = ITEMS.register("rare_blademasters_prowess",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_DEADEYE_GLASS = ITEMS.register("rare_deadeye_glass",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_REAPERS_SCYTHE = ITEMS.register("rare_reapers_scythe",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_ARCHITECTS_HASTE = ITEMS.register("rare_architects_haste",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_VITAL_CONVERSION_RING = ITEMS.register("rare_vital_conversion_ring",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_BOUNTY_TOTEM = ITEMS.register("rare_bounty_totem",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));
    public static final DeferredItem<Item> RARE_HUNGRY_BANQUET = ITEMS.register("rare_hungry_banquet",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)));

    public static final DeferredItem<Item> UNCOMMON_PEARL_EYE = ITEMS.register("uncommon_pearl_eye",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> UNCOMMON_SCENT_OF_COMPOST = ITEMS.register("uncommon_scent_of_compost",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)));
    public static final DeferredItem<Item> UNCOMMON_FERTILE_CLOD = ITEMS.register("uncommon_fertile_clod",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)));



    // --- パックアイテム ---
    public static final DeferredItem<Item> RANDOM_PACK = ITEMS.register("random_pack",
            () -> new RandomPackItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> RARE_PACK = ITEMS.register("rare_pack",
            () -> new RarePackItem(new Item.Properties()
                    .rarity(Rarity.RARE)));
    public static final DeferredItem<Item> EPIC_PACK = ITEMS.register("epic_pack",
            () -> new EpicPackItem(new Item.Properties()
                    .rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> LEGENDARY_PACK = ITEMS.register("legendary_pack",
            () -> new LegendaryPackItem(new GlintingItem.Properties()
                    .rarity(Rarity.COMMON)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
