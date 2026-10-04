package dev.galaxynoxus.xpore;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(XpOreMod.MOD_ID)
public final class XpOreMod {
    public static final String MOD_ID = "xpore";
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public XpOreMod(FMLJavaModLoadingContext context) {
        XpOreConfig.initialize();
        var bus = context.getModBusGroup();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModParticles.PARTICLES.register(bus);
        ModWorldGeneration.PLACEMENT_TYPES.register(bus);
        BuildCreativeModeTabContentsEvent.getBus(bus).addListener(ModItems::creativeTab);
    }
}
