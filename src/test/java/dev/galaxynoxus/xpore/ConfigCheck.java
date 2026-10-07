package dev.galaxynoxus.xpore;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class ConfigCheck {
    private static int assertions;
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("xpore-config-check-");
        try {
            Path defaults = root.resolve("defaults");
            XpOreConfig config = XpOreConfig.load(defaults);
            check(Files.exists(defaults.resolve("xpore.json")), "Cria arquivo ausente");
            check(config.minXpDrop() == 8 && config.maxXpDrop() == 13, "Defaults de XP");
            check(config.oreGenerationRarity() == 6, "Default de raridade");
            check(config.caveSurfaceRarity() == 16, "Default de raridade nas cavernas");
            check(config.enableDeepslateVariant(), "Ardósia habilitada por padrão");
            check(config.applyFortune(8, 0) == 8, "Sem Fortuna preserva base");
            check(config.applyFortune(3, 1) == 4, "Fortuna arredonda para baixo");
            check(config.applyFortune(8, 2) == 18, "Fortuna II");
            check(config.applyFortune(8, 3) == 27, "Fortuna III exponencial");
            check(config.applyFortune(0, Integer.MAX_VALUE) == 0, "Zero não gera NaN/XP");
            check(config.applyFortune(10_000, Integer.MAX_VALUE) == 1_000_000, "Sem overflow");

            Path file = defaults.resolve("xpore.json");
            Files.writeString(file, "{\"minXpDrop\":10,\"maxXpDrop\":10,\"fortuneMultiplier\":2.0,"
                    + "\"oreGenerationRarity\":8,\"enableDeepslateVariant\":false}");
            config = XpOreConfig.load(defaults);
            check(config.minXpDrop() == 10 && config.maxXpDrop() == 10, "Faixa fixa permitida");
            check(config.applyFortune(10, 3) == 80, "Multiplicador configurado");
            check(config.oreGenerationRarity() == 8, "Raridade configurada");
            check(!config.enableDeepslateVariant(), "Desativação da variante");

            Files.writeString(file, "{\"minXpDrop\":20,\"maxXpDrop\":2,\"oreGenerationRarity\":0,"
                    + "\"fortuneMultiplier\":0.5,\"customNote\":\"preservar\"}");
            config = XpOreConfig.load(defaults);
            check(config.maxXpDrop() == 20, "Max não pode ficar abaixo de min");
            check(config.oreGenerationRarity() == 1, "Raridade não pode ser zero");
            check(config.applyFortune(20, 3) == 20, "Multiplicador inválido normalizado para 1");
            var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            check(json.get("enableDeepslateVariant").getAsBoolean(), "Campo faltante persistido");
            check(json.get("maxXpDrop").getAsInt() == 20, "Correção persistida");
            check(json.get("customNote").getAsString().equals("preservar"), "Campo extra preservado");

            String invalid = "{\"minXpDrop\":null}";
            Files.writeString(file, invalid);
            config = XpOreConfig.load(defaults);
            check(config.minXpDrop() == 8, "Arquivo inválido usa defaults");
            try (var paths = Files.list(defaults)) {
                Path backup = paths.filter(p -> p.toString().endsWith(".bak")).findFirst().orElseThrow();
                check(Files.readString(backup).equals(invalid), "Backup preserva original inválido");
            }
            check(JsonParser.parseString(Files.readString(file)).getAsJsonObject()
                    .get("minXpDrop").getAsInt() == 8, "Defaults restaurados no disco");
            System.out.println("ConfigCheck: " + assertions + " verificações passaram.");
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) { Files.deleteIfExists(p); }
            }
        }
    }
}
