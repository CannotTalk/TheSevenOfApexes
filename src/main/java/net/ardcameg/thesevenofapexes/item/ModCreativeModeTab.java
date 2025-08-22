package net.ardcameg.thesevenofapexes.item;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheSevenOfApexes.MOD_ID);

    public static final Supplier<CreativeModeTab> SEVEN_APEXES_TAB = CREATIVE_MODE_TAB.register("seven_apexes_item_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LEGENDARY_PRIDE.get()))
                    .title(Component.translatable("creativetab.seven_apexes.seven_apexes_item"))
                    .displayItems(((parameters, output) -> {
                        output.accept(ModItems.LEGENDARY_PRIDE);
                        output.accept(ModItems.LEGENDARY_ENVY);
                        output.accept(ModItems.LEGENDARY_WRATH);
                        output.accept(ModItems.LEGENDARY_SLOTH);
                        output.accept(ModItems.LEGENDARY_GREED);
                        output.accept(ModItems.LEGENDARY_GLUTTONY);
                        output.accept(ModItems.LEGENDARY_LUST);
                        output.accept(ModItems.LEGENDARY_SUNLIGHT_SACRED_SEAL);
                        output.accept(ModItems.LEGENDARY_MOONLIGHT_SACRED_SEAL);

                        output.accept(ModItems.EPIC_LIGHTNING_FIST);
                        output.accept(ModItems.EPIC_PHOENIX_FEATHER);
                        output.accept(ModItems.EPIC_LIFE_STEEL_STICK);
                        output.accept(ModItems.EPIC_BERSERKERS_DRAG);
                        output.accept(ModItems.EPIC_SHADOW_BIND_GLOVES);
                        output.accept(ModItems.EPIC_ARRIVAL_OF_REVIVAL);
                        output.accept(ModItems.EPIC_WALKING_ANATHEMA);
                        output.accept(ModItems.EPIC_SCARRED_GRAIL);
                        output.accept(ModItems.EPIC_VOID_MANTLE);
                        output.accept(ModItems.EPIC_STEEL_CLAWS);
                        output.accept(ModItems.EPIC_FIENDS_BARGAIN);
                        output.accept(ModItems.EPIC_REVERSAL_HOURGLASS);
                        output.accept(ModItems.EPIC_GOLIATHS_GAVEL);
                        output.accept(ModItems.EPIC_HEART_OF_STORM);

                        output.accept(ModItems.RARE_GUARDIANS_CREST);
                        output.accept(ModItems.RARE_GILLS_CHARM);
                        output.accept(ModItems.RARE_SNIPERS_MONOCLE);
                        output.accept(ModItems.RARE_LAST_STAND);
                        output.accept(ModItems.RARE_BLADEMASTERS_PROWESS);
                        output.accept(ModItems.RARE_DEADEYE_GLASS);
                        output.accept(ModItems.RARE_REAPERS_SCYTHE);
                        output.accept(ModItems.RARE_ARCHITECTS_HASTE);
                        output.accept(ModItems.RARE_VITAL_CONVERSION_RING);
                        output.accept(ModItems.RARE_BOUNTY_TOTEM);
                        output.accept(ModItems.RARE_HUNGRY_BANQUET);

                        output.accept(ModItems.UNCOMMON_PEARL_EYE);
                        output.accept(ModItems.UNCOMMON_SCENT_OF_COMPOST);
                        output.accept(ModItems.UNCOMMON_FERTILE_CLOD);
                        output.accept(ModItems.UNCOMMON_LUCKY_FLINT);
                        output.accept(ModItems.UNCOMMON_REDUNDANT_FLINT);
                        output.accept(ModItems.UNCOMMON_HEALING_LINENS);
                        output.accept(ModItems.UNCOMMON_SPIDERS_WARP);
                        output.accept(ModItems.UNCOMMON_SECRET_ART_OF_SEWING);

                        output.accept(ModItems.COMMON_SHINING_AURA);
                        output.accept(ModItems.COMMON_EMPERORS_NEW_CLOTHES);


                        output.accept(ModItems.RANDOM_PACK);
                        output.accept(ModItems.RARE_PACK);
                        output.accept(ModItems.EPIC_PACK);
                        output.accept(ModItems.LEGENDARY_PACK);
                    }))
                    .build());



    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
