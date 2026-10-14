"""Spider test harness: a Prism instance "dev" (copy of "Haven Snapshot") with a flat creative world "dev" that shows
every spider behavior on its own labeled mob ("Spider: walk", "Ice Spider: attack_bite", ...) and loops every spider
sound clip and every Angry Raccoon sound, with 2 s of silence between them.

The server side (this datapack) makes the mobs walk, climb, leap, get hurt, die and carry a rider; the mod's harness
driver (Harness.java, on in dev through -Dhaven_arachnowhat.harness=true) does what only the client can show: the
attack swing and each Ice Spider animation and state.

Run: uv run --with nbtlib python -I tools/harness.py [--launch]   (builds nothing: run ./gradlew build first)
In game: /reload after a rerun; /function spider_harness:stop|start (sound loop); /spiders on|off (originals or raccoons)
"""
import json
import shutil
import struct
import subprocess
import sys
import zipfile
from pathlib import Path

import nbtlib
from nbtlib.tag import Byte, Compound, Int, List, Long, String

REPO = Path(__file__).resolve().parent.parent
PRISM = Path.home() / ".local/share/PrismLauncher"
SRC = PRISM / "instances/Haven Snapshot"
DST = PRISM / "instances/dev"
GAME = DST / "minecraft"
WORLD = GAME / "saves/dev"
NS = "spider_harness"
MOD_ID = "haven_arachnowhat"
JAR = REPO / "build/libs/haven-arachnowhat-1.0.0.jar"
JVM_ARGS = "-Dhaven_arachnowhat.harness=true"
PAUSE_TICKS = 40  # 2 s between the end of one clip and the start of the next
SKIP = ["saves", "screenshots", "logs", "crash-reports", "journeymap/data", "backups"]
Y = -60  # standing height on the default flat world
BUILD = 6  # bump to rebuild the stage in an existing world (then /reload in game)
PLAYER = (0, Y + 9, -16)  # on a glass platform, looking south over the stations

# Stations, one mob each: (entity id, label, action, x, z). Actions the datapack drives: walk (pen), climb (shaft),
# leap, hurt, death, jockey. The mod's harness driver drives attack and the Ice Spider ones. All others stand still.
SPIDER_ACTIONS = ["idle", "walk", "climb", "attack", "leap", "hurt", "death", "jockey"]
CAVE_ACTIONS = ["idle", "walk", "climb", "attack", "leap", "hurt", "death"]
ICE_ACTIONS = ["idle", "walk", "climbing", "crouching", "grappling", "hurt", "death",
               "attack_bite", "attack_fang_basic", "attack_right_swipe", "attack_grapple_pounce", "leap_back",
               "long_cast", "emerge_from_ground"]


def stations():
    out = []
    for i, a in enumerate(SPIDER_ACTIONS):
        out.append(("minecraft:spider", "Spider", a, -21 + i * 6, 2))
    for i, a in enumerate(CAVE_ACTIONS):
        out.append(("minecraft:cave_spider", "Cave Spider", a, -18 + i * 6, 12))
    for i, a in enumerate(ICE_ACTIONS):
        out.append(("irons_spellbooks:ice_spider", "Ice Spider", a, -33 + (i % 7) * 11, 26 + (i // 7) * 14))
    return out


# Sound events of the spider mobs. The Cave Spider uses the Spider's events.
EVENTS = [
    ("Spider / Cave Spider", "minecraft", ["entity.spider.ambient", "entity.spider.hurt",
                                           "entity.spider.death", "entity.spider.step"]),
    ("Ice Spider", "irons_spellbooks", ["entity.ice_spider.ambient", "entity.ice_spider.hurt",
                                        "entity.ice_spider.death", "entity.ice_spider.bite",
                                        "entity.ice_spider.swing", "entity.ice_spider.grapple_latch",
                                        "entity.ice_spider.grapple_spit", "entity.ice_spider.howl"]),
]


def ogg_seconds(data: bytes) -> float:
    rate = struct.unpack_from("<I", data, data.index(b"\x01vorbis") + 12)[0]
    granule = struct.unpack_from("<q", data, data.rindex(b"OggS") + 6)[0]
    return granule / rate


def mod_jar(namespace: str) -> Path:
    for jar in sorted((SRC / "minecraft/mods").glob("*.jar")):
        with zipfile.ZipFile(jar) as z:
            if f"assets/{namespace}/sounds.json" in z.namelist():
                return jar
    raise SystemExit(f"no jar with assets/{namespace}/sounds.json")


def read_asset(namespace: str, path: str) -> bytes:
    """A vanilla asset from the launcher's object store, or a mod asset from its jar."""
    if namespace == "minecraft":
        objects = json.loads((PRISM / "assets/indexes/17.json").read_text())["objects"]
        h = objects[f"minecraft/{path}"]["hash"]
        return (PRISM / "assets/objects" / h[:2] / h).read_bytes()
    with zipfile.ZipFile(mod_jar(namespace)) as z:
        return z.read(f"assets/{namespace}/{path}")


def split(name: str, default_ns: str):
    return name.split(":", 1) if ":" in name else (default_ns, name)


def clips():
    """(label, sound id to play, seconds). Every distinct spider sound file (through the harness resource pack, which
    gives each file its own event), then every Angry Raccoon event (as the game plays it: a random variant, so the wait
    covers the longest one)."""
    files = {}
    for group, ns, events in EVENTS:
        table = json.loads(read_asset(ns, "sounds.json"))
        for event in events:
            for s in table[event]["sounds"]:
                fns, fpath = split(s if isinstance(s, str) else s["name"], ns)
                key = f"{fns}:{fpath}"
                if key not in files:
                    files[key] = [group, [], ogg_seconds(read_asset(fns, f"sounds/{fpath}.ogg"))]
                files[key][1].append(event.split(".")[-1])
    out = []
    for i, (key, (group, events, secs)) in enumerate(files.items()):
        out.append((f"{group} | {'/'.join(events)} | {key.split('/')[-1].split(':')[-1]}", f"{NS}:clip{i}", secs))
    vanilla = json.loads(read_asset("minecraft", "sounds.json"))
    ours = json.loads((REPO / f"src/main/resources/assets/{MOD_ID}/sounds.json").read_text())
    for key, entry in ours.items():
        ref = entry["sounds"][0]
        event = split(ref["name"], "minecraft")[1]
        longest = max(ogg_seconds(read_asset("minecraft", f"sounds/{(f if isinstance(f, str) else f['name'])}.ogg"))
                      for f in vanilla[event]["sounds"] if not (isinstance(f, dict) and f.get("type") == "event"))
        if not key.endswith(".grit"):   # layers play with their main event
            out.append((f"Raccoon | {key}", f"{MOD_ID}:{key}", longest / ref.get("pitch", 1.0)))
    return out, list(files)


def copy_instance():
    """A copy of the source instance on first run; the harness JVM flag every run."""
    fresh = not DST.exists()
    if fresh:
        subprocess.run(["cp", "-a", "--reflink=auto", str(SRC), str(DST)], check=True)
        for rel in SKIP:
            shutil.rmtree(GAME / rel, ignore_errors=True)
    set_keys = {"OverrideJavaArgs": "true", "JvmArgs": JVM_ARGS}
    if fresh:
        set_keys |= {"name": "dev", "totalTimePlayed": "0", "lastTimePlayed": "0", "lastLaunchTime": "0"}
    cfg = DST / "instance.cfg"
    lines = [f"{k}={set_keys[k]}" if (k := line.split("=", 1)[0]) in set_keys else line
             for line in cfg.read_text().splitlines()]
    cfg.write_text("\n".join(lines) + "\n")


def install_mod():
    if not JAR.exists():
        raise SystemExit(f"{JAR} missing: run ./gradlew build first")
    for old in (GAME / "mods").glob("haven-arachnowhat-*.jar"):
        old.unlink()
    shutil.copy2(JAR, GAME / "mods" / JAR.name)


def write(path: Path, text: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)


def resource_pack(files):
    root = GAME / "resourcepacks" / NS
    shutil.rmtree(root, ignore_errors=True)
    write(root / "pack.mcmeta", json.dumps({"pack": {"pack_format": 34, "description": "Spider sound harness"}}))
    sounds = {f"clip{i}": {"sounds": [{"name": key}]} for i, key in enumerate(files)}
    write(root / f"assets/{NS}/sounds.json", json.dumps(sounds, indent=1))
    opts = GAME / "options.txt"
    lines = opts.read_text().splitlines()
    for i, line in enumerate(lines):
        if line.startswith("resourcePacks:"):
            packs = [p for p in json.loads(line[len("resourcePacks:"):]) if p != f"file/{NS}"]
            lines[i] = "resourcePacks:" + json.dumps(packs + [f"file/{NS}"], separators=(",", ":"), ensure_ascii=False)
    opts.write_text("\n".join(lines) + "\n")


def nbt(eid, action, extra=""):
    """A station mob: named "<its translated type name>: <action>", so the label shows the name the client uses."""
    ns, path = eid.split(":")
    still = "" if action in ("walk", "climb") else \
        'attributes:[{id:"minecraft:generic.movement_speed",base:0d},{id:"minecraft:generic.knockback_resistance",base:1d}],'
    invulnerable = "" if action in ("hurt", "death") else "Invulnerable:1b,"
    return (f"{{Silent:1b,PersistenceRequired:1b,{invulnerable}{still}Tags:[\"sh_spider\",\"sh_b{BUILD}\",\"sh_{action}\"],"
            f"CustomName:'[{{\"translate\":\"entity.{ns}.{path}\"}},\": {action}\"]',CustomNameVisible:1b{extra}}}")


def datapack(items):
    root = WORLD / "datapacks" / NS
    shutil.rmtree(root, ignore_errors=True)
    fn = root / f"data/{NS}/function"
    write(root / "pack.mcmeta", json.dumps({"pack": {"pack_format": 48, "description": "Spider harness"}}))
    write(root / "data/minecraft/tags/function/load.json", json.dumps({"values": [f"{NS}:load"]}))
    write(root / "data/minecraft/tags/function/tick.json", json.dumps({"values": [f"{NS}:tick"]}))
    write(fn / "load.mcfunction", "\n".join([
        "scoreboard objectives add sh dummy",
        "scoreboard players add #i sh 0", "scoreboard players add #t sh 0",
        "gamerule doMobSpawning false", "gamerule doDaylightCycle false", "gamerule doWeatherCycle false",
        "gamerule doInsomnia false", "gamerule doPatrolSpawning false", "gamerule doTraderSpawning false",
        "gamerule doMobLoot false", "difficulty easy", "time set noon", "weather clear",
        f"schedule function {NS}:next 40t replace", ""]))
    write(fn / "tick.mcfunction", "\n".join([
        f"execute as @a[tag=!sh_ready] at @s run function {NS}:setup",
        f"execute unless data storage {NS}:state {{built:{BUILD}}} if entity @a run function {NS}:build",
        # mobs of an older build load a moment after the stage is rebuilt, so the build's kill misses them: drop them
        # into the void (out of sight, no death animation) whenever they appear
        f"execute as @e[tag=sh_spider,tag=!sh_b{BUILD}] at @s run tp @s ~ -200 ~",
        "scoreboard players add #t sh 1",
        f"execute if score #t sh matches 80.. run scoreboard players set #t sh 0",
        f"execute if score #t sh matches 0 run function {NS}:pulse_a",
        f"execute if score #t sh matches 40 run function {NS}:pulse_b", ""]))
    px, py, pz = PLAYER
    write(fn / "setup.mcfunction", "\n".join([
        "tag @s add sh_ready", "gamemode creative @s", f"tp @s {px} {py} {pz} 0 25", ""]))

    stage, deaths = [], []
    for eid, label, action, x, z in stations():
        big = eid.endswith("ice_spider")
        if action == "walk":
            r = 7 if big else 3
            stage.append(f"fill {x - r} {Y} {z - r} {x + r} {Y + 4} {z + r} glass hollow")
            stage.append(f"summon {eid} {x} {Y + 1} {z} {nbt(eid, action)}")
        elif action == "climb":
            r = 0 if eid.endswith("cave_spider") else 1   # a shaft one block wider than the mob: it always touches a wall
            stage.append(f"fill {x - r - 1} {Y} {z - r - 1} {x + r + 1} {Y + 7} {z + r + 1} glass hollow")
            stage.append(f"summon {eid} {x} {Y + 1} {z} {nbt(eid, action)}")
        elif action == "death":
            deaths.append(f"summon {eid} {x} {Y} {z} {nbt(eid, action)}")
        elif action == "jockey":
            rider = ('{id:"minecraft:skeleton",Silent:1b,NoAI:1b,PersistenceRequired:1b,Invulnerable:1b,Tags:["sh_spider","sh_b' + str(BUILD) + '"],'
                     'ArmorItems:[{},{},{},{id:"minecraft:leather_helmet",count:1}]}')
            stage.append(f"summon {eid} {x} {Y} {z} {nbt(eid, action, f',Passengers:[{rider}]')}")
        else:
            stage.append(f"summon {eid} {x} {Y} {z} {nbt(eid, action)}")
    clear = [f"fill -48 {Y} {z0} 48 {Y + 12} {z0 + 15} air" for z0 in range(-32, 64, 16)]
    write(fn / "build.mcfunction", "\n".join([
        "forceload add -48 -32 48 63", *clear, "kill @e[tag=sh_spider]",
        f"fill {px - 2} {py - 1} {pz - 2} {px + 2} {py - 1} {pz + 2} glass", *stage,
        f"tp @a {px} {py} {pz} 0 25",
        f"data modify storage {NS}:state built set value {BUILD}", ""]))
    # Every 4 s: leapers jump, hurt mobs take a hit and heal, a fresh mob appears at each death station (pulse_a),
    # and 2 s later it dies (pulse_b), so the death animation and its sound play every cycle.
    write(fn / "pulse_a.mcfunction", "\n".join([
        "execute as @e[tag=sh_leap] run data merge entity @s {Motion:[0.0d,0.55d,0.0d]}",
        "execute as @e[tag=sh_hurt] run damage @s 1 minecraft:generic",
        "effect give @e[tag=sh_hurt] minecraft:regeneration 3 4 true",
        "kill @e[tag=sh_death]", *deaths, ""]))
    write(fn / "pulse_b.mcfunction", "\n".join([
        "execute as @e[tag=sh_leap] run data merge entity @s {Motion:[0.0d,0.55d,0.0d]}",
        "execute as @e[tag=sh_hurt] run damage @s 1 minecraft:generic",
        "kill @e[tag=sh_death]", ""]))

    write(fn / "start.mcfunction", f"schedule function {NS}:next 1t replace\n")
    write(fn / "stop.mcfunction", f"schedule clear {NS}:next\ntitle @a actionbar \"Sound loop stopped\"\n")
    write(fn / "next.mcfunction", "\n".join(
        f"execute if score #i sh matches {i} run return run function {NS}:clip/{i}" for i in range(len(items))) + "\n")
    for i, (label, sound, secs) in enumerate(items):
        wait = int(secs * 20 + 0.999) + PAUSE_TICKS
        write(fn / f"clip/{i}.mcfunction", "\n".join([
            f"title @a actionbar {json.dumps(f'{label} ({secs:.1f}s)')}",
            f"execute as @a at @s run playsound {sound} master @s ~ ~ ~ 1 1",
            f"scoreboard players set #i sh {(i + 1) % len(items)}",
            f"schedule function {NS}:next {wait}t replace", ""]))


def world():
    if (WORLD / "level.dat").exists():
        return
    src = next((SRC / "minecraft/saves").iterdir()) / "level.dat"
    f = nbtlib.load(src)
    d = f["Data"]
    for k in ("Player", "DragonFight", "CustomBossEvents", "ScheduledEvents", "WanderingTraderId"):
        d.pop(k, None)
    d["LevelName"] = String("dev")
    d["GameType"] = Int(1)
    d["allowCommands"] = Byte(1)
    d["Difficulty"] = Byte(1)
    d["raining"], d["thundering"] = Byte(0), Byte(0)
    d["Time"], d["DayTime"] = Long(6000), Long(6000)
    d["SpawnX"], d["SpawnY"], d["SpawnZ"] = Int(PLAYER[0]), Int(Y), Int(PLAYER[2])
    d["WorldGenSettings"]["dimensions"]["minecraft:overworld"]["generator"] = Compound({
        "type": String("minecraft:flat"),
        "settings": Compound({
            "biome": String("minecraft:plains"), "lakes": Byte(0), "features": Byte(0),
            "structure_overrides": List[String]([]),
            "layers": List[Compound]([
                Compound({"block": String("minecraft:bedrock"), "height": Int(1)}),
                Compound({"block": String("minecraft:dirt"), "height": Int(2)}),
                Compound({"block": String("minecraft:grass_block"), "height": Int(1)}),
            ]),
        }),
    })
    WORLD.mkdir(parents=True)
    f.save(WORLD / "level.dat")


def main():
    copy_instance()
    install_mod()
    items, files = clips()
    world()
    resource_pack(files)
    datapack(items)
    total = sum(int(s * 20 + 0.999) + PAUSE_TICKS for *_, s in items) / 20
    print(f"{len(stations())} stations, {len(items)} sound clips, loop {total:.0f}s")
    if "--launch" in sys.argv:
        subprocess.Popen(["prismlauncher", "-l", "dev", "-w", "dev"], start_new_session=True,
                         stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


if __name__ == "__main__":
    main()
