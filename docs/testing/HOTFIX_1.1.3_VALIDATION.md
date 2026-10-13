# v1.1.3 compatibility validation

Review date: October 13, 2026. Baseline: `3928203ec63947ee756b0d34024251d9d755b2a1`
on `architectury-multiloader-migration` (v1.1.2). The working tree was clean before
the hotfix edits. The named GitHub repository's branch matched this baseline.
The local `origin` still uses the historical Fabric repository URL; it was not
rewritten. No publication is performed by this task.

## Exact dependencies

Cobblemon: `com.cobblemon:mod:1.8.2+1.21.1`,
`com.cobblemon:fabric:1.8.2+1.21.1`, and
`com.cobblemon:neoforge:1.8.2+1.21.1`, from the official
[artifact repository](https://artefacts.cobblemon.com/releases/com/cobblemon/).

Mega Showdown: `1.3.0+1.8.2+1.21.1-hotfix` on both loaders.

| Loader | Modrinth version | CurseForge file | Downloaded JAR SHA-256 |
|---|---|---|---|
| Fabric | [EAunp9Kz](https://modrinth.com/mod/cobblemon-mega-showdown/version/EAunp9Kz) | [9146466](https://www.curseforge.com/minecraft/mc-mods/cobblemon-mega-showdown/files/9146466) | `933202511fb5d15f72527ab9cd5db5fa3a7c8a7c15ae4b14c80fa768e57b38bf` |
| NeoForge | [C0yyAEcs](https://modrinth.com/mod/cobblemon-mega-showdown/version/C0yyAEcs) | [9146465](https://www.curseforge.com/minecraft/mc-mods/cobblemon-mega-showdown/files/9146465) | `73682f424ca00e797e06611688b952368fea6c4ef674ebc5d8157716e7c10f5a` |

Modrinth API records identify the October 12 hotfix and correct loaders. Download
SHA-512 values matched the API records. Both Modrinth JARs were also
byte-identical by SHA-256 to the corresponding CurseForge CDN downloads. Embedded metadata confirms the full
hotfix version and loader entrypoints. The exact IDs remain Gradle runtime-only
dependencies; no Curse Maven fallback or repository change was needed.

The requested ranges were evaluated with Fabric Loader 0.17.3's
`VersionPredicate` and Maven `VersionRange`/`DefaultArtifactVersion` (the
NeoForge version representation). Both accept `1.8.2+1.21.1` and
`1.3.0+1.8.2+1.21.1-hotfix` within their respective requested ranges.
This is a version-parser check, separate from actual loader startup.

## Source and resource audit

Compared official shared Cobblemon 1.8.1 and 1.8.2 source artifacts:

- `PokemonProperties`, `CobblemonEntities`, the save/load/capture event types,
  `BattleFaintedEvent`, `LootDroppedEvent`, `Statuses`, and `PersistentStatus`
  are unchanged. Reviewed changes in imported `Pokemon`, `PokemonEntity`, and
  `CobblemonEvents` do not demonstrate a broken API used by this mod.
- `PokemonServerDelegate` is unchanged. It still removes a dying entity when
  `deathTime == 60`, supporting the existing callback at 59. Death effects may
  pause death-time progression; this is not a promise of exactly 59 elapsed
  server ticks. In-game callback validation remains required.
- `FaintInstruction`, `DropTable`, and the capture-event producer are unchanged
  between these versions. The added `BATTLE_ENDED` event does not replace the
  existing subscribed events.
- `MoveToAttackTargetTask` is unchanged. This does not prove that experimental
  pursuit works; existing aggression code and its known limitations are retained.
- The manual entity-construction path in `PokemonSpawnService` is retained.
  `createEntity` additionally applies custom entity properties, but that behavior
  is already present in 1.8.1. Switching APIs is deferred rather than presented
  as a demonstrated 1.8.2 regression fix.
- `TMMoveComponent`, the component registration, and `TechnicalMachineItem` are
  unchanged. All 19 distinct moves referenced by the 12 tower TM reward tables
  have native `data/cobblemon/tms/<move>.json` definitions in 1.8.2. The v1.1.2
  native-TM conversion is preserved.
- Referenced Cobblemon `fishing_boat` / `shipwreck_cove` and Mega Showdown
  `mega_site` / `megaroid` / `wishing_weald` structure resources exist in the
  target artifacts. Mega Showdown item/model evidence exists for the current
  orb, mega stone, blank Z, meteorite, mushroom, and Zygarde-cell IDs.
  Optional legacy aliases in item tags remain optional; old function references
  are not evidence that those aliases are registered by the current dependency.

No gameplay Java or content changes were justified by this source comparison.
Runtime checks can still expose behavior or addon-mixin problems that static
inspection does not establish.

## Build and execution results

- Full clean common/Fabric/NeoForge build: **passed**, 29 tasks executed.
- All three Gradle test tasks: **NO-SOURCE**; this repository has no automated
  test sources. Do not report this as a passing gameplay test suite.
- All 405 shared resource JSON files parse successfully.
- Both production JARs: ZIP integrity, embedded v1.1.3 metadata, requested
  dependency ranges, common entrypoint/mixins and all 657 shared resources checked.
  Data/assets match the source bytes. No bundled Cobblemon/Mega Showdown classes
  or dependency JARs; NeoForge contains no Fabric mod metadata.
- Build warnings: existing deprecated API usage in `RayquazaCompletionCondition`,
  Gradle 9 deprecations, and an upstream remapping warning for `BeeGrowCropGoal`.
  These did not fail the build.
- Validation work has stopped at the maintainer's request. The implementation
  and build preparation are complete; gameplay and publication sign-off belong
  to the maintainer.
- The isolated Fabric development server reached `Done`, completed a resource
  reload, loaded 13 spawn profiles and 13 dungeon profiles, and processed `stop`
  through saving all dimensions. The remaining launcher processes were stopped
  when the maintainer requested that testing cease. This was a development
  launch, not a clean installation test of the production JAR.
- NeoForge runtime, client rendering, and the manual gameplay scenarios below
  were not tested. Both loader builds passed as recorded above.

The first Fabric development launch stalled because Cobblemon's battle engine
could not load `com.ibm.icu.text.DateFormat`. The successful isolated launch
added the existing ICU 71.1 library to that test task's classpath. Cobblemon's
normal Fabric artifact already bundles ICU 71.1. This workaround exists only in
the ignored `.gradle/hotfix-1.1.3/smoke.init.gradle` file, explicitly selected
with `--init-script`; no project dependency, normal run configuration, or release
JAR was changed to include it. Ordinary development commands do not load it.
The ordinary Fabric development server's classpath issue remains unresolved.

The successful launch also logged upstream optional-mixin/data-fixer warnings
and `Invalid item id: null` warnings from the mod's older trader-generator smoke
helper. No changes were made to suppress them. These observations are not a
claim that every trader or encounter has been verified in-game.

The initial Gradle attempt could not resolve Loom because this machine's default
Java trust store rejected the repository certificate (`PKIX path building failed`).
A Java HTTPS probe succeeded with the existing Windows trust store. Subsequent
commands use process-local settings, without disabling certificate verification
or changing global trust/project configuration:

```powershell
$env:JAVA_TOOL_OPTIONS = '-Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NONE'
.\gradlew.bat clean :common:build :fabric:build :neoforge:build --no-daemon --no-parallel --max-workers=2 --console=plain
```

Local evidence is under `.gradle/hotfix-1.1.3/` (ignored, not release content).

## Manual gameplay checks before publication

Use separate Fabric and NeoForge test instances with the exact recommended
dependencies and final remapped JARs. Use fresh worlds or copies, never original
player saves. Follow [the existing matrix](DUNGEON_POKEMON_TEST_MATRIX.md):

1. Trigger normal and secret Rayquaza encounters; check the profile, one-time
   locks, aftermath, completion and removal of dungeon restrictions.
2. Spawn a dungeon Pokémon marker; verify one spawn, home/instance metadata,
   chunk unload/reload, save/restart, allowed capture and uncatchable profiles.
3. Compare targeting/busy/battle behavior with v1.1.2. Record preexisting movement
   failures separately; do not expand AI scope to fix them during this hotfix.
4. Test opted-in Wind Charged/Oozing effects after both direct kills and battle
   defeats, including a long battle where the visible effect expires. Expect one
   callback near deathTime 59, none on capture/unload, and correct supplemental or
   replacement loot. Check both `dropAfterDeathAnimation` settings.
5. Roll native TM rewards and verify move components and usable moves in-game.
6. Resolve Sky Pillar and all three Mega Showdown maps, claim a structure map,
   and exercise custom/natural traders and cartographers.
7. Run `/reload`; check controller editing/protection, save/restart, Crystal
   Heart rendering/light/mining/self-drop NBT, and logs for missing registry or
   resource errors. Repeat relevant multiplayer cases with two players.

Future permitted dependency patches need their own smoke-test record. They are
not verified by the v1.1.3 preparation results.

## Production artifacts

| Loader | Relative path | SHA-256 |
|---|---|---|
| fabric | `fabric/build/libs/cobblemon-eld-1.1.3-fabric-mc1.21.1-cob1.8.2.jar` | `2ea71bd86b00ab96067e15b7460587be85e3886346d728e2f836efa0b4281300` |
| neoforge | `neoforge/build/libs/cobblemon-eld-1.1.3-neoforge-mc1.21.1-cob1.8.2.jar` | `04e4cbb8362d7f8e9c38801a7a6eedce34309584647ee798c642de7be708b270` |

These are built candidates; publication readiness depends on the runtime and
manual checks above. No files have been uploaded or published.
