# Release Checklist — 1.1.3

## Publication scope

The project publishes release files through:

- Modrinth
- CurseForge

A GitHub Release is not required.

Each platform receives two separate files:

```text
Fabric:
fabric/build/libs/cobblemon-eld-1.1.3-fabric-mc1.21.1-cob1.8.2.jar

NeoForge:
neoforge/build/libs/cobblemon-eld-1.1.3-neoforge-mc1.21.1-cob1.8.2.jar
```

## 1. Confirm source state

From the repository root:

```powershell
git status --short
git branch --show-current
git log -1 --oneline
```

Review all changes before the final build and record the base commit plus any
uncommitted release patch. Do not discard unrelated work to make the tree clean.

Confirm in `gradle.properties`:

```properties
mod_version=1.1.3
minecraft_version=1.21.1
cobblemon_version=1.8.2+1.21.1
```

Use [COMPATIBILITY.md](COMPATIBILITY.md) for the exact development pins and
allowed runtime ranges. Record completed checks in
[HOTFIX_1.1.3_VALIDATION.md](testing/HOTFIX_1.1.3_VALIDATION.md); this checklist
is not evidence that tests have passed. No automated publication is part of the
hotfix preparation.

## 2. Confirm dormant content remains dormant

- Crystal Caves has no active structure registration.
- No active random-map entry points to Crystal Caves.
- No active tick function calls the retired voucher-map path.
- Public descriptions do not advertise Crystal Caves as released content.

The dormant templates and functions may remain in the common source tree when
nothing active calls or registers them.

## 3. Clean build

Run:

```powershell
.\gradlew.bat `
    clean `
    :common:build `
    :fabric:build `
    :neoforge:build `
    --no-daemon `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

Require:

```text
BUILD SUCCESSFUL
```

## 4. Select the correct files

Upload only:

```text
fabric/build/libs/cobblemon-eld-1.1.3-fabric-mc1.21.1-cob1.8.2.jar
neoforge/build/libs/cobblemon-eld-1.1.3-neoforge-mc1.21.1-cob1.8.2.jar
```

Do not upload:

```text
common/build/libs/*
*-sources.jar
*-dev-slim.jar
*-dev-shadow.jar
```

## 5. Inspect JAR contents

Fabric must contain:

```text
fabric.mod.json
icon.png
pp_legendarydungeons.mixins.json
data/pp_legendarydungeons/
porker/pp_legendarydungeons/
```

NeoForge must contain:

```text
META-INF/neoforge.mods.toml
icon.png
pp_legendarydungeons.mixins.json
data/pp_legendarydungeons/
porker/pp_legendarydungeons/
```

NeoForge should not contain `fabric.mod.json`.

Confirm both processed metadata files contain version `1.1.3`, not
`${version}`. Confirm Cobblemon ranges are `>=1.8.2 <1.9.0` / `[1.8.2,1.9.0)`
and Mega Showdown ranges are `>=1.3.0 <1.4.0` / `[1.3.0,1.4.0)`. Check them
against the full embedded dependency versions, not only their filenames.

## 6. Test the exact release JARs

Use clean launcher instances rather than only Gradle `runClient`.

Fabric instance:

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Fabric Language Kotlin
- Architectury API
- Cobblemon 1.8.2
- Mega Showdown 1.3.0+1.8.2+1.21.1-hotfix (October 12, 2026)
- Required transitive dependencies

NeoForge instance:

- Minecraft 1.21.1
- NeoForge 21.1.x
- Architectury API
- Cobblemon 1.8.2
- Mega Showdown 1.3.0+1.8.2+1.21.1-hotfix (October 12, 2026)
- Required transitive dependencies

Minimum checks on both:

- Title screen loads.
- Mod name, version, description, and icon are correct.
- Fresh world opens.
- `/reload` completes without a blocking error.
- Parent and zone controller blocks exist.
- Controller screens open.
- Sky Pillar can generate or be located.
- Normal Rayquaza summon works.
- Secret/alternate Rayquaza summon works.
- Dungeon Pokémon spawn, capture, unload/reload, and save/restart preserve the
  expected encounter lifecycle.
- Direct and battle defeats execute opted-in death callbacks once near death
  tick 59; capture/unload do not execute them.
- Additional/replacement loot and native TM rewards work.
- WTrader, Sky Pillar and Mega Showdown maps resolve correctly.
- Crystal Heart rendering, configuration and self-drop preservation work.
- Existing aggression is compared with v1.1.2; preexisting failures are recorded,
  not repaired as part of the hotfix.
- Dedicated servers start on both loaders.
- World saves and reopens.
- `latest.log` has no blocking error from `pp_legendarydungeons`.

Use [DUNGEON_POKEMON_TEST_MATRIX.md](testing/DUNGEON_POKEMON_TEST_MATRIX.md)
for detailed regression scenarios.

## 7. Optional existing-world smoke check

Use copies only.

- Open a Fabric test-world copy on Fabric.
- Open a NeoForge test-world copy on NeoForge.
- Confirm controllers, links, saved values, scoreboards, functions, and Sky
  Pillar content remain present.
- Do not represent Fabric-to-NeoForge world migration as certified unless it was
  separately tested.

## 8. Prepare platform metadata

For both Modrinth and CurseForge:

- Version number: `1.1.3`
- Minecraft version: `1.21.1`
- Select the correct loader for each file.
- Mark the required dependencies rather than bundling them.
- Use the same release notes and known-limitations summary.
- Upload the Fabric and NeoForge JARs as separate loader files.

Recommended dependency summary:

```text
Required:
- Cobblemon 1.8.2
- Architectury API
- Mega Showdown 1.3.0+1.8.2+1.21.1-hotfix (October 12, 2026)

Fabric file also requires:
- Fabric API
- Fabric Language Kotlin

NeoForge file requires:
- NeoForge 21.1.x
- Kotlin for Forge when required by the installed dependency set
```

Check each platform’s dependency selector before publishing. Do not rely only on
text in the description.

## 9. Release notes

Start with [the v1.1.3 release notes](releases/1.1.3.md). Include:

- Architectury-based Fabric and NeoForge support.
- Current released dungeons and systems.
- Minecraft and Cobblemon versions.
- Required dependencies.
- Crystal Caves intentionally disabled.
- GUI scale 4 editor limitation.
- Any deferred multiplayer or compatibility tests that materially affect users.

Do not claim exhaustive validation when only smoke testing was performed.

## 10. Publish order

1. Upload both files as drafts when the platform supports drafts.
2. Recheck filename, loader, Minecraft version, dependencies, and release notes.
3. Publish the Fabric file.
4. Publish the NeoForge file.
5. Download each published file once and compare its filename and size with the
   locally tested artifact.
6. Start one clean instance with each downloaded file when practical.

## 11. Documentation closure

Update:

```text
docs/testing/HOTFIX_1.1.3_VALIDATION.md
docs/releases/1.1.3.md
```

Record the exact build commit, artifact filenames, platform project version,
publication date, and any issue discovered during upload or post-upload smoke
testing.
