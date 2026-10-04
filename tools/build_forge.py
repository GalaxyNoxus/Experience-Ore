import argparse
import os
from pathlib import Path
import subprocess
from prepare_release import artifact_filename, properties, read_jar, targets


def compare_outputs(latest, minimum, minecraft, version):
    latest_hash = read_jar(latest, 'forge', minecraft, version)[2]
    minimum_hash = read_jar(minimum, 'forge', minecraft, version)[2]
    if latest_hash != minimum_hash:
        raise RuntimeError(f'Forge {minecraft}: minimum and latest builds differ. Review compatibility before publishing.')


def build_forge(minecraft):
    root = Path(__file__).resolve().parents[1]
    os.chdir(root)
    target = next((t for t in targets() if t['loader'] == 'forge' and t['minecraft'] == minecraft), None)
    if target is None:
        raise ValueError('Unsupported Forge target: ' + minecraft)
    minimum = target.get('minimumForge', target['forge'])
    wrapper = root / 'forge' / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
    command = [str(wrapper)] if os.name == 'nt' else ['bash', str(wrapper)]
    command += ['-p', str(root / 'forge'), f'-PminecraftVersion={minecraft}', '--no-daemon', 'buildAndCollect']
    versions = [minimum, target['forge']] if minimum != target['forge'] else [target['forge']]
    for forge in versions:
        print(f'Building Minecraft {minecraft} with Forge {forge}', flush=True)
        subprocess.run(command + [f'-PforgeVersion={forge}'], check=True)
    if minimum != target['forge']:
        props = properties()
        filename = artifact_filename(props['archives_base_name'], props['mod_version'], 'forge', [minecraft])
        compare_outputs(root / 'build/raw/forge' / minecraft / filename,
                        root / 'build/validation/forge' / minecraft / filename,
                        minecraft, props['mod_version'])
        print(f'Forge {minecraft}: minimum and latest builds have identical normalized output.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft', required=True)
    build_forge(parser.parse_args().minecraft)
