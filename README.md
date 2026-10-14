# haven-arachnawhat

An arachnophobia mod for Minecraft 1.21.1 (NeoForge). Spiders become **Angry Raccoons**: new models, names and sounds,
with every spider behavior still animated. **Client only:** only you see the raccoons. The server, other players and
the game itself are unchanged: the mobs act, fight and drop the same as before.

| Spider | Becomes |
|---|---|
| Spider | Angry Raccoon |
| Cave Spider | Angry Cave Raccoon |
| Ice Spider ([Iron's Spells 'n Spellbooks](https://modrinth.com/mod/irons-spells-n-spellbooks), if installed) | Angry Ice Raccoon |

## Download

**[haven-arachnawhat.jar](https://github.com/Frosthaven/minecraft-haven-arachnawhat/releases/latest/download/haven-arachnawhat.jar)**
(always the latest build).

## Install

You need Minecraft 1.21.1 with NeoForge (any modpack on it works, for example Create Ultimate Selection 2).

1. Open your instance folder.
   - CurseForge: right-click the modpack > Open Folder.
   - Prism Launcher: right-click the instance > Folder, then open `minecraft`.
2. Copy `haven-arachnawhat.jar` into the `mods` folder.
3. Start the game.

To update, download the jar again and replace the old one. To remove the mod, delete the jar.

## Turn it on or off

- In game, type `/spiders on` to see spiders, or `/spiders off` to see angry raccoons. It switches at once and is remembered.
- Or: Esc > Mods > haven-arachnawhat > Config > **Replace spiders with angry raccoons** (default: on).

## What changes

- **Models:** a raccoon model with spider colours, dark purple in place of black, and glowing eyes. It walks, bites,
  leaps, climbs walls nose-up, gets hurt and dies like the spider it replaces. The Angry Ice Raccoon also lies flat
  on walls and ceilings, crouches, grapples, and has its own version of each Ice Spider attack.
- **Names:** in name tags, Jade, death messages and spawn eggs (English).
- **Sounds:** gruff fox growls in place of spider hisses, with their own subtitles.
- **Not changed:** item drops (String, Spider Eye) and items that have "spider" in their name.

Works with Fresh Animations and other entity model packs: they cannot change the raccoons, and they apply again when
spiders are switched back on.

## Development

Build: `./gradlew build` (Gradle on JDK 17 or 21; the jar is in `build/libs`). See CLAUDE.md for the layout and the
test harness (`tools/harness.py`), and `tools/textures.py` for the textures.
