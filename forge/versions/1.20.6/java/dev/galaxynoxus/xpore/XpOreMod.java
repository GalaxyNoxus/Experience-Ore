package dev.galaxynoxus.xpore;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(XpOreMod.MOD_ID)
public final class XpOreMod {
    public static final String MOD_ID = "xpore";
    public static ResourceLocation id(String path) { return new ResourceLocation(MOD_ID, path); }
    public XpOreMod() {
        var context = FMLJavaModLoadingContext.get();
        XpOreConfig.initialize();
        var bus = context.getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModParticles.PARTICLES.register(bus);
        ModWorldGeneration.PLACEMENT_TYPES.register(bus);
        bus.addListener(ModItems::creativeTab);
    }
}
