#!/usr/bin/env python3
"""Reproducibly generate original pixel textures and vanilla cuboid item models."""
from PIL import Image, ImageDraw
from pathlib import Path
import json, math, random

ROOT = Path(__file__).parent
ASSETS = ROOT / 'src/main/resources/assets/arcanerealms'
DATA = ROOT / 'src/main/resources/data/arcanerealms/recipe'

PALETTES = {
    'astral': {
        'wood': (58, 42, 88), 'metal': (193, 160, 236), 'gem': (150, 75, 255), 'glow': (218, 179, 255), 'grip': (42, 34, 70)
    },
    'ember': {
        'wood': (99, 51, 39), 'metal': (255, 181, 68), 'gem': (255, 95, 33), 'glow': (255, 222, 113), 'grip': (60, 33, 34)
    },
    'frost': {
        'wood': (43, 79, 102), 'metal': (144, 223, 237), 'gem': (77, 183, 255), 'glow': (212, 251, 255), 'grip': (34, 60, 80)
    }
}

def color_texture(color, seed, category):
    rng = random.Random(seed)
    img = Image.new('RGBA', (32, 32)); pixels = img.load()
    for y in range(32):
        for x in range(32):
            # Directional pixel shading gives the 3D mesh hand-made pixel-art grain.
            edge = 10 if (x+y) % 7 == 0 else 0
            vertical = -9 if (x % 8 in (6, 7) and category in ('wood', 'grip')) else 0
            grain = rng.choice([-9,-6,-4,-2,0,1,3,6,9])
            shine = 10 if (x-y)%19 < 3 else 0
            drift = grain+edge+vertical+shine
            if category == 'gem':
                drift += 22 if (x+y) % 12 <= 3 else -6
            pixels[x, y] = tuple(max(0, min(255, ch+drift)) for ch in color) + (255,)
    return img

def cube(fr, to, texture, angle=None, origin=None, shade=True):
    faces = {face: {'texture': '#'+texture, 'uv': [0,0,16,16]}
             for face in ['north','south','east','west','up','down']}
    c = {'from': list(fr), 'to': list(to), 'faces': faces, 'shade': shade}
    if angle is not None:
        c['rotation'] = {'origin': origin or [8,8,8], 'axis':'z', 'angle': angle, 'rescale': True}
    return c

def model_for(staff):
    cubes = []
    # Oak/obsidian heart, decorated grip, and five separate engraved bands.
    cubes.append(cube([7.20,0.25,7.20],[8.80,12.9,8.80],'wood'))
    cubes.append(cube([7.05,0.05,7.05],[8.95,1.25,8.95],'metal'))
    cubes.append(cube([7.12,1.25,7.10],[8.88,4.15,8.9],'grip'))
    for y in [1.55,3.45,5.4,7.9,10.25,12.25]:
        cubes.append(cube([6.82,y,6.82],[9.18,y+0.42,9.18],'metal'))
    # Cross-checker runes on the shaft: three inset rows, offset per staff.
    for y in [4.8,6.6,9.2]:
        cubes.append(cube([7.2,y,6.95],[8.8,y+0.4,7.2],'glow'))
    cubes.append(cube([6.4,11.8,6.4],[9.6,12.7,9.6],'metal'))
    cubes.append(cube([7.45,12.2,7.45],[8.55,14.25,8.55],'wood'))
    # Vertical central jewel has four diamond facets, rotated in 3D as cuboids.
    cubes.append(cube([6.9,13.0,6.9],[9.1,15.4,9.1],'gem',45,[8,14.2,8],False))
    cubes.append(cube([7.43,13.1,6.28],[8.57,15.45,9.72],'glow',45,[8,14.2,8],False))
    cubes.append(cube([7.25,14.7,7.25],[8.75,16.0,8.75],'glow',45,[8,15,8],False))

    if staff == 'astral':
        # Antler-like silver crescent crown with 4 'orbit' jewels.
        for side in (-1,1):
            x = 8 + side*2.0
            cubes.append(cube([x-.45,12.35,7.4],[x+.45,15.0,8.6],'metal',-side*22,[x,13.6,8]))
            cubes.append(cube([x-.36,14.7,7.4],[x+.36,15.7,8.6],'glow',-side*25,[x,14.8,8]))
        for dx,dz in [(0,-2.3),(0,2.3),(-2.3,0),(2.3,0)]:
            cubes.append(cube([8+dx-0.45,13.8,8+dz-0.45],[8+dx+0.45,14.7,8+dz+0.45],'gem',45,[8+dx,14.25,8+dz],False))
    elif staff == 'ember':
        # Fireburst forks: orange spikes surrounding a blazing core.
        for side in (-1,1):
            x = 8 + side*1.55
            cubes.append(cube([x-.55,12.2,7.4],[x+.55,15.65,8.6],'metal',side*30,[x,13.8,8]))
            cubes.append(cube([x-.37,14.45,7.3],[x+.37,16,8.7],'gem',side*45,[x,15.0,8],False))
        for dz in (-1.35,1.35):
            cubes.append(cube([7.4,12.6,8+dz-.3],[8.6,14.6,8+dz+.3],'gem'))
        cubes.append(cube([7.48,15.25,7.48],[8.52,16,8.52],'glow',45,[8,15.6,8],False))
    else:
        # Ice-crystal silhouette with symmetric offset faceted ice spires.
        for side in (-1,1):
            x = 8+side*2
            cubes.append(cube([x-.48,11.6,7.5],[x+.48,14.6,8.5],'metal',side*35,[x,13,8]))
            cubes.append(cube([x-.43,13.65,7.4],[x+.43,15.8,8.6],'glow',side*35,[x,14.5,8],False))
        for dz in (-2.05,2.05):
            cubes.append(cube([7.6,12.5,8+dz-.4],[8.4,15.1,8+dz+.4],'gem',45,[8,13.8,8+dz],False))
        cubes.append(cube([7.3,15.15,7.3],[8.7,16,8.7],'glow',45,[8,15.7,8],False))
    return {
        'gui_light': 'front',
        'ambientocclusion': False,
        'textures': {t:'arcanerealms:item/'+staff+'_'+t for t in ['wood','metal','gem','glow','grip']},
        'elements': cubes,
        'display': {
            'thirdperson_righthand': {'rotation':[0,90,45], 'translation':[0,3.4,1], 'scale':[0.75,0.75,0.75]},
            'thirdperson_lefthand': {'rotation':[0,-90,-45], 'translation':[0,3.4,1], 'scale':[0.75,0.75,0.75]},
            'firstperson_righthand': {'rotation':[0,-92,31], 'translation':[1.1,3.6,0.8], 'scale':[0.83,0.83,0.83]},
            'firstperson_lefthand': {'rotation':[0,92,-31], 'translation':[1.1,3.6,0.8], 'scale':[0.83,0.83,0.83]},
            'gui': {'rotation':[30,225,0], 'translation':[0,0,0], 'scale':[0.84,0.84,0.84]},
            'ground': {'rotation':[0,0,0], 'translation':[0,1.7,0], 'scale':[0.53,0.53,0.53]},
            'fixed': {'rotation':[0,90,0], 'translation':[0,0,0], 'scale':[0.75,0.75,0.75]}
        }
    }

for idx,(staff,palette) in enumerate(PALETTES.items()):
    for color_type, color in palette.items():
        path = ASSETS/'textures/item'/f'{staff}_{color_type}.png'
        color_texture(color, 1000+idx*10+len(color_type), color_type).save(path)
    name = staff+'_staff'
    with (ASSETS/'models/item'/f'{name}.json').open('w') as f:
        json.dump(model_for(staff), f, indent=2)
    with (ASSETS/'items'/f'{name}.json').open('w') as f:
        json.dump({'model':{'type':'minecraft:model', 'model':f'arcanerealms:item/{name}'}},f,indent=2)

translations = {
    'item.arcanerealms.astral_staff': 'Astral Staff',
    'item.arcanerealms.ember_staff': 'Ember Staff',
    'item.arcanerealms.frost_staff': 'Frost Staff',
}
(ASSETS/'lang/en_us.json').write_text(json.dumps(translations,indent=2)+'\n')

# Simple 3x3 recipes, each rune is a different vanilla mineral.
recipes = {
    'astral_staff': {'G':'minecraft:amethyst_shard','M':'minecraft:gold_ingot','S':'minecraft:stick'},
    'ember_staff': {'G':'minecraft:blaze_powder','M':'minecraft:copper_ingot','S':'minecraft:stick'},
    'frost_staff': {'G':'minecraft:blue_ice','M':'minecraft:iron_ingot','S':'minecraft:stick'},
}
for name,parts in recipes.items():
    obj={'type':'minecraft:crafting_shaped','category':'equipment',
         'pattern':[' G ',' M ',' S '],
         'key':{k:v for k,v in parts.items()},
         'result':{'id':'arcanerealms:'+name,'count':1}}
    (DATA/(name+'.json')).write_text(json.dumps(obj,indent=2)+'\n')
print('Generated',len(list((ASSETS/'models/item').glob('*.json'))),'3D staff models and',len(list((ASSETS/'textures/item').glob('*.png'))),'textures')
