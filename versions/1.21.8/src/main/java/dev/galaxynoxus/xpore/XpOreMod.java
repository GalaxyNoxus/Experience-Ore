package dev.galaxynoxus.xpore;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public final class XpOreMod implements ModInitializer {
    public static final String MOD_ID = "xpore";
    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    @Override
    public void onInitialize() {
        
        XpOreConfig.initialize();
        ModBlocks.register();
        ModItems.register();
        ModParticles.register();
        ModWorldGeneration.register();
    }
}
