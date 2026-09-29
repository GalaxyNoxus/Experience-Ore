from pathlib import Path
import argparse
import io
import json
import math
import zipfile
import numpy as np
from PIL import Image, ImageDraw, ImageFont

parser=argparse.ArgumentParser()
parser.add_argument('--minecraft-jar',required=True)
args=parser.parse_args()
root=Path(__file__).resolve().parents[1]
assets=root/'src/main/resources/assets'
vanilla=zipfile.ZipFile(args.minecraft_jar)
w,h=1200,720
pixels=np.full((h,w,3),(20,27,27),dtype=np.uint8)
depth=np.full((h,w),-np.inf)
view=np.array([1.0,.78,1.0]);view/=np.linalg.norm(view)
right=np.array([1.0,0,-1.0]);right/=np.linalg.norm(right)
up=np.cross(view,right)
basis=np.array([right,-up,view])

def vertices(face,lo,hi):
    x,y,z=lo;X,Y,Z=hi
    return np.array({
        'down':[(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)],
        'up':[(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)],
        'north':[(X,Y,z),(X,y,z),(x,y,z),(x,Y,z)],
        'south':[(x,Y,Z),(x,y,Z),(X,y,Z),(X,Y,Z)],
        'west':[(x,Y,z),(x,y,z),(x,y,Z),(x,Y,Z)],
        'east':[(X,Y,Z),(X,y,Z),(X,y,z),(X,Y,z)]}[face],dtype=float)

def triangle(points,uv,texture,light):
    lo=np.maximum(np.floor(points[:,:2].min(axis=0)).astype(int),[0,0])
    hi=np.minimum(np.ceil(points[:,:2].max(axis=0)).astype(int),[w-1,h-1])
    if any(hi<lo):return
    X,Y=np.meshgrid(np.arange(lo[0],hi[0]+1)+.5,np.arange(lo[1],hi[1]+1)+.5)
    a,b,c=points
    den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
    if abs(den)<1e-8:return
    A=((b[1]-c[1])*(X-c[0])+(c[0]-b[0])*(Y-c[1]))/den
    B=((c[1]-a[1])*(X-c[0])+(a[0]-c[0])*(Y-c[1]))/den
    C=1-A-B
    Z=A*a[2]+B*b[2]+C*c[2]
    U=A*uv[0,0]+B*uv[1,0]+C*uv[2,0]
    V=A*uv[0,1]+B*uv[1,1]+C*uv[2,1]
    tx=np.clip((U*texture.shape[1]).astype(int),0,texture.shape[1]-1)
    ty=np.clip((V*texture.shape[0]).astype(int),0,texture.shape[0]-1)
    colors=texture[ty,tx]
    region=depth[lo[1]:hi[1]+1,lo[0]:hi[0]+1]
    mask=(A>=-1e-6)&(B>=-1e-6)&(C>=-1e-6)&(Z>region)&(colors[:,:,3]>127)
    region[mask]=Z[mask]
    target=pixels[lo[1]:hi[1]+1,lo[0]:hi[0]+1]
    target[mask]=np.clip(colors[:,:,:3][mask]*light,0,255).astype(np.uint8)

for variant,cx in [('experience_ore',300),('deepslate_experience_ore',900)]:
    model=json.loads((assets/f'xpore/models/block/{variant}.json').read_text())
    textures={}
    for key,ref in model['textures'].items():
        namespace,path=ref.split(':')
        texture=Image.open(io.BytesIO(vanilla.read(f'assets/minecraft/textures/{path}.png'))) if namespace=='minecraft' else Image.open(assets/f'{namespace}/textures/{path}.png')
        textures[key]=np.array(texture.convert('RGBA'))
    for element in model['elements']:
        for face,settings in element['faces'].items():
            v=vertices(face,element['from'],element['to'])
            rot=element.get('rotation')
            if rot:
                angle=math.radians(rot['angle']);c,s=math.cos(angle),math.sin(angle)
                m={'x':[[1,0,0],[0,c,-s],[0,s,c]],'y':[[c,0,s],[0,1,0],[-s,0,c]],'z':[[c,-s,0],[s,c,0],[0,0,1]]}[rot['axis']]
                origin=np.array(rot['origin']);v=(v-origin)@np.array(m).T+origin
            normal=np.cross(v[1]-v[0],v[2]-v[0]);length=np.linalg.norm(normal)
            if length<1e-8 or np.dot(normal,view)<=0:continue
            normal/=length
            transformed=(v-8)@basis.T
            transformed[:,:2]*=12
            transformed[:,:2]+=np.array([cx,385])
            uv=np.array([[0,0],[0,1],[1,1],[1,0]],dtype=float)
            uv=np.roll(uv,-settings.get('rotation',0)//90,axis=0)
            light=1.0 if not element.get('shade',True) else .70+.30*max(0,np.dot(normal,np.array([.25,.9,.35])))
            texture=textures[settings['texture'][1:]]
            for indices in [(0,1,2),(0,2,3)]:triangle(transformed[list(indices)],uv[list(indices)],texture,light)
img=Image.fromarray(pixels);draw=ImageDraw.Draw(img)
font='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
def text(x,y,s,size,color):draw.text((x,y),s,font=ImageFont.truetype(font,size),fill=color)
text(45,30,'EXPERIENCE ORE  /  1.0.0',27,(216,245,194))
text(45,70,'Shards finos e escuros + pontas verde-claro emissivas',18,(162,182,175))
text(140,590,'Minério de Experiência',22,(227,239,225))
text(718,590,'Minério de Experiência Profundo',22,(227,239,225))
text(45,662,'Prévia técnica dos modelos e texturas. Não é uma captura do Minecraft; sem shaders.',16,(145,166,157))
(root/'docs').mkdir(exist_ok=True)
img.save(root/'docs/model-preview.png')
