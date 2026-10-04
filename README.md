# Experience Ore

A Fabric and Forge mod that adds experience ores with configurable XP rewards.

- **Experience Ore** — stone variant found between Y 0 and 40.
- **Deepslate Experience Ore** — tougher variant found between Y -20 and 0.
- Emissive crystal textures for compatible shaders.
- Configurable XP drops and world generation.

The ores generate in ordinary veins. They do not grow crystals or use a geode system.

## Mining

An **iron pickaxe or better** is required.

| Enchantment | Result |
| --- | --- |
| None | Drops 3–8 XP by default, without an item |
| Fortune | Increases the XP reward |
| Silk Touch | Drops the ore block instead of XP |

Fortune uses the following formula:

```text
XP = floor(base XP × fortuneMultiplier ^ Fortune level)
```

## Configuration

The mod creates `config/xpore.json` on its first launch:

```json
{
  "minXpDrop": 3,
  "maxXpDrop": 8,
  "fortuneMultiplier": 1.5,
  "oreGenerationRarity": 2,
  "enableDeepslateVariant": true
}
```

| Option | Description |
| --- | --- |
| `minXpDrop` | Minimum base XP reward |
| `maxXpDrop` | Maximum base XP reward |
| `fortuneMultiplier` | XP multiplier applied for each Fortune level |
| `oreGenerationRarity` | Higher values make generation rarer |
| `enableDeepslateVariant` | Enables natural generation of the deepslate variant |

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
