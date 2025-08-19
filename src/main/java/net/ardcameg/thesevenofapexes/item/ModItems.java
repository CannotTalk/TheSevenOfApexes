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


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
