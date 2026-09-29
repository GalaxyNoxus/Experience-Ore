from pathlib import Path
from PIL import Image, ImageDraw
import json
import copy
import itertools

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'

def write_json(path, data):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

for folder in ['assets/xpore/blockstates', 'assets/xpore/models/block', 'assets/xpore/models/item',
               'data/xpore/loot_table/blocks', 'data/xpore/worldgen/configured_feature',
               'data/xpore/worldgen/placed_feature']:
    for name in ['xp_ore', 'deepslate_xp_ore']:
        (ROOT / folder / (name + '.json')).unlink(missing_ok=True)
for name in ['xp_ore.png', 'xp_crystals.png']:
    (ROOT / 'assets/xpore/textures/block' / name).unlink(missing_ok=True)
texdir = ROOT / 'assets/xpore/textures/block'
texdir.mkdir(parents=True, exist_ok=True)
crystal = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
mask = Image.new('L', (16, 16), 0)

crystal_grid = [
"................",
".......4........",
"......453.......",
".....44524......",
"....4445224.....",
"....4566324.....",
"....1334121.....",
"....1234120.....",
"554..124110..455",
"4554.113110.4554",
"4264401231.44624",
"4225101130224224",
".42141012014124.",
"..411110202112..",
"...2101042012...",
"...1000240001...",
]

tier_colors = [
    (14, 54, 26),
    (22, 78, 38),
    (32, 102, 50),
    (55, 140, 70),
    (85, 175, 95),
    (150, 225, 130),
    (235, 255, 205),
]
tier_emission = [41, 59, 78, 112, 145, 201, 254]

for y, row in enumerate(crystal_grid):
    for x, ch in enumerate(row):
        if ch == '.':
            continue
        tier = int(ch)
        crystal.putpixel((x, y), (*tier_colors[tier], 255))
        mask.putpixel((x, y), tier_emission[tier])

crystal.save(texdir/'experience_crystal.png')
emissive=Image.new('RGBA',crystal.size,(0,0,0,0))
specular=Image.new('RGBA',crystal.size,(0,0,0,0))
for y in range(crystal.height):
    for x in range(crystal.width):
        color=crystal.getpixel((x,y))
        if color[3]:
            strength=min(mask.getpixel((x,y)),254)
            specular.putpixel((x,y),(130,10,0,strength))
            if strength:
                emissive.putpixel((x,y),tuple(round(v*strength/254) for v in color[:3])+(255,))
emissive.save(texdir/'experience_crystal_e.png')
specular.save(texdir/'experience_crystal_s.png')
Image.new('RGBA',crystal.size,(128,128,255,255)).save(texdir/'experience_crystal_n.png')

inclusions=Image.new('RGBA',(16,16),(0,0,0,0))
d=ImageDraw.Draw(inclusions)
for x,y,shape in [(2,3,0),(10,2,1),(6,10,1),(12,12,0)]:
    d.polygon([(x,y),(x+1,y-1),(x+3,y+1),(x+2,y+3),(x,y+2)],fill=(36,80,54,255))
    d.rectangle((x+1,y,x+2,y+1),fill=(32,138,69,255))
    d.point((x+1,y),fill=(86,181,67,255))
    if shape:d.point((x+2,y+2),fill=(29,110,61,255))
inclusions.save(texdir/'experience_inclusions.png')
Image.new('RGBA',inclusions.size,(0,0,0,0)).save(texdir/'experience_inclusions_e.png')
inclusions_specular=Image.new('RGBA',inclusions.size,(0,0,0,0))
for y in range(inclusions.height):
    for x in range(inclusions.width):
        r,g,b,a=inclusions.getpixel((x,y))
        if a:
            inclusions_specular.putpixel((x,y),(90,10,0,0))
inclusions_specular.save(texdir/'experience_inclusions_s.png')
Image.new('RGBA',inclusions.size,(128,128,255,255)).save(texdir/'experience_inclusions_n.png')

p=ROOT/'assets/minecraft/optifine/emissive.properties'
p.parent.mkdir(parents=True,exist_ok=True);p.write_text('suffix.emissive=_e\n')
faces=['down','up','north','south','west','east']
transforms={
'up':((1,0,0),(0,1,0),(0,0,1)),
'down':((1,0,0),(0,-1,0),(0,0,-1)),
'north':((1,0,0),(0,0,1),(0,-1,0)),
'south':((1,0,0),(0,0,-1),(0,1,0)),
'west':((0,-1,0),(1,0,0),(0,0,1)),
'east':((0,1,0),(-1,0,0),(0,0,1))}
def transform(point,m):
    return [round(8+sum(m[i][k]*(point[k]-8) for k in range(3)),5) for i in range(3)]
def transform_element(element,m):
    points=[transform(p,m) for p in itertools.product(*zip(element['from'],element['to']))]
    result=copy.deepcopy(element)
    result['from']=[min(p[i] for p in points) for i in range(3)]
    result['to']=[max(p[i] for p in points) for i in range(3)]
    rot=result['rotation'];old_axis='xyz'.index(rot['axis'])
    new_axis=next(i for i in range(3) if m[i][old_axis])
    rot['axis']='xyz'[new_axis];rot['angle']*=m[new_axis][old_axis]
    rot['origin']=transform(rot['origin'],m)
    return result
def face_vertices(face,lo,hi):
    x,y,z=lo;X,Y,Z=hi
    return {
        'down':[(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)],
        'up':[(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)],
        'north':[(X,Y,z),(X,y,z),(x,y,z),(x,Y,z)],
        'south':[(x,Y,Z),(x,y,Z),(X,y,Z),(X,Y,Z)],
        'west':[(x,Y,z),(x,y,z),(x,y,Z),(x,Y,Z)],
        'east':[(X,Y,Z),(X,y,Z),(X,y,z),(X,Y,z)]}[face]
shards=[]
for direction,m in transforms.items():
    for angle in [-45,45]:
        e={'from':[1,15.75,8],'to':[15,27.75,8],
           'rotation':{'origin':[8,16,8],'axis':'y','angle':angle,'rescale':False},
           'shade':False,
           'faces':{f:{'uv':[0,0,16,16],'texture':'#crystal'} for f in ['north','south']}}
        result=transform_element(e,m)

        axis_vectors={'north':(0,0,-1),'south':(0,0,1)}
        directions={(0,0,-1):'north',(0,0,1):'south',(0,-1,0):'down',
                    (0,1,0):'up',(-1,0,0):'west',(1,0,0):'east'}
        transformed_faces={}
        for face,vec in axis_vectors.items():
            normal=tuple(sum(m[i][k]*vec[k] for k in range(3)) for i in range(3))
            target_face=directions[normal]
            data=copy.deepcopy(e['faces'][face])
            first=transform(face_vertices(face,e['from'],e['to'])[0],m)
            vertices=face_vertices(target_face,result['from'],result['to'])
            index=next(i for i,v in enumerate(vertices) if all(abs(v[j]-first[j])<1e-5 for j in range(3)))
            data['rotation']=(-index*90)%360
            transformed_faces[target_face]=data
        result['faces']=transformed_faces
        shards.append(result)
variants=[('experience_ore','stone','stone','stone_ore_replaceables',3,40),
          ('deepslate_experience_ore','deepslate','deepslate_top','deepslate_ore_replaceables',-17,0)]
for name,base,top,target,min_y,max_y in variants:
    cube={'from':[0,0,0],'to':[16,16,16],'faces':{f:{'uv':[0,0,16,16],
          'texture':'#top' if f in ['up','down'] else '#stone','cullface':f} for f in faces}}
    write_json(f'assets/xpore/models/block/{name}.json',{'parent':'minecraft:block/block','ambientocclusion':True,
        'textures':{'stone':f'minecraft:block/{base}','top':f'minecraft:block/{top}',
        'crystal':'xpore:block/experience_crystal', 'inclusions':'xpore:block/experience_inclusions',
        'particle':f'minecraft:block/{base}'},
        'elements':[cube,{'from':[-0.01,-0.01,-0.01],'to':[16.01,16.01,16.01],
        'faces':{f:{'uv':[0,0,16,16],'texture':'#inclusions','cullface':f} for f in faces}}]+copy.deepcopy(shards),'display':{'gui':{'rotation':[30,225,0],
        'translation':[0,0,0],'scale':[.44,.44,.44]}}})
    write_json(f'assets/xpore/blockstates/{name}.json',{'variants':{'':{'model':f'xpore:block/{name}'}}})
    write_json(f'assets/xpore/models/item/{name}.json',{'parent':f'xpore:block/{name}'})
    write_json(f'data/xpore/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[]})
    write_json(f'data/xpore/worldgen/configured_feature/{name}.json',{'type':'minecraft:ore',
        'config':{'size':3,'discard_chance_on_air_exposure':0,'targets':[{'target':{
        'predicate_type':'minecraft:tag_match','tag':f'minecraft:{target}'},'state':{'Name':f'xpore:{name}'}}]}})
    write_json(f'data/xpore/worldgen/placed_feature/{name}.json',{'feature':f'xpore:{name}','placement':[
        {'type':'minecraft:count','count':4},{'type':'xpore:config_rarity'}, {'type':'minecraft:in_square'},
        {'type':'minecraft:height_range','height':{'type':'minecraft:uniform',
        'min_inclusive':{'absolute':min_y},'max_inclusive':{'absolute':max_y}}},{'type':'minecraft:biome'}]})
for tag in ['mineable/pickaxe','needs_iron_tool']:
    write_json(f'data/minecraft/tags/block/{tag}.json',{'replace':False,
        'values':['xpore:experience_ore','xpore:deepslate_experience_ore']})
write_json('assets/xpore/lang/en_us.json',{'block.xpore.experience_ore':'Experience Ore',
        'block.xpore.deepslate_experience_ore':'Deepslate Experience Ore'})
write_json('assets/xpore/lang/pt_br.json',{'block.xpore.experience_ore':'Minério de Experiência',
        'block.xpore.deepslate_experience_ore':'Minério de Experiência Profundo'})
particle_dir=ROOT/'assets/xpore/textures/particle';particle_dir.mkdir(parents=True,exist_ok=True)
orb=Image.new('RGBA',(8,8),(0,0,0,0))
for y in range(8):
    for x in range(8):
        d=((x-3.5)**2+(y-3.5)**2)**.5
        if d<3.6:orb.putpixel((x,y),(228,255,165,255) if d<1.6 else (105,250,45,int(220*(3.6-d)/2)))
orb.save(particle_dir/'xp_aura.png')
for name in ['xp_aura','xp_trail']:
    write_json(f'assets/xpore/particles/{name}.json',{'textures':['xpore:xp_aura']})
print('Modelos, texturas, mapas emissivos e todos os IDs de recursos atualizados.')
