# Experience Ore

A Fabric and Forge mod that adds experience ores with configurable XP rewards.

- **Experience Ore** — stone variant found between Y 0 and 40.
- **Deepslate Experience Ore** — tougher variant found between Y -20 and 0.
- Emissive crystal textures for compatible shaders.
- Configurable XP drops and world generation.

The ores generate in ordinary veins, with additional generation beside air on cave surfaces. They do not grow crystals or use a geode system.

## Mining

An **iron pickaxe or better** is required.

| Enchantment | Result |
| --- | --- |
| None | Drops 8–13 XP by default, without an item |
| Fortune | Increases the XP reward |
| Silk Touch | Drops the ore block instead of XP |

Fortune uses the following formula:

```text
XP = floor(base XP × fortuneMultiplier ^ Fortune level)
```

## Configuration

The mod creates `config/xpore.json` on its first launch. Existing configuration values are preserved when updating; change them manually to use the defaults below. Restart the game or server after editing. Generation changes only affect newly generated chunks.

```json
{
  "minXpDrop": 8,
  "maxXpDrop": 13,
  "fortuneMultiplier": 1.5,
  "oreGenerationRarity": 6,
  "enableDeepslateVariant": true,
  "caveSurfaceRarity": 16
}
```

| Option | Description |
| --- | --- |
| `minXpDrop` | Minimum base XP reward |
| `maxXpDrop` | Maximum base XP reward |
| `fortuneMultiplier` | XP multiplier applied for each Fortune level |
| `oreGenerationRarity` | Normal vein generation has a 1-in-N chance per attempt; higher values make veins rarer |
| `enableDeepslateVariant` | Enables natural generation of the deepslate variant, including cave surfaces |
| `caveSurfaceRarity` | Extra cave surface generation has a 1-in-N chance per eligible position; higher values make exposed ores rarer, and 0 disables this extra generation |

## Minecraft support

| Minecraft | Fabric | Forge | Game Java version |
| --- | --- | --- | --- |
| 1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4 | Yes | Yes | 17 |
| 1.20.5 | Yes | No Forge release available | 21 |
| 1.20.6 | Yes | Yes | 21 |
| 1.21, 1.21.1 | Yes | Yes | 21 |
| 1.21.2 | Yes | No Forge release available | 21 |
| 1.21.3–1.21.11 | Yes | Yes | 21 |
| 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | Yes | Yes | 25 |

`targets.json` lists the 24 Fabric targets. `forge-targets.json` pins the 22 Forge targets. This repository does not build NeoForge artifacts.

## Shaders

Both loaders use the original crystal textures and emission maps. The port does not make the stone base emissive. Crystal glow and bloom require a compatible shader pack and rendering setup.

## License

Project code and original textures are licensed under the **MIT License**.

The Gradle Wrapper is distributed under the **Apache License 2.0**. Its license and notices are included separately.
