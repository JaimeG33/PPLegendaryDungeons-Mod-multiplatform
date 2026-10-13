# Compatibility

## Exact development target for v1.1.3

| Component | Supported baseline |
|---|---|
| Mod version | `1.1.3` |
| Minecraft | `1.21.1` |
| Java | `21` |
| Cobblemon | `1.8.2+1.21.1` |
| Architectury API | `13.0.8` |
| Fabric Loader | `0.17.3` |
| Fabric API | `0.116.7+1.21.1` |
| Fabric Language Kotlin | `1.13.7+kotlin.2.2.21` |
| NeoForge | `21.1.214` |
| Kotlin for Forge | `5.10.0` |
| Accessories | `1.1.0-beta.52+1.21.1` |
| Mega Showdown | `1.3.0+1.8.2+1.21.1-hotfix` |

Gradle pins Cobblemon `com.cobblemon:mod`, `com.cobblemon:fabric`, and
`com.cobblemon:neoforge` to `1.8.2+1.21.1`. Other toolchain/dependency versions
are unchanged from v1.1.2. Build and runtime verification results are recorded
separately in [the v1.1.3 validation report](testing/HOTFIX_1.1.3_VALIDATION.md).

## Allowed runtime ranges and recommended artifacts

| Dependency | Fabric range | NeoForge range |
|---|---|---|
| Cobblemon | `>=1.8.2 <1.9.0` | `[1.8.2,1.9.0)` |
| Mega Showdown | `>=1.3.0 <1.4.0` | `[1.3.0,1.4.0)` |

Compatible future Cobblemon 1.8.x and Mega Showdown 1.3.x patches may load
without republishing this mod. Being allowed by metadata is not a test result:
smoke-test each new patch before calling it officially tested. The dependency
mods' own requirements must also be satisfied.

Use the **October 12, 2026 Mega Showdown hotfix**, not the initial 1.3.0 build:

| Loader | Exact Modrinth Maven coordinate | Corresponding CurseForge file |
|---|---|---|
| Fabric | `maven.modrinth:SszvX85I:EAunp9Kz` | [9146466](https://www.curseforge.com/minecraft/mc-mods/cobblemon-mega-showdown/files/9146466) |
| NeoForge | `maven.modrinth:SszvX85I:C0yyAEcs` | [9146465](https://www.curseforge.com/minecraft/mc-mods/cobblemon-mega-showdown/files/9146465) |

Both embed `1.3.0+1.8.2+1.21.1-hotfix`. Fabric treats the suffix after `+`
as build metadata, so the requested numeric range cannot require this particular
hotfix. Keep the flexible ranges and recommend these exact downloads; no custom
version checker is installed. Development dependencies remain pinned by version
ID. Cobblemon and Mega Showdown are not bundled into our production JARs.

## Loader support

The project publishes separate artifacts:

```text
Fabric:
cobblemon-eld-1.1.3-fabric-mc1.21.1-cob1.8.2.jar

NeoForge:
cobblemon-eld-1.1.3-neoforge-mc1.21.1-cob1.8.2.jar
```

Use the JAR that matches the installed loader. Do not place both project JARs in
the same instance.

Shared gameplay and resources are built from the common module. Loader-specific
modules provide metadata, dependencies, entrypoints, and minimal adapters.

## Cobblemon APIs used directly

The shared code uses Cobblemon APIs including Pokémon properties and entities,
battle-faint and loot-drop events, ownership/completion state, and shared
registries.

Any future Cobblemon update should be tested for:

- Changed event payloads or timing.
- Renamed or removed entity/property APIs.
- Ownership accessor changes.
- Loot event cancellation or ordering.
- Battle participant and killer attribution.
- Changes to published Fabric and NeoForge artifacts.

Do not broaden the declared Cobblemon range merely because the game starts.
Compile and smoke-test the exact target before changing metadata.

## Known caveats

### Paused encounter behavior work

The v1.1.3 hotfix preserves v1.1.2 gameplay code. Faction/vanilla-mob encounter
work is experimental and paused; existing pursuit, targeting, and native attack
limitations are not fixed by this release. Shared provocation and raid expansion
remain unfinished. Do not describe dependency migration as an AI improvement.

### Multiplayer battle attribution

Some cooperative or multi-participant battle paths may require more precise
responsible-player attribution than selecting the first battle participant.
This remains a known hardening area.

### Modded drop cancellation

Another addon can cancel Cobblemon’s loot event. Depending on event ordering,
the additional Minecraft loot table may also be skipped. This project should
not remove or replace another addon’s drops.

### GUI scale

The zone editor may overflow at GUI scale 4. This is a known non-blocking UI
issue unless one loader behaves differently.

### Crystal Caves

Crystal Caves remains intentionally dormant. Source assets may exist, but the
dungeon should not be actively registered, mapped, or advertised until its
release work resumes.

## Compatibility confidence

A release is considered smoke-tested only after:

- The remapped Fabric JAR launches in a clean Fabric instance.
- The remapped NeoForge JAR launches in a clean NeoForge instance.
- Fresh worlds open on both.
- Dedicated servers start on both.
- Representative controllers and Rayquaza flows work.
- Metadata displays the intended version and dependencies.
- No blocking resource, registry, mixin, or datapack error appears.

Extended multiplayer, existing-world migration, dependency-floor, and
performance testing may remain documented as deferred rather than being
represented as passed.
