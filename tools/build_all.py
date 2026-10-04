import argparse
import os
from pathlib import Path
import subprocess
from build_forge import build_forge
from prepare_release import prepare, targets


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft')
    parser.add_argument('--loader', choices=['fabric', 'forge', 'both'], default='both')
    parser.add_argument('--output', type=Path, default=Path('build/release'))
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    os.chdir(root)
    selected = [t for t in targets() if (args.loader == 'both' or t['loader'] == args.loader)
                and (args.minecraft is None or t['minecraft'] == args.minecraft)]
    if not selected:
        parser.error('No matching target. Forge is unavailable for Minecraft 1.20.5 and 1.21.2.')
    for target in selected:
        loader, minecraft = target['loader'], target['minecraft']
        if loader == 'forge':
            build_forge(minecraft)
            continue
        wrapper = root / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
        command = [str(wrapper)] if os.name == 'nt' else ['bash', str(wrapper)]
        command += [f':{minecraft}:buildAndCollect']
        print(f'Building {loader} {minecraft}', flush=True)
        subprocess.run(command, check=True)
    if args.loader == 'both' and args.minecraft is None:
        prepare(root / 'build/raw', args.output)
    else:
        print('Raw JARs are in build/raw. Consolidate after all targets have been built.')


if __name__ == '__main__':
    main()
