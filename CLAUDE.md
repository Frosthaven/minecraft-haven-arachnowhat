# haven-arachnawhat (`haven_arachnawhat`)

Client-only NeoForge 1.21.1 mod: spiders are drawn, named and heard as Angry Raccoons. Spider and Cave Spider always;
Iron's Spells 'n Spellbooks' Ice Spider when that mod is loaded (optional dependency, `ordering="AFTER"` so our
renderer wins). Must work in plain NeoForge too: no hard dependency on any modpack mod or texture.

## Layout (`src/main/java/dev/frosthaven/havenarachnawhat`)

- `HavenArachnawhat`: registers config + screen, model layer, renderers, sound swap, `/spiders`, harness driver.
- `RaccoonConfig` (`replaceSpiders`, default true) and `Raccoons` (`enabled()`, `set()`, `changed()`, name map, skins).
  `/spiders on` = spiders, `/spiders off` = raccoons (`SpiderCommands`), saved to the config.
- `RaccoonModel`: our own quadruped in its own layer (`haven_arachnawhat:raccoon`), so EMF/Fresh Animations never touch
  it. Walk, head look, swing bite, airborne leap pose. `RaccoonRenderer` scales it per spider (1.15 / 0.6), tips it
  nose-up while `isClimbing()`, and hands the entity to the original renderer when replacing is off.
- `RaccoonSounds`: `PlaySoundEvent` swaps `entity.spider.*` (nearest spider-like mob picks the variant),
  `entity.parrot.imitate.spider` and Iron's `entity.ice_spider.*` for our events, and plays each event's `.grit` layer
  with it (sounds.json: fox voices plus a quieter wolf/hoglin/polar bear/ravager sound).
- Names: `ClientLanguageMixin` maps the spider name keys to `haven_arachnawhat.name.*` while replacing, so they switch
  live; `EntityTypeAccessor` clears the cached type name on change. No language file overrides.
- `compat/irons`: `IceRaccoonModel` (crouch, grapple, named animations), `IceRaccoonRenderer` (surface-normal rotation
  like Iron's, scale 2.6), `IceRaccoonAnimations` (keyframes, same names and lengths as Iron's GeckoLib ones).
  `mixin/IceSpiderAnimationMixin` (@Pseudo) records each `playAnimation(name)` into `IceAnims`.
  `IronsCompat.clearQueuedAnimations` drops Iron's queued animation on toggle (else it replays).
- Textures: `tools/textures.py` paints them (64x64, the model's box UVs); never hand-edit the PNGs.
- Compile-only deps from Modrinth's maven by version id (a version number alone can resolve the Fabric build).

## Test harness

`tools/harness.py` (uv run --with nbtlib python -I tools/harness.py [--launch]) builds Prism instance `dev` (copy of
"Haven Snapshot", JVM flag `-Dhaven_arachnawhat.harness=true`), installs build/libs' jar, and a flat creative world
`dev` with 29 labeled stations (one behavior each, labels use the translated type name) and a sound loop of every
spider clip then every raccoon event. Datapack drives walk/climb/leap/hurt/death/jockey; `Harness.java` drives the
attack swing and each Ice Spider animation/state client side. Bump `BUILD` to rebuild the stage (then /reload);
old-build mobs are voided as they load. Java or asset changes need a game restart (the jar is held open).

Release: push to main; CI publishes `haven-arachnawhat.jar` as the `latest` release.
