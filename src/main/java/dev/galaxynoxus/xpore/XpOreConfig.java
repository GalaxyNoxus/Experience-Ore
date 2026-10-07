package dev.galaxynoxus.xpore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class XpOreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("xpore/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static XpOreConfig instance = new XpOreConfig();

    private int minXpDrop = 8;
    private int maxXpDrop = 13;
    private double fortuneMultiplier = 1.5;
    
    private int oreGenerationRarity = 6;
    private int caveSurfaceRarity = 16;
    private boolean enableDeepslateVariant = true;

    public static XpOreConfig get() { return instance; }
    public static void initialize() {
        instance = load(FabricLoader.getInstance().getConfigDir());
    }

    public static XpOreConfig load(Path configDir) {
        Path file = configDir.resolve("xpore.json");
        XpOreConfig config = new XpOreConfig();
        JsonObject contents = null;
        boolean writeBack = !Files.exists(file);
        if (!writeBack) {
            try {
                JsonElement parsed;
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    parsed = JsonParser.parseReader(reader);
                }
                if (!parsed.isJsonObject()) {
                    throw new IllegalArgumentException("A raiz de xpore.json precisa ser um objeto JSON.");
                }
                contents = parsed.getAsJsonObject();
                config.caveSurfaceRarity = readInt(contents, "caveSurfaceRarity", config.caveSurfaceRarity);
                config.minXpDrop = readInt(contents, "minXpDrop", config.minXpDrop);
                config.maxXpDrop = readInt(contents, "maxXpDrop", config.maxXpDrop);
                config.oreGenerationRarity = readInt(contents, "oreGenerationRarity", config.oreGenerationRarity);
                if (contents.has("fortuneMultiplier")) {
                    JsonElement v = contents.get("fortuneMultiplier");
                    requireNumber(v, "fortuneMultiplier");
                    config.fortuneMultiplier = v.getAsDouble();
                }
                if (contents.has("enableDeepslateVariant")) {
                    JsonElement v = contents.get("enableDeepslateVariant");
                    if (!v.isJsonPrimitive() || !v.getAsJsonPrimitive().isBoolean()) {
                        throw new IllegalArgumentException("enableDeepslateVariant precisa ser booleano.");
                    }
                    config.enableDeepslateVariant = v.getAsBoolean();
                }
                writeBack = config.normalize();
                JsonObject normalized = GSON.toJsonTree(config).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : normalized.entrySet()) {
                    if (!contents.has(entry.getKey())) { writeBack = true; }
                    contents.add(entry.getKey(), entry.getValue());
                }
            } catch (IOException | RuntimeException error) {
                LOGGER.error("Config inválida/ilegível em {}. Usando defaults.", file, error);
                config = new XpOreConfig();
                contents = null;
                
                Path backup = configDir.resolve("xpore.json.invalid-" + System.currentTimeMillis() + ".bak");
                try {
                    Files.copy(file, backup);
                    writeBack = true;
                    LOGGER.warn("Config original preservada em {}", backup);
                } catch (IOException backupError) {
                    writeBack = false;
                    LOGGER.error("Backup falhou; arquivo original mantido intacto.", backupError);
                }
            }
        }
        if (writeBack) {
            try { save(file, contents != null ? contents : GSON.toJsonTree(config).getAsJsonObject()); }
            catch (IOException error) {
                LOGGER.error("Não foi possível salvar {}. Valores carregados continuam em memória.", file, error);
            }
        }
        return config;
    }

    private static void requireNumber(JsonElement value, String key) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(key + " precisa ser um número.");
        }
    }
    private static int readInt(JsonObject object, String key, int fallback) {
        if (!object.has(key)) { return fallback; }
        JsonElement value = object.get(key);
        requireNumber(value, key);
        return value.getAsBigDecimal().intValueExact();
    }
    private boolean normalize() {
        int oldMin = minXpDrop, oldMax = maxXpDrop, oldRarity = oreGenerationRarity;
        int oldCaveRarity = caveSurfaceRarity;
        caveSurfaceRarity = Math.max(0, Math.min(10_000, caveSurfaceRarity));
        double oldMultiplier = fortuneMultiplier;
        minXpDrop = Math.max(0, Math.min(10_000, minXpDrop));
        maxXpDrop = Math.max(minXpDrop, Math.min(10_000, maxXpDrop));
        oreGenerationRarity = Math.max(1, Math.min(10_000, oreGenerationRarity));
        fortuneMultiplier = Double.isFinite(fortuneMultiplier)
                ? Math.max(1.0, Math.min(10.0, fortuneMultiplier)) : 1.5;
        boolean changed = oldCaveRarity != caveSurfaceRarity || oldMin != minXpDrop || oldMax != maxXpDrop
                || oldRarity != oreGenerationRarity || oldMultiplier != fortuneMultiplier;
        if (changed) { LOGGER.warn("Valores fora dos limites de xpore.json foram corrigidos."); }
        return changed;
    }
    private static void save(Path file, JsonObject contents) throws IOException {
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "xpore-", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(contents) + System.lineSeparator(), StandardCharsets.UTF_8);
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }
    public int minXpDrop() { return minXpDrop; }
    public int maxXpDrop() { return maxXpDrop; }
    public int caveSurfaceRarity() { return caveSurfaceRarity; }
    public int oreGenerationRarity() { return oreGenerationRarity; }
    public boolean enableDeepslateVariant() { return enableDeepslateVariant; }

    public int applyFortune(int baseXp, int level) {
        if (baseXp <= 0) { return 0; } 
        double result = baseXp * Math.pow(fortuneMultiplier, Math.max(0, level));
        return (int) Math.min(1_000_000.0, Math.floor(result));
    }
}
