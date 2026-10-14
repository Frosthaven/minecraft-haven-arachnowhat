"""Paints the Mecha Raccoon textures (64x64, the UV layout of RaccoonModel.createBodyLayer) and their glow-only
textures (eyes, chest core, tail light) into src/main/resources/assets/haven_arachnowhat/textures/entity/. Run after
changing the model's boxes or a palette: uv run --with pillow python -I tools/textures.py
"""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/haven_arachnowhat/textures/entity"

# Roles: plate (armour), seam (panel lines, joints, spine, feet), light (belly plate, muzzle), rivet, visor (dark glass
# band over the eyes), grille (mouth), glow (eyes, chest core, tail light: full bright). Spider tones as metal.
PALETTES = {
    "mecha_raccoon": dict(plate=0x4a4650, seam=0x221a2a, light=0x7d7886, rivet=0xdfe4ea, visor=0x120d1a,
                          grille=0x0a0710, glow=0x38c8ff),
    "mecha_cave_raccoon": dict(plate=0x2a4a50, seam=0x111d26, light=0x52858c, rivet=0xdfe4ea, visor=0x081016,
                               grille=0x05090c, glow=0x38c8ff),
    "mecha_ice_raccoon": dict(plate=0x9fc6dc, seam=0x4f6a9a, light=0xe2f2fa, rivet=0xffffff, visor=0x1d2440,
                              grille=0x141a30, glow=0x5ae0ff),
}
FLAT = ("glow", "visor", "grille", "rivet")   # no brushed-metal grain


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

    def corners(rect):
        """A rivet in each corner of a panel."""
        x0, y0, x1, y1 = rect
        for x, y in ((x0, y0), (x1 - 1, y0), (x0, y1 - 1), (x1 - 1, y1 - 1)):
            roles[(x, y)] = "rivet"

    def pattern(x0, y0, rows, code):
        for r, row in enumerate(rows):
            for c, ch in enumerate(row):
                roles[(x0 + c, y0 + r)] = code[ch]

    # body 10x8x12: spine and panel seams with a rivet in each panel corner, side vents, belly plate, chest core
    f = box(0, 0, 10, 8, 12, "plate")
    x0, y0, x1, y1 = f["top"]
    fill((x0 + 4, y0, x1 - 4, y1), "seam")
    fill((x0, y0 + 6, x1, y0 + 7), "seam")
    for px0, px1 in ((x0, x0 + 4), (x1 - 4, x1)):
        corners((px0, y0, px1, y0 + 6))
        corners((px0, y0 + 7, px1, y1))
    fill(f["bottom"], "light")
    for side in ("west", "east"):
        x0, y0, x1, y1 = f[side]
        fill((x0, y1 - 1, x1, y1), "seam")
        for x in (x0 + 4, x0 + 8):
            fill((x, y0, x + 1, y1 - 1), "seam")
        for y in (y0 + 3, y0 + 5):
            fill((x0 + 5, y, x0 + 8, y + 1), "grille")
        for px0, px1 in ((x0, x0 + 4), (x0 + 5, x0 + 8), (x0 + 9, x1)):
            corners((px0, y0, px1, y1 - 1))
    x0, y0, x1, y1 = f["front"]
    fill((x0, y1 - 1, x1, y1), "seam")
    corners((x0, y0, x1, y0 + 3))
    fill((x0 + 2, y0 + 3, x1 - 2, y1 - 1), "light")
    corners((x0 + 2, y0 + 3, x1 - 2, y1 - 1))
    fill((x0 + 4, y0 + 4, x0 + 6, y0 + 6), "glow")
    x0, y0, x1, y1 = f["back"]
    fill((x0, y0, x1, y1), "seam")
    fill((x0 + 1, y0 + 1, x1 - 1, y1 - 1), "plate")
    corners((x0 + 1, y0 + 1, x1 - 1, y1 - 1))

    # head 7x6x5: visor band with glowing eyes, bolted brow, visor wraps round the sides, light chin
    f = box(0, 20, 7, 6, 5, "plate")
    fill(f["bottom"], "light")
    x0, y0, *_ = f["front"]
    pattern(x0, y0, ["RPPPPPR",
                     "SSSPSSS",
                     "VGVVVGV",
                     "VVVVVVV",
                     "LLLLLLL",
                     "LSLLLSL"],
            {"P": "plate", "L": "light", "S": "seam", "V": "visor", "G": "glow", "R": "rivet"})
    for side, cols in (("west", range(2, 5)), ("east", range(0, 3))):
        sx, sy, *_ = f[side]
        for c in cols:
            for r in (2, 3):
                roles[(sx + c, sy + r)] = "visor"
        roles[(sx + 2, sy + 4)] = "rivet"
    x0, y0, x1, y1 = f["top"]
    fill((x0 + 3, y0, x0 + 4, y1), "seam")
    corners((x0, y0, x1, y1))

    # snout 3x2x2 (light, visor nose tip) and jaw 3x1x2 (grille mouth on top, seam teeth on the front)
    f = box(24, 20, 3, 2, 2, "light")
    x0, y0, *_ = f["front"]
    roles[(x0 + 1, y0)] = "visor"
    f = box(24, 24, 3, 1, 2, "light")
    fill(f["top"], "grille")
    x0, y0, *_ = f["front"]
    roles[(x0 + 1, y0)] = "seam"

    # ears 2x2x1: plate, seam inside
    for v in (20, 24):
        f = box(36, v, 2, 2, 1, "plate")
        fill(f["front"], "seam")

    # tail 3x3x9: segments (seam every 2 px along its length), glowing tip light
    f = box(0, 32, 3, 3, 9, "plate")
    for side in ("top", "bottom"):
        x0, y0, x1, y1 = f[side]
        for y in range(y0 + 1, y1, 2):
            fill((x0, y, x1, y + 1), "seam")
    for side in ("west", "east"):
        x0, y0, x1, y1 = f[side]
        for x in range(x0 + 1, x1, 2):
            fill((x, y0, x + 1, y1), "seam")
    x0, y0, x1, y1 = f["back"]
    fill((x0, y0, x1, y1), "seam")
    roles[(x0 + 1, y0 + 1)] = "glow"

    # legs 3x4x3: plate, seam knee joint, seam feet, a rivet on the front
    for i in range(4):
        f = box(44, i * 7, 3, 4, 3, "plate")
        for side in ("west", "east", "front", "back"):
            x0, y0, x1, y1 = f[side]
            fill((x0, y0 + 2, x1, y0 + 3), "seam")
            fill((x0, y1 - 1, x1, y1), "seam")
        x0, y0, *_ = f["front"]
        roles[(x0 + 1, y0 + 1)] = "rivet"
        fill(f["bottom"], "seam")

    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rows = {y: rnd.uniform(0.94, 1.06) for y in range(64)}   # brushed metal: each row a little lighter or darker
    for (x, y), role in roles.items():
        rgb = pal[role]
        r, g, b = rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255
        if role not in FLAT:
            k = rows[y] * rnd.uniform(0.98, 1.02)
            r, g, b = (min(255, int(c * k)) for c in (r, g, b))
        img.putpixel((x, y), (r, g, b, 255))
        if role == "glow":
            glow.putpixel((x, y), (r, g, b, 255))
    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f"{name}.png")
    glow.save(OUT / f"{name}_eyes.png")


if __name__ == "__main__":
    for name, pal in PALETTES.items():
        paint(name, pal)
        print("painted", name)
