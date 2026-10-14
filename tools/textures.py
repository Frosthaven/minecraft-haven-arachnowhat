"""Paints the Angry Raccoon textures (64x64, the UV layout of RaccoonModel.createBodyLayer) and their eyes-only
textures into src/main/resources/assets/haven_arachnawhat/textures/entity/. Run after changing the model's boxes or
a palette: uv run --with pillow python -I tools/textures.py
"""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/haven_arachnawhat/textures/entity"

# Roles: fur, dark (dorsal stripe, tail rings, paws), light (belly, muzzle, brows), mask, eye (glows), mouth.
# Spider tones with dark purple in place of black.
PALETTES = {
    "angry_raccoon": dict(fur=0x4b4146, dark=0x2b1d33, light=0x8a7a72, mask=0x1c1026, eye=0xe02020, mouth=0x120a18),
    "angry_cave_raccoon": dict(fur=0x1d3d40, dark=0x150b24, light=0x4f8f96, mask=0x10081c, eye=0xe02020, mouth=0x0a0612),
    "angry_ice_raccoon": dict(fur=0xa5dbf7, dark=0x5d78b8, light=0xf2fbff, mask=0x2a1b4a, eye=0x38b6ff, mouth=0x1a1030),
}


def faces(u, v, w, h, d):
    """Minecraft box UV faces: name -> (x0, y0, x1, y1). West touches front on its right edge, east on its left."""
    return {
        "top": (u + d, v, u + d + w, v + d), "bottom": (u + d + w, v, u + d + 2 * w, v + d),
        "west": (u, v + d, u + d, v + d + h), "front": (u + d, v + d, u + d + w, v + d + h),
        "east": (u + d + w, v + d, u + 2 * d + w, v + d + h), "back": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


def paint(name, pal):
    rnd = random.Random(name)
    roles = {}   # (x, y) -> role

    def fill(rect, role):
        x0, y0, x1, y1 = rect
        for y in range(y0, y1):
            for x in range(x0, x1):
                roles[(x, y)] = role

    def box(u, v, w, h, d, role):
        for rect in faces(u, v, w, h, d).values():
            fill(rect, role)
        return faces(u, v, w, h, d)

    # body 10x8x12: dark dorsal stripe, light belly and lowest side row, light lower chest
    f = box(0, 0, 10, 8, 12, "fur")
    x0, y0, x1, y1 = f["top"]
    fill((x0 + 3, y0, x1 - 3, y1), "dark")
    fill(f["bottom"], "light")
    for side in ("west", "east", "front", "back"):
        x0, y0, x1, y1 = f[side]
        fill((x0, y1 - 1, x1, y1), "light")
    x0, y0, x1, y1 = f["front"]
    fill((x0 + 2, y0 + 4, x1 - 2, y1), "light")
    fill(f["back"], "dark")

    # head 7x6x5: the angry mask face, mask band round the sides, light chin
    f = box(0, 20, 7, 6, 5, "fur")
    fill(f["bottom"], "light")
    x0, y0, *_ = f["front"]
    face = ["FFFFFFF",
            "LLDFDLL",
            "MEMLMEM",
            "MMMLMMM",
            "LLLLLLL",
            "LLLLLLL"]
    code = {"F": "fur", "L": "light", "D": "dark", "M": "mask", "E": "eye"}
    for r, row in enumerate(face):
        for c, ch in enumerate(row):
            roles[(x0 + c, y0 + r)] = code[ch]
    for side, cols in (("west", range(2, 5)), ("east", range(0, 3))):
        sx, sy, *_ = f[side]
        for c in cols:
            for r in (2, 3):
                roles[(sx + c, sy + r)] = "mask"

    # snout 3x2x2 (light, dark nose on top of the front) and jaw 3x1x2 (dark mouth on top)
    f = box(24, 20, 3, 2, 2, "light")
    x0, y0, *_ = f["front"]
    roles[(x0 + 1, y0)] = "mask"
    f = box(24, 24, 3, 1, 2, "light")
    fill(f["top"], "mouth")

    # ears 2x2x1: dark, light inside
    for v in (20, 24):
        f = box(36, v, 2, 2, 1, "dark")
        fill(f["front"], "light")

    # tail 3x3x9: rings every 2 px along its length, dark tip
    f = box(0, 32, 3, 3, 9, "fur")
    for side in ("top", "bottom"):
        x0, y0, x1, y1 = f[side]
        for y in range(y0, y1):
            if (y - y0) // 2 % 2:
                fill((x0, y, x1, y + 1), "dark")
    for side in ("west", "east"):
        x0, y0, x1, y1 = f[side]
        for x in range(x0, x1):
            if (x - x0) // 2 % 2:
                fill((x, y0, x + 1, y1), "dark")
    fill(f["back"], "dark")

    # legs 3x4x3: fur on top, dark paws
    for i in range(4):
        f = box(44, i * 7, 3, 4, 3, "dark")
        for side in ("west", "east", "front", "back"):
            x0, y0, x1, y1 = f[side]
            fill((x0, y0, x1, y0 + 2), "fur")

    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (x, y), role in roles.items():
        rgb = pal[role]
        r, g, b = rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255
        if role not in ("eye", "mask", "mouth"):   # fur grain
            k = rnd.uniform(0.9, 1.1)
            r, g, b = (min(255, int(c * k)) for c in (r, g, b))
        img.putpixel((x, y), (r, g, b, 255))
        if role == "eye":
            eyes.putpixel((x, y), (r, g, b, 255))
    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f"{name}.png")
    eyes.save(OUT / f"{name}_eyes.png")


if __name__ == "__main__":
    for name, pal in PALETTES.items():
        paint(name, pal)
        print("painted", name)
