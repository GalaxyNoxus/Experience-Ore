# Experience Ore

A Fabric and Forge mod that adds experience ores with green crystal shards, orbiting particles, and configurable XP rewards.

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

Mod version: **1.0.0**. Fabric requires Fabric API. Forge builds do not require Fabric API.

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

## Building

Use JDK 25 to run Gradle, with JDK 17 and JDK 21 available for older targets. Set `JAVA_HOME` to JDK 25. Gradle can download missing toolchains through Foojay. Build scripts require Python 3.11 or newer.

Build both loaders and prepare a release from the repository root:

```powershell
python tools/build_all.py
```

Build only one target:

```powershell
python tools/build_all.py --loader fabric --minecraft 1.20.1
python tools/build_all.py --loader forge --minecraft 1.20.1
```

Direct Gradle commands on Windows:

```powershell
.\gradlew.bat :1.20.1:buildAndCollect
.\forge\gradlew.bat -p forge -PminecraftVersion=1.20.1 buildAndCollect
```

On Linux/macOS, use `bash ./gradlew` or `bash ./forge/gradlew` with the same arguments.

Fabric uses Stonecutter. Forge uses the separate `forge/` Gradle project. Both loaders share the original assets and orbital math. Forge resource metadata declares the client resource and server data formats for each target. Forge targets through 1.20.4 include SRG remapping for production; `buildAndCollect` collects the installable output.

Raw installable JARs are in `build/raw/<loader>/<minecraft>/`. Complete releases are in `build/release/`, together with `release-manifest.json`.

## Sharing JARs across Minecraft versions

The release script compares actual compiled classes, resources, and runtime metadata for each loader. It merges identical builds and declares their exact supported versions. Differences keep builds separate. Fabric and Forge always use separate JARs.

Every target still compiles during validation. Consolidation reduces downloadable files, while preserving per-version API checks. Only build metadata and ZIP timestamps are excluded from comparison.

Previous Fabric compilation checks identified two byte-identical groups for the 1.20 series:

- **1.20–1.20.4**: one Java 17 JAR.
- **1.20.5–1.20.6**: one separate Java 21 JAR.

Forge 1.20 and 1.20.1 also produced identical remapped classes and resources in earlier compilation checks.

The pipeline recalculates groups after each build. `release-manifest.json` lists the resulting Forge and Fabric groups. Compilation and byte identity do not replace in-game checks of world generation, mining, particles, and shaders.

Package an already completed set of raw builds:

```sh
python tools/prepare_release.py --input build/raw --output build/release
```

The output directory must be empty. For another full build, use `python tools/build_all.py --output build/release-next`.

## Validation status

The full 46-target matrix still requires a successful GitHub Actions run. Earlier local checks compiled Fabric 1.20–1.20.6 and 1.21–1.21.4, and Forge 1.20, 1.20.1, 1.20.3, 1.20.4 and 26.3. These checks preceded the final resource metadata update; in-game testing remains pending.

## GitHub Actions and Modrinth

Keep everything in the same repository. Pushes and pull requests build both loaders. Once all builds succeed, the **xpore-release** artifact contains the consolidated JARs. Individual `xpore-raw-*` artifacts contain per-target outputs.

For automatic Modrinth publishing, add the repository Actions secret `MODRINTH_TOKEN`, with permission to create versions on the `xp-ore` project. Push a tag matching `mod_version` in `gradle.properties`, such as `v1.0.0`. Publishing runs only on matching tag pushes in `GalaxyNoxus/experience-ore`; ordinary branch pushes only build.

Grouped uploads use a short deterministic version identifier to respect Modrinth limits; filenames and metadata retain the supported Minecraft versions.

The publisher checks hashes, loader metadata, version coverage, and existing uploads before sending files. Each upload declares its exact Minecraft versions and loader. Fabric API is required only for Fabric uploads. Identical existing versions are skipped; conflicting files stop publication.

## Shaders

Both loaders use the original crystal textures and emission maps. The port does not make the stone base emissive. Crystal glow and bloom require a compatible shader pack and rendering setup.

## License

Project code and original textures are licensed under the **MIT License**.

The Gradle Wrapper is distributed under the **Apache License 2.0**. Its license and notices are included separately.
